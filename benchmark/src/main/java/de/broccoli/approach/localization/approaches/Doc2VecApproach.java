package de.broccoli.approach.localization.approaches;

import de.broccoli.approach.localization.approaches.doc2Vec.DocumentIterator;
import de.broccoli.approach.localization.models.Document;
import de.broccoli.approach.localization.models.LocationResultList;
import de.broccoli.dataimporter.models.Bug;
import de.broccoli.utils.smartshark.SmartSHARKProjectDataProvider;
import de.ugoe.cs.smartshark.model.Issue;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static de.broccoli.approach.localization.util.JavaLanguageConst.JAVA_LANGUAGE_KEYS;
import static de.broccoli.approach.localization.util.JavaLanguageConst.OTHER_STOPWORDS;


public class Doc2VecApproach extends AbstractApproach {

    private DefaultTokenizerFactory tokenizerFactory;
    private ParagraphVectors paragraphVectors;
    private List<Document> files;

    @Override
    public String getApproachName() {
        return "Doc2Vec";
    }

    @Override
    public void buildCache(List<Document> files) {
        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
        this.files = files;
        train();
    }

    @Override
    public void execute(Bug issue, List<Document> files, LocationResultList results) {
        classifiy(issue, results);
    }

    private void classifiy(Bug issue, LocationResultList results) {
        String text = issue.getBugDescription() + issue.getBugSummary();
        List<String> files = new ArrayList<>(paragraphVectors.predictSeveral(text,10));
        for (String file: files) {
            Optional<Document> optionalDocument = this.files.stream().filter(document -> document.getFileName(true).equals(file)).findFirst();
            optionalDocument.ifPresent(document -> results.addPoints(getApproachName(), 1.0D, document));
        }
    }

    private void train() {
        List<String> stopWords = new ArrayList<>(Arrays.asList(JAVA_LANGUAGE_KEYS));
        stopWords.addAll(Arrays.asList(OTHER_STOPWORDS));
        paragraphVectors = new ParagraphVectors.Builder()
                .learningRate(0.025)
                .minLearningRate(0.001)
                .batchSize(100)
                .epochs(10)
                .iterate(new DocumentIterator(files))
                .trainWordVectors(true)
                .tokenizerFactory(tokenizerFactory)
                .stopWords(stopWords)
                .build();

        // Start model training
        paragraphVectors.fit();
    }
}
