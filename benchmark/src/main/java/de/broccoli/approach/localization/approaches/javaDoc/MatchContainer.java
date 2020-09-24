package de.broccoli.approach.localization.approaches.javaDoc;


import de.broccoli.approach.localization.models.Document;

import java.util.List;

public class MatchContainer {

    private Document file;

    private List<String> javaDoc;
    private List<String> classAndImplementation;
    private List<String> methodNames;

    public MatchContainer(Document file, List<String> javaDoc, List<String> classAndImplementation, List<String> methodNames) {
        this.file = file;
        this.javaDoc = javaDoc;
        this.classAndImplementation = classAndImplementation;
        this.methodNames = methodNames;
    }

    public int getScore(List<String> text) {
        int sum = 0;
        for (String word: text) {
            sum += getScore(word,1);
        }
        return sum;
    }

    public int getScore(String word, int toAdd) {
        int score = 0;
        if(javaDoc.contains(word))
        {
            score += toAdd;
        }

        if(classAndImplementation.contains(word))
        {
            score += toAdd;
        }

        if(methodNames.contains(word))
        {
            score += toAdd;
        }

        return score;
    }

    public Document getDocument()
    {
        return file;
    }
}
