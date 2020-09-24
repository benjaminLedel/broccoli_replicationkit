package de.broccoli.context;

import de.broccoli.dataimporter.DataImporter;
import de.broccoli.dataimporter.models.Bug;
import de.broccoli.utils.git.GitService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.util.*;

public class BroccoliContext {

    private static ThreadLocal<BroccoliContext> threadLocal = new ThreadLocal<BroccoliContext>();

    private String baseFolder = "E:\\broccoli\\";
    private String algorithm;

    private String separator;
    private String lineSeparator;
    private boolean windows;

    private String projectName;
    private String sourceCodeDir;
    private String repoDir;
    private String workDir;
    private float alpha;
    private String outputFile;

    private String indriPath;
    private int topN;


    private DataImporter importer;
    private GitService gitService;
    private Repository repo;
    private String model;
    private ArrayList<Bug> cache;

    private Map<String, Integer> globalContextVars = new HashMap<>();
    private String singleBug;

    public BroccoliContext() {
        algorithm = "";
        this.separator = System.getProperty("file.separator");
        this.lineSeparator = System.getProperty("line.separator");
        this.indriPath = "\"C:\\Program Files\\Indri\\Indri 5.6\\bin\\";
        setContextVar("past_days", 15);
        if(System.getProperty("os.name").toLowerCase().contains("windows"))
        {
            windows = true;
        } else {
            windows = false;
        }
        gitService = new GitService();
    }

    public void createBasicContext(DataImporter importer)
    {
        this.cache = null;
        this.importer = importer;
        String bugFolder = "";
        if(singleBug != null)
        {
            bugFolder = separator + singleBug;
        }
        File directory = new File(baseFolder + "workspace" + separator + algorithm + bugFolder);
        if (! directory.exists()){
            directory.mkdirs();
        }
        workDir = directory.getAbsolutePath();
        File outputfolder =  new File("output" + separator + algorithm );
        if( !outputfolder.exists())
            outputfolder.mkdirs();
        outputFile = "output" + separator + algorithm + separator + projectName + "_output.txt";

        //if(singleBug != null)
        //{
          //  createTempFolderAndCheckout(singleBug);
        //}
    }

    public void createTempFolderAndCheckout(Bug bug) {
        try {
            // set the repo to the current state of the issue creation
            // create a complete file index
            repo = gitService.openRepository(this.getRepoDir());
            File checkOut = new File("repospace" + separator + bug.getBugId());
            RevCommit commit = findLatestCommitBeforeDate(bug.getOpenDate());
            gitService.checkout(this.repoDir, checkOut, commit.getId().getName());
            this.sourceCodeDir = checkOut.getAbsolutePath();
        } catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private RevCommit findLatestCommitBeforeDate(Date date) throws GitAPIException {
        RevCommit latestCommit = new Git(repo).log().setMaxCount(1).call().iterator().next();
        Date nearstDate = null;
        try {
            Git git = new Git(repo);
            Iterator<RevCommit> iterator = git.log().all().call().iterator();
            while (iterator.hasNext()) {
                RevCommit commit = iterator.next();
                Date comparedate = commit.getAuthorIdent().getWhen();
                if (date == null || date.after(comparedate)) {
                    if (nearstDate == null || comparedate.after(nearstDate)) {
                        latestCommit = commit;
                        nearstDate = comparedate;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return latestCommit;
    }

    public static BroccoliContext getInstance()
    {
        if(threadLocal.get() == null)
        {
         threadLocal.set(new BroccoliContext());
        }
        return threadLocal.get();
    }

    public String getSeparator() {
        return separator;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getSourceCodeDir() {
        return sourceCodeDir;
    }

    public String getWorkDir() {
        return workDir;
    }

    public float getAlpha() {
        return alpha;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public ArrayList<Bug> getBugList() {
        if(cache == null)
            cache = (ArrayList<Bug>) importer.getBugs();
     //   if(singleBug != null)
       // {
         //   ArrayList<Bug> bugs = new ArrayList<Bug>();
           // bugs.add(singleBug);
           // return bugs;
       // }
        return cache;
    }

    // Interal dataprovider usage

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setSourceCodeDir(String sourceCodeDir) {
        this.sourceCodeDir = sourceCodeDir;
    }

    public void setContextVar(String var, Integer value)
    {
        globalContextVars.put(var, value);
    }

    public Integer getContextVar(String var)
    {
        return globalContextVars.get(var);
    }

    public Map<String, Integer>  getContextVars()
    {
        return globalContextVars;
    }

    public String getIndriPath() {
        return indriPath;
    }

    public void setIndriPath(String indriPath) {
        this.indriPath = indriPath;
    }

    public int getTopN() {
        return topN;
    }

    public void setTopN(int topN) {
        this.topN = topN;
    }

    public String getRepoDir() {
        return repoDir;
    }

    public void setRepoDir(String repoDir) {
        this.repoDir = repoDir;
    }

    public boolean isWindows() {
        return windows;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public DataImporter getImporter() {
        return importer;
    }

    public void setSingleBug(String bugId) {
        this.singleBug = bugId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
