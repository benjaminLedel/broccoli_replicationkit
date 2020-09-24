package de.broccoli.rating;

public class AlgorithmResult {

    private String project;
    private double top1;
    private double top5;
    private double top10;

    private double map;
    private double mrr;

    public AlgorithmResult(String project, double top1, double top5, double top10, double map, double mrr) {
        this.top1 = top1;
        this.top5 = top5;
        this.top10 = top10;
        this.map = map;
        this.mrr = mrr;
        this.project = project;
    }

    public double getTop1() {
        return top1;
    }

    public void setTop1(int top1) {
        this.top1 = top1;
    }

    public double getTop5() {
        return top5;
    }

    public void setTop5(int top5) {
        this.top5 = top5;
    }

    public double getTop10() {
        return top10;
    }

    public void setTop10(int top10) {
        this.top10 = top10;
    }

    public double getMap() {
        return map;
    }

    public void setMap(double map) {
        this.map = map;
    }

    public double getMrr() {
        return mrr;
    }

    public void setMrr(double mrr) {
        this.mrr = mrr;
    }

    public String getProject() {
        return project;
    }
}
