package de.broccoli.approach.localization.models;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LocationResult {

    private Map<String, Double> score;
    private Double classifierScore;
    private Document filename;

    public LocationResult(Document filename) {
        this.score = new HashMap<>();
        this.filename = filename;
    }

    public LocationResult(String approach, double points, Document file) {
        this(file);
        setScore(approach,points);
    }

    /**
     * Return the weigteh sum
     * @return
     */
    public Double getSimpleSum() {
        double scoreSum = 0.0D;
        for (Map.Entry<String, Double> entry : score.entrySet()) {
            scoreSum = scoreSum + entry.getValue();
        }
        return scoreSum;
    }

    /**
     * Retruns the score of a special approach
     * @param approach
     * @return
     */
    public Double getScore(String approach) {
        return score.getOrDefault(approach, 0.0D);
    }

    public void setScore(String approach, double score) {
        this.score.put(approach,score);
    }

    public Document getDocument() {
        return filename;
    }

    public void addScore(String approach, double points) {
        Double savedScore = this.score.getOrDefault(approach,0.0D);
        savedScore += points;
        setScore(approach,savedScore);
    }

    public Set<String> getApproachs() {
        return score.keySet();
    }

    public void setClassifierScore(double result) {
        classifierScore = result;
    }

    public Double getClassifierScore() {
        if(classifierScore == null)
            return 0.0d;
        return classifierScore;
    }
}
