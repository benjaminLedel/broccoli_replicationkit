package de.broccoli.utils;

import java.io.File;

public class ProjectConfiguration {

    private String project;
    private String version;
    private File bugRepo;
    private File gitRepo;
    private File sources;

    public ProjectConfiguration(String project, String version, File bugRepo, File gitRepo, File sources) {
        this.version = version;
        this.project = project;
        this.bugRepo = bugRepo;
        this.gitRepo = gitRepo;
        this.sources = sources;
    }

    public File getBugRepo() {
        return bugRepo;
    }

    public void setBugRepo(File bugRepo) {
        this.bugRepo = bugRepo;
    }

    public File getGitRepo() {
        return gitRepo;
    }

    public void setGitRepo(File gitRepo) {
        this.gitRepo = gitRepo;
    }

    public File getSources() {
        return sources;
    }

    public void setSources(File sources) {
        this.sources = sources;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String toString()
    {
        return  project + "/" + version + " :" + getBugRepo().getName() + " " + getGitRepo().getName() + " " + getSources().getName();
    }
}
