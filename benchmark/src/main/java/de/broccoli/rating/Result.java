package de.broccoli.rating;

public class Result {

    private String bugId;
    private String file;
    private int rank;
    private double score;

    public Result(String[] entries) {
        bugId = entries[0];
        file = entries[1];
        rank = Integer.parseInt(entries[2]);
        score = Double.parseDouble(entries[3]);
    }

    public String getBugId() {
        return bugId;
    }

    public void setBugId(String bugId) {
        this.bugId = bugId;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
