package de.broccoli.approach.localization;


import de.broccoli.approach.localization.api.Approach;
import de.broccoli.approach.localization.api.Trainer;
import de.broccoli.approach.localization.classifier.DataReaderWriter;
import de.broccoli.approach.localization.classifier.PrintContext;
import de.broccoli.approach.localization.classifier.WekaClassifier;
import de.broccoli.approach.localization.models.LocationResult;
import de.broccoli.approach.localization.models.LocationResultList;
import de.broccoli.approach.localization.util.ApproachFactory;
import de.broccoli.approach.localization.util.FileUtils;
import de.broccoli.approach.localization.util.ScoreMatcher;
import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.models.Bug;
import de.ugoe.cs.smartshark.model.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BroccoliLocalizationTrainer implements Trainer {

    private WekaClassifier classifier;
    private Logger logger = LoggerFactory.getLogger(BroccoliLocalizationTrainer.class.getName());
    private List<Approach> approaches = new ArrayList<>();
    private List<String> projects;

    @Override
    public void initForProject(List<String> projects) {
        this.projects = projects;
        classifier = new WekaClassifier();
        approaches = ApproachFactory.getApproaches();
    }

    @Override
    public void train(String modelFile) throws Exception {
        String trainFile = createCSVIndexFile();
        DataReaderWriter writer = new DataReaderWriter(trainFile);
        classifier.train(writer.getInstancesFromFile(), modelFile);
        // delete train file?
    }

    public void trainCross(int folds, String modelFile) throws Exception {
        String trainFile = createCSVIndexFile();
        DataReaderWriter writer = new DataReaderWriter(trainFile);
        classifier.trainCrossSingle(folds, writer.getInstancesFromFile(), modelFile);
    }

    public String createCSVIndexFile() {
        DataReaderWriter writer = new DataReaderWriter();
        PrintContext context = writer.startCSV(approaches);
        for (String project: projects) {
            try {
                // we use here a instance per project to train our model
                BroccoliLocalizationRunner runner = new BroccoliLocalizationRunner();
                runner.init(project,BroccoliContext.getInstance().getModel());
                List<Bug> issues = BroccoliContext.getInstance().getBugList();
                logger.info("Found " + issues.size() + " issues in project " + project +" to analyze");
                int processed = 0;

                runner.prepare();

                for (Bug issue : issues) {
                    // First we will get the real changed files from the database
                    List<String> filenamesOfCommit = new ArrayList<>(issue.getSet());
                    // Then we will remove all files that are not valid
                    // filenamesOfCommit = filenamesOfCommit.stream().filter(FileUtils::isValidFile).collect(Collectors.toList());
                    if (filenamesOfCommit.size() > 0) {
                        LocationResultList bugLocations = runner.runForApproachs(issue);
                        List<Integer> contained = new ArrayList<>();
                        for (LocationResult bugLocation: bugLocations) {
                            boolean match =  filenamesOfCommit.stream().filter(s -> BroccoliLocalizationRunner.isInResultSet(s, bugLocation)).count() > 0;
                            contained.add(match ?  1 : 0);
                        }
                        if(!contained.contains(1))
                        {
                            logger.warn("no file in this bug is possible");
                        }
                        writer.appendToCSV(issue.getBugId(),bugLocations,contained,context);
                    }
                    processed++;
                    if (processed % 10 == 0) {
                        logger.info("Processed: " + processed + "/" + issues.size());
                    }
                }

                runner.shutdown();

            } catch (Exception e)
            {
                logger.error("Failed for project " + project + " training");
                e.printStackTrace();
            }
        }
        writer.closeCSV(context);
        return writer.getFile();
    }

    @Override
    public void shutdown() {

    }
}
