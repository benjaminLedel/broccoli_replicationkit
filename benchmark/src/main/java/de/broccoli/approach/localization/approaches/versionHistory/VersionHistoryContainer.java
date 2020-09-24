package de.broccoli.approach.localization.approaches.versionHistory;

import de.broccoli.approach.localization.models.Document;

public class VersionHistoryContainer {

    private Document document;
    private Double score;

    public VersionHistoryContainer(Document document, Double score) {
        this.document = document;
        this.score = score;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
