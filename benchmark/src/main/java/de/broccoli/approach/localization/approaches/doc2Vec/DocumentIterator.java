package de.broccoli.approach.localization.approaches.doc2Vec;

import de.broccoli.approach.localization.models.Document;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.sentenceiterator.labelaware.LabelAwareSentenceIterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DocumentIterator implements LabelAwareSentenceIterator {

    private SentencePreProcessor preProcessor;
    private Iterator<Document> iterator;
    private List<Document> files;
    private Document currentDoc;

    public DocumentIterator(List<Document> files) {
        this.files = files;
        reset();
    }

    @Override
    public String nextSentence() {
        currentDoc = iterator.next();
        return currentDoc.getContent();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public void reset() {
        iterator = files.iterator();
    }

    @Override
    public void finish() {
        // nothing to do...
    }

    @Override
    public SentencePreProcessor getPreProcessor() {
        return preProcessor;
    }

    @Override
    public void setPreProcessor(SentencePreProcessor preProcessor) {
        this.preProcessor = preProcessor;
    }

    @Override
    public String currentLabel() {
        return currentDoc.getFileName(true);
    }

    @Override
    public List<String> currentLabels() {
        return Arrays.asList(currentLabel());
    }
}
