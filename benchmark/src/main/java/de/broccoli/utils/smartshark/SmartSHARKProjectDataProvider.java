package de.broccoli.utils.smartshark;

import de.broccoli.utils.git.GitService;
import de.ugoe.cs.smartshark.model.*;
import org.bson.types.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.mongodb.morphia.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SmartSHARKProjectDataProvider {

    private Project project;
    private SmartSHARKDatabaseProvider databaseProvider;
    private GitService gitService;
    private VCSSystem vcsSystem;

    public SmartSHARKProjectDataProvider(String project)
    {
        databaseProvider = new SmartSHARKDatabaseProvider();
        gitService = new GitService();

        Query<Project> projects = databaseProvider.getDatastore().createQuery(Project.class);
        projects.and(
                projects.criteria("name").equal(project)
        );
        this.project = projects.get();

        Query<VCSSystem> vcsSystems = databaseProvider.getDatastore().createQuery(VCSSystem.class);
        vcsSystems.and(
                vcsSystems.criteria("project_id").equal(this.project.getId())
        );
        this.vcsSystem = vcsSystems.get();
    }

    public Repository openGitRepository() throws Exception {
        String path = Common.loadRepoFromMongoDB(project.getName(), databaseProvider.getMongoClient());
        return gitService.openRepository(path);
    }

    public List<Issue> getIssues() {
        Query<IssueSystem> systems = databaseProvider.getDatastore().createQuery(IssueSystem.class);
        systems.and(systems.criteria("projectId").equal(project.getId()));

        for (IssueSystem system: systems) {
            Query<Issue> issues = databaseProvider.getDatastore().createQuery(Issue.class);
            issues.and(issues.criteria("issueSystemId").equal(system.getId()));
            return issues.asList();
        }
        return new ArrayList<>();
    }

    public List<Commit> getLinkedCommits(ObjectId issueId, ObjectId vcsSystem) {
        Query<Commit> systems = databaseProvider.getDatastore().createQuery(Commit.class);
        systems.and(systems.criteria("vcs_system_id").equal(vcsSystem)).and(systems.criteria("linked_issue_ids").hasThisOne(issueId));
        return systems.asList();
    }

    public List<File> getFilesOfCommit(Commit commit) {
        Query<FileAction> systems = databaseProvider.getDatastore().createQuery(FileAction.class);
        systems.and(systems.criteria("commit_id").equal(commit.getId()));

        List<File> files = new ArrayList<>();
        for (FileAction action: systems) {
            Query<File> issues = databaseProvider.getDatastore().createQuery(File.class);
            issues.and(issues.criteria("_id").equal(action.getFileId()));
            files.addAll(issues.asList());
        }
        return files;
    }

    public String getCommentsOfIssue(Issue issue) {
        Query<IssueComment> comments = databaseProvider.getDatastore().createQuery(IssueComment.class);
        comments.and(comments.criteria("issue_id").equal(issue.getId()));
        StringBuilder commentsContent = new StringBuilder();
        for (IssueComment comment : comments)
        {
            commentsContent.append(" ").append(comment.getComment());
        }
        return commentsContent.toString();
    }

    public List<String> getRealFileFromCommitsOfIssue(Issue issue) {
        List<Commit> commits = getLinkedCommits(issue.getId(), this.vcsSystem.getId());
        List<String> files = new ArrayList<>();
        for (Commit commit: commits) {
            List<File> filesDB = getFilesOfCommit(commit);
            for (File fileDB: filesDB) {
                String path = fileDB.getPath();
                files.add(path);
            }
        }
        // onyl java files, since some approaches are stupid
        return files.stream().filter(s -> s.contains(".java"))
                .collect(Collectors.toList());
    }

    public Project getProject() {
        return project;
    }

    public SmartSHARKDatabaseProvider getDatabaseProvider() {
        return databaseProvider;
    }

    public GitService getGitService() {
        return gitService;
    }

}
