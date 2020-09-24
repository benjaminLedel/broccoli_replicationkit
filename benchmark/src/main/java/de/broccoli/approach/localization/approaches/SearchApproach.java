package de.broccoli.approach.localization.approaches;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import de.broccoli.approach.localization.models.Document;
import de.broccoli.approach.localization.models.LocationResultList;
import de.broccoli.approach.localization.util.PreProcessingUtils;
import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.models.Bug;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.Doc;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SearchApproach extends AbstractApproach {

    private RestHighLevelClient client;
    private String index;
    private Logger logger = LoggerFactory.getLogger(SearchApproach.class.getName());

    public SearchApproach() {
        // Connect to elastic
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));
        index = BroccoliContext.getInstance().getProjectName() + "_files";
        index = index.toLowerCase();
    }

    @Override
    public String getApproachName() {
        return "elastic";
    }

    public List<String> getApproachLabels() {
        return Arrays.asList("elastic_full","elastic_method","elastic_pfad");
    }

    @Override
    public void buildCache(List<Document> files) {

        try {
            DeleteByQueryRequest request = new DeleteByQueryRequest(index);
            request.setQuery(QueryBuilders.matchAllQuery());
            client.deleteByQuery(request, RequestOptions.DEFAULT);

        } catch (Exception e) {
            // e.printStackTrace();
        }

        try {
            int i = 0;
            for (Document d : files) {


                XContentBuilder builder = XContentFactory.jsonBuilder();
                builder.startObject();
                {
                    builder.field("name", d.getFileName(true));
                    builder.field("content", d.getContent());
                    builder.field("path", d.getProjectPath());
                    builder.field("javaDoc", d.getJavaDoc());
                    CompilationUnit unit = d.getCompilationUnit();
                    if (unit != null) {

                        List<TypeDeclaration> classOrInterfaceDeclarations = unit.findAll(TypeDeclaration.class);
                        List<String> methodNames = classOrInterfaceDeclarations.stream()
                                .map(typeDeclaration -> typeDeclaration.findAll(MethodDeclaration.class))
                                .flatMap(Collection::stream)
                                .map(NodeWithSimpleName::getNameAsString)
                                .collect(Collectors.toList());
                        builder.field("methodNames", methodNames);

                        List<String> delecatrionNames = classOrInterfaceDeclarations.stream()
                                .map(TypeDeclaration::getName)
                                .map(SimpleName::asString)
                                .collect(Collectors.toList());
                        builder.field("className", delecatrionNames);
                    }
                }
                builder.endObject();
                IndexRequest indexRequest = new IndexRequest(index).id(String.valueOf(i)).source(builder);
                try {
                    IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);
                } catch (ElasticsearchException e) {
                    e.printStackTrace();
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(Bug issue, List<Document> files, LocationResultList results) {
        try {

            searchFullText(issue,files,results);
            searchMethodNames(issue,files,results);
            searchPfad(issue,files,results);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            try {
                DeleteByQueryRequest request = new DeleteByQueryRequest(index);
                request.setQuery(QueryBuilders.matchAllQuery());
                client.deleteByQuery(request, RequestOptions.DEFAULT);

            } catch (Exception e) {
                e.printStackTrace();
            }
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void searchFullText(Bug issue, List<Document> files, LocationResultList results) throws IOException {
        List<String> searchWords = PreProcessingUtils.instance.preProcessNaturalLanguage(issue.getBugSummary() + " " + issue.getBugDescription());
        SearchSourceBuilder builder = new SearchSourceBuilder();
        QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("content",searchWords.stream().collect(Collectors.joining(" ")))
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .autoGenerateSynonymsPhraseQuery(true)
                .maxExpansions(10);

        builder.query(matchQueryBuilder);
        int gesamt = Math.min(files.size(), 10000);
        builder.size(gesamt);
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        searchRequest.source(builder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] searchHits = response.getHits().getHits();
        if(searchHits.length > 0) {
            double maxValue = searchHits[0].getScore();
            for (SearchHit hit : searchHits) {
                Document d = files.get(Integer.parseInt(hit.getId()));
                results.addPoints("elastic_full", hit.getScore()/maxValue, d);
            }
        }
    }

    private void searchMethodNames(Bug issue, List<Document> files, LocationResultList results) throws IOException {
        List<String> searchWords = PreProcessingUtils.instance.preProcessNaturalLanguage(issue.getBugSummary() + " " + issue.getBugDescription());
        SearchSourceBuilder builder = new SearchSourceBuilder();
        String text = searchWords.stream().collect(Collectors.joining(" "));
        QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("methodNames",text)
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .maxExpansions(10);

        builder.query(matchQueryBuilder);
        int gesamt = Math.min(files.size(), 10000);
        builder.size(gesamt);
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        searchRequest.source(builder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] searchHits = response.getHits().getHits();
        if(searchHits.length > 0) {
            double maxValue = searchHits[0].getScore();
            for (SearchHit hit : searchHits) {
                Document d = files.get(Integer.parseInt(hit.getId()));
                results.addPoints("elastic_method", hit.getScore()/maxValue, d);
            }
        }
    }

    private void searchPfad(Bug issue, List<Document> files, LocationResultList results) throws IOException {
        String all = issue.getBugSummary() + " " + issue.getBugDescription();

        SearchSourceBuilder builder = new SearchSourceBuilder();
        String text = Arrays.stream(all.split(" ")).filter(s -> s.contains("/") || s.contains(".java")).collect(Collectors.joining(" "));

        QueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery(text,"path")
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .maxExpansions(10);

        builder.query(matchQueryBuilder);
        int gesamt = Math.min(files.size(), 10000);
        builder.size(gesamt);
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        searchRequest.source(builder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] searchHits = response.getHits().getHits();
        if(searchHits.length > 0) {
            double maxValue = searchHits[0].getScore();
            for (SearchHit hit : searchHits) {
                Document d = files.get(Integer.parseInt(hit.getId()));
                results.addPoints("elastic_pfad", hit.getScore()/maxValue, d);
            }
        }
    }
}
