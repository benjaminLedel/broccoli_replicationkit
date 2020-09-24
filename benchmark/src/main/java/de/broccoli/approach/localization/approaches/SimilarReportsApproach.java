package de.broccoli.approach.localization.approaches;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import de.broccoli.approach.localization.approaches.versionHistory.VersionHistoryContainer;
import de.broccoli.approach.localization.models.Document;
import de.broccoli.approach.localization.models.LocationResultList;
import de.broccoli.approach.localization.util.PreProcessingUtils;
import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.models.Bug;
import org.apache.http.HttpHost;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SimilarReportsApproach extends AbstractApproach {

    private RestHighLevelClient client;
    private Map<String, Document> pathToDocument;
    private String index;

    public SimilarReportsApproach() {
        // Connect to elastic
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));
        index = BroccoliContext.getInstance().getProjectName() + "_reports";
        index = index.toLowerCase();
    }
    @Override
    public String getApproachName() {
        return "similarReports";
    }

    @Override
    public void buildCache(List<Document> files) {
        pathToDocument = new HashMap<>();
        for (Document d: files) {
            pathToDocument.put(d.getProjectPath(), d);
        }

        try {
            DeleteByQueryRequest request = new DeleteByQueryRequest(index);
            request.setQuery(QueryBuilders.matchAllQuery());
            client.deleteByQuery(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
           // e.printStackTrace();
        }

        try {
            int i = 0;
            for(Bug bug : BroccoliContext.getInstance().getBugList())
            {

                XContentBuilder builder = XContentFactory.jsonBuilder();
                builder.startObject();
                {
                    builder.field("summary", bug.getBugSummary());
                    builder.field("content", bug.getBugSummary() + " " + bug.getBugDescription());
                    builder.field("fixedFiles", bug.getSet());
                    builder.field("openDate", bug.getOpenDate());
                    builder.field("fixedDate", bug.getFixDate());
                }
                builder.endObject();
                IndexRequest indexRequest = new IndexRequest(index).id(bug.getBugId()).source(builder);
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


    @Override
    public void execute(Bug issue, List<Document> files, LocationResultList results) {
        try {

            searchFullText(issue,files,results);
            searchContent(issue,files,results);

        } catch (Exception e) {
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

        QueryBuilder two = QueryBuilders.rangeQuery("fixedDate").lt(issue.getOpenDate());

        QueryBuilder both = QueryBuilders.boolQuery().should(two).should(matchQueryBuilder);
        builder.query(both);
        builder.size(100);
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        searchRequest.source(builder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] searchHits = response.getHits().getHits();
        Map<Document, Float> treffer = new HashMap<>();
        for (SearchHit hit : searchHits) {
            // not the same bug
            if(hit.getId().equals(issue.getBugId()))
                continue;

            List<String> field = (List<String>) hit.getSourceAsMap().get("fixedFiles");
            for(String fileName: field)
            {
                   Document d = pathToDocument.get(fileName);
                   if(d != null)
                   {
                       treffer.put(d,hit.getScore());
                   }
            }
        }
        List<Document> sorted = files.stream().sorted(new Comparator<Document>() {
            @Override
            public int compare(Document o1, Document o2) {
                if(!treffer.containsKey(o1) && !treffer.containsKey(o2))
                    return 0;
                if(!treffer.containsKey(o1) && treffer.containsKey(o2))
                    return -1;
                if(treffer.containsKey(o1) && !treffer.containsKey(o2))
                    return 1;
                return treffer.get(o1).compareTo(treffer.get(o2));
            }
        }).collect(Collectors.toList());
        int i = 0;
        int gesamt = files.size();
        for (Document file :sorted)
        {
            results.addPoints("elastic_similarReports_fullSearch", (double)i/(double)gesamt, file);
            i++;
        }
        //
    }

    private void searchContent(Bug issue, List<Document> files, LocationResultList results) throws IOException {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("content", QueryParserUtil.escape(issue.getBugDescription()))
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .autoGenerateSynonymsPhraseQuery(true)
                .maxExpansions(10);
        QueryBuilder two = QueryBuilders.rangeQuery("fixedDate").lt(issue.getOpenDate());

        QueryBuilder both = QueryBuilders.boolQuery().should(two).should(matchQueryBuilder);

        builder.query(both);
        builder.size(100);
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        searchRequest.source(builder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] searchHits = response.getHits().getHits();
        Map<Document, Float> treffer = new HashMap<>();
        for (SearchHit hit : searchHits) {
            // not the same bug
            if(hit.getId().equals(issue.getBugId()))
                continue;
            List<String> field = (List<String>) hit.getSourceAsMap().get("fixedFiles");
            for(String fileName: field)
            {
                Document d = pathToDocument.get(fileName);
                if(d != null)
                {
                    treffer.put(d,hit.getScore());
                }
            }
        }
        List<Document> sorted = files.stream().sorted(new Comparator<Document>() {
            @Override
            public int compare(Document o1, Document o2) {
                if(!treffer.containsKey(o1) && !treffer.containsKey(o2))
                    return 0;
                if(!treffer.containsKey(o1) && treffer.containsKey(o2))
                    return -1;
                if(treffer.containsKey(o1) && !treffer.containsKey(o2))
                    return 1;
                return treffer.get(o1).compareTo(treffer.get(o2));
            }
        }).collect(Collectors.toList());
        int i = 0;
        int gesamt = files.size();
        for (Document file :sorted)
        {
            results.addPoints("elastic_similarReports_content", (double)i/(double)gesamt, file);
            i++;
        }
        //
    }
}
