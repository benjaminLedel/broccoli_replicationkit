package de.broccoli.approach.localization.approaches;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import de.broccoli.approach.localization.approaches.javaDoc.MatchContainer;
import de.broccoli.approach.localization.models.Document;
import de.broccoli.approach.localization.models.LocationResultList;
import de.broccoli.approach.localization.util.PreProcessingUtils;
import de.broccoli.dataimporter.models.Bug;
import de.broccoli.utils.smartshark.SmartSHARKProjectDataProvider;
import de.ugoe.cs.smartshark.model.Issue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class JavaDocAndMethodsApproach extends AbstractApproach {

    private List<MatchContainer> index;

    private List<Document> files;

    public static String LABEL_JAVA_SEARCH = "java_search_match";
    public static String LABEL_DOT_WORDS = "dot_words_match";
    public static String LABEL_JAVA_CLASS_AND_METHODS = "class_and_method_match";

    @Override
    public String getApproachName() {
        return "JavaDocAndMethods";
    }

    @Override
    public void buildCache(List<Document> files) {
        this.files = files;
        index = new ArrayList<>();
        buildIndex();
    }

    @Override
    public void execute(Bug issue, List<Document> files, LocationResultList results) {

        String text = issue.getBugSummary() + issue.getBugDescription();
        List<String> searchWords = PreProcessingUtils.instance.preProcessNaturalLanguage(text);
        List<String> dotWords = PreProcessingUtils.instance.findWordsWithDots(text);
        List<String> javaWords = PreProcessingUtils.instance.findJavaMethods(text);

        // int i = 0;

        for (MatchContainer container: index) {
            // System.out.println(index.size() + " Index done " + i);
            results.addPoints(LABEL_JAVA_SEARCH, container.getScore(searchWords), container.getDocument());
            results.addPoints(LABEL_DOT_WORDS, container.getScore(dotWords), container.getDocument());
            results.addPoints(LABEL_JAVA_CLASS_AND_METHODS, container.getScore(javaWords), container.getDocument());
            // i++;
        }
    }

    @Override
    public List<String> getApproachLabels() {
        return Arrays.asList(LABEL_JAVA_SEARCH,LABEL_DOT_WORDS,LABEL_JAVA_CLASS_AND_METHODS);
    }

    private void buildIndex() {
        for (Document file: files) {
           CompilationUnit unit = file.getCompilationUnit();
           if(unit == null)
           {
               // this file will be skipped
               continue;
           }
            List<String> javaDoc = file.getJavaDoc();
            List<TypeDeclaration> classOrInterfaceDeclarations = unit.findAll(TypeDeclaration.class);
            List<String> delecatrionNames = classOrInterfaceDeclarations.stream()
                    .map(TypeDeclaration::getName)
                    .map(SimpleName::asString)
                    .collect(Collectors.toList());
            List<String> methodNames = classOrInterfaceDeclarations.stream()
                    .map(typeDeclaration -> typeDeclaration.findAll(MethodDeclaration.class))
                    .flatMap(Collection::stream)
                    .map(NodeWithSimpleName::getNameAsString)
                    .collect(Collectors.toList());

            // clean the values
            //tokenization with lemmatization,part of speech taggin,sentence segmentation
            javaDoc = javaDoc.stream()
                    .flatMap(s -> PreProcessingUtils.instance.preProcessNaturalLanguage(s).stream())
                    .collect(Collectors.toList());

            delecatrionNames = delecatrionNames.stream().flatMap(s -> PreProcessingUtils.instance.preProcessJavaLangaugeName(s).stream()).collect(Collectors.toList());
            methodNames = methodNames.stream().flatMap(s -> PreProcessingUtils.instance.preProcessJavaLangaugeName(s).stream()).collect(Collectors.toList());

            index.add(new MatchContainer(file,javaDoc,delecatrionNames,methodNames));
        }
    }

}
