package de.broccoli.dataimporter.smartshark;

import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.DataImporter;
import de.broccoli.dataimporter.models.Bug;
import de.broccoli.utils.smartshark.SmartSHARKProjectDataProvider;
import de.ugoe.cs.smartshark.model.Issue;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.List;

public class SmartSharkDataImporter implements DataImporter {

    private SmartSHARKProjectDataProvider provider;
    private Repository repository;
    private List<Bug> bugs = new ArrayList<>();

    public SmartSharkDataImporter(String projectName)
    {
        provider = new SmartSHARKProjectDataProvider(projectName);
        try {
            repository = provider.openGitRepository();
            createContext();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createContext()
    {
        BroccoliContext context = BroccoliContext.getInstance();
        context.setProjectName(provider.getProject().getName());
        context.setSourceCodeDir(repository.getDirectory().getAbsolutePath().replace("\\.git",""));
        context.setRepoDir(repository.getDirectory().getAbsolutePath().replace("\\.git",""));
        context.createBasicContext(this);
    }

    public SmartSHARKProjectDataProvider getProvider() {
        return provider;
    }

    @Override
    public List<Bug> getBugs() {
       if(bugs.size() != 0)
       {
            return bugs;
       }
       List<Issue> issues = provider.getIssues();
       int i = 0;
        for (Issue issue: issues) {
            Bug bug = new Bug();
            // dummy id, since some implementation use this "id" and parse it to integer values ...
            bug.setBugId(String.valueOf(i));
            bug.setBugSummary(issue.getTitle());
            bug.setBugDescription(issue.getDesc());
            bug.setOpenDate(issue.getCreatedAt());
            bug.setFixDate(issue.getUpdatedAt());
            List<String> files = provider.getRealFileFromCommitsOfIssue(issue);
            for (String file: files) {
                bug.addFixedFile(file);
            }
            if(bug.getSet().size() > 0) {
                bugs.add(bug);
                i++;
            }
        }
        return bugs;
    }

    @Override
    public void clearData() {
        try {
            FileUtils.deleteDirectory(new File(BroccoliContext.getInstance().getWorkDir()));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        try {
            FileUtils.deleteDirectory(new File(BroccoliContext.getInstance().getSourceCodeDir()));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
