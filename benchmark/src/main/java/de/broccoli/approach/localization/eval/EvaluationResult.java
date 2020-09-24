package de.broccoli.approach.localization.eval;

public class EvaluationResult {

    private int[] countsMetrics;
    private int issuesWithFiles;
    private String project;

    public EvaluationResult(int[] countsMetrics, int issuesWithFiles, String project) {
        this.countsMetrics = countsMetrics;
        this.issuesWithFiles = issuesWithFiles;
        this.project = project;
    }

    public int[] getCountsMetrics() {
        return countsMetrics;
    }

    public int getIssuesWithFiles() {
        return issuesWithFiles;
    }

    public String getProject() {
        return project;
    }
}
