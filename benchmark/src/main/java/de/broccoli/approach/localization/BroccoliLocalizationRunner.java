package de.broccoli.approach.localization;

import de.broccoli.approach.localization.api.Approach;
import de.broccoli.approach.localization.api.Runner;
import de.broccoli.approach.localization.classifier.WekaClassifier;
import de.broccoli.approach.localization.models.Document;
import de.broccoli.approach.localization.models.LocationResult;
import de.broccoli.approach.localization.models.LocationResultList;
import de.broccoli.approach.localization.util.ApproachFactory;
import de.broccoli.approach.localization.util.FileUtils;
import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.DataImporter;
import de.broccoli.dataimporter.models.Bug;
import de.broccoli.rating.AlgorithmResult;
import de.broccoli.utils.git.GitService;
import de.broccoli.utils.smartshark.Common;
import de.broccoli.utils.smartshark.SmartSHARKProjectDataProvider;
import de.ugoe.cs.smartshark.model.Issue;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BroccoliLocalizationRunner implements Runner {

    private DataImporter projectDataProvider;
    private Logger logger = LoggerFactory.getLogger(BroccoliLocalizationRunner.class.getName());
    private List<Approach> approaches = new ArrayList<>();
    private WekaClassifier classifier;
    private BufferedWriter writer;
    private List<Document> files;


    @Override
    public void init(String projectName, String modelName) throws Exception
    {
        if(modelName != null) {
            classifier = new WekaClassifier();
            classifier.loadModel(modelName);
        } else {
            logger.warn("Not using a general selection model");
        }
        writer = new BufferedWriter(new FileWriter(BroccoliContext.getInstance().getOutputFile()));
        projectDataProvider = BroccoliContext.getInstance().getImporter();

        approaches = ApproachFactory.getApproaches();
    }

    @Override
    public void run() throws Exception {
        List<Bug> issues = projectDataProvider.getBugs();
        logger.info( "Found " + issues.size() + " Issues to analyze");
        int size = issues.size();
        int i = 0;
        int detected = 0;

        prepare();

        for (Bug issue: issues) {
            List<LocationResult> results = locateBugForIssue(issue);
            if(results.size() > 0)
            {
                detected++;
            }
            i++;
            logger.info("Issue (" + i + "/" + size + ") ready");
            // write results to final file
            writeResult(issue, results);

        }
        logger.info("Issue (" + detected + "/" + size + ") detected");
        shutdown();
    }

    public void optimize()
    {

    }

    public void writeResult(Bug issue, List<LocationResult> results) throws IOException {
        int i = 0;
        BufferedWriter fullWriter = new BufferedWriter(new FileWriter(BroccoliContext.getInstance().getWorkDir() + BroccoliContext.getInstance().getSeparator() + issue.getBugId() + ".txt"));
        for (LocationResult result : results) {
            String filename = result.getDocument().getProjectPath();
            if(issue.getSet().contains(filename))
            {
                writer.write(issue.getBugId() + "\t" + filename + "\t" + i + "\t" + result.getClassifierScore() + "\n");
            }
            fullWriter.write(issue.getBugId() + "\t" + filename + "\t" + i + "\t" + result.getClassifierScore() + "\n");
            i++;
        }
        fullWriter.close();
    }


    @Override
    public List<Approach> getApproaches() {
        return approaches;
    }

    @Override
    public void prepare() {
        // 0. create some basic data
        logger.info("Start building file index");
        files = createFileIndex();
        logger.info("Finish building file index");


        for (Approach approach: approaches) {
            logger.info("Start building cache for approach " + approach.getApproachName());
            approach.buildCache(files);
            logger.info("Finish building cache for approach " + approach.getApproachName());
        }
    }

    public void shutdown() throws IOException {
        for (Approach approach: approaches) {
            logger.info("Shutting down approach " + approach.getApproachName());
            approach.shutdown();
            logger.info("Finish shutting down for approach " + approach.getApproachName());
        }
        writer.close();
    }

    /**
     * The returned list will be ordered
     * @param issue
     * @return
     * @throws Exception
     */
    public List<LocationResult> locateBugForIssue(Bug issue) throws Exception {
        LocationResultList data = runForApproachs(issue);
        if(classifier != null) {
            // the result will update the scores of each data object
            classifier.predict(data, getApproaches());
            // Order the list
            data.sort((result, t1) -> t1.getClassifierScore().compareTo(result.getClassifierScore()));
            return data;
        }

        data.sort((result, t1) -> t1.getSimpleSum().compareTo(result.getSimpleSum()));
        return data;
    }

    /**
     * Returned object is *not* sorted by the score! just a execution of all registered approaches
     * @param issue
     * @return
     * @throws Exception
     */
    public LocationResultList runForApproachs(Bug issue) throws Exception {
        // This will be the final result
        LocationResultList results = new LocationResultList();

        results.addAll(files);


        if(BroccoliContext.getInstance().getContextVar("realistic") != null &&
                BroccoliContext.getInstance().getContextVar("realistic") == 1)
        {
            // checks if it is realistic that we can predict the file
            int countFindable = 0;
            for (String file : issue.getSet())
            {
                if(isInResultSet(file, results))
                {
                    countFindable++;
                }
            }
            if(countFindable < issue.getSet().size())
            {
                logger.warn("Missing files in tree (only " + countFindable + " from " + issue.getSet().size() +")" );
                return results;
            }
        }

            for (Approach approach : approaches) {
                logger.info("Start approach " + approach.getApproachName());
                approach.execute(issue, files, results);
                logger.info("Finish approach " + approach.getApproachName());
            }

        return results;
    }

    private List<Document> createFileIndex() {
        Path start = Paths.get(BroccoliContext.getInstance().getSourceCodeDir());
        List<Document> documents = new ArrayList<>();
        Collection<File> files = org.apache.commons.io.FileUtils.listFiles(start.toFile(), new String[]{"java"}, true);
        for (File file: files) {
            if(FileUtils.isValidFile(file.getName()))
            {
                Document d = FileUtils.getDocumentOfFile(file);
                FileUtils.applyRootPath(start,d);
                documents.add(d);
            }
        }
//        try (Stream<Path> stream = Files.walk(start, Integer.MAX_VALUE)) {
//            return stream
//                    .filter(Files::isRegularFile)
//                    .filter(FileUtils::isValidFile)
//                    .map(FileUtils::getDocumentOfFile)
//                    .map(d -> FileUtils.applyRootPath(start,d))
//                    .collect(Collectors.toList());
//
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//        }
        return documents;
    }

    public static boolean isInResultSet(String filename, List<LocationResult> locationResults)
    {
        for (LocationResult locationResult : locationResults)
        {
            if(isInResultSet(filename,locationResult))
                return true;
        }
        return false;

    }
    public static boolean isInResultSet(String filename, LocationResult locationResult)
    {
        filename = filename.toLowerCase();
        String path = locationResult.getDocument().getProjectPath().toLowerCase();
        String packageName = locationResult.getDocument().getJavaName().toLowerCase();
        return filename.equals(path) || filename.equals(packageName) || filename.equals(packageName + ".java");
    }

}
