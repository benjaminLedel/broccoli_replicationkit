package de.broccoli.approach.localization.eval;

import de.broccoli.approach.localization.BroccoliLocalizationRunner;
import de.broccoli.approach.localization.models.LocationResult;
import de.broccoli.approach.localization.util.FileUtils;
import de.broccoli.approach.localization.util.PrintUtils;
import de.broccoli.approach.localization.util.ScoreMatcher;
import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.models.Bug;
import de.ugoe.cs.smartshark.model.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Will calculate the TOP 1, 5, 10
 *
 */
public class EvaluationRunner {

    private static final int[] metrics = {1,5,10};

    private Logger logger = LoggerFactory.getLogger(EvaluationRunner.class.getName());
    private BroccoliLocalizationRunner runner;
    private String model;

    public EvaluationRunner(BroccoliLocalizationRunner runner, String model)
    {
        this.runner = runner;
        this.model = model;
    }

    public List<EvaluationResult> evaluateProjects(String... project)
    {
        List<EvaluationResult> results = new ArrayList<>();
        try {
            for (int i = 0; i < project.length; i++)
            {
                runner = new BroccoliLocalizationRunner();
                runner.init(project[i], model);
                EvaluationResult result = testRunner();
                results.add(result);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        // print result
        PrintUtils.printEvaluationResults(results, metrics);
        return results;
    }

    private EvaluationResult testRunner() throws Exception {
        List<Bug> issues = BroccoliContext.getInstance().getBugList();
        logger.info("Found " + issues.size() + " Issues to analyze");
        // Basic init the metrics
        int[] countsMetrics = new int[metrics.length];

        int procesed = 0;
        int issuesWithFiles = 0;
        for (Bug issue : issues) {
            // First we will get the real changed files from the database
            List<String> filenamesOfCommit = new ArrayList<>(issue.getSet());
            // Then we will remove all files that are not valid
            filenamesOfCommit = filenamesOfCommit.stream().filter(FileUtils::isValidFile).collect(Collectors.toList());
            // if we have more than zero files, we have to predict
            if (filenamesOfCommit.size() > 0) {
                // predict
                List<LocationResult> bugLocations = runner.locateBugForIssue(issue);
                // iterate over each metric
                for (int i = 0; i < metrics.length; i++) {
                    List<LocationResult> subset = bugLocations.subList(0, metrics[i]);
                    // check if *any* file, that is in the subset was really changed,
                    boolean match = false;
                    for (String fileName : filenamesOfCommit) {
                        match = ScoreMatcher.isContained(fileName, subset) || match;
                    }
                    // if we have a match, than we count the issue as a success!
                    if (match) {
                        countsMetrics[i]++;
                    }
                }
                // we update now the number of issuesWithFiles
                issuesWithFiles++;
            }
            // just a reminder
            procesed++;
            if (procesed % 10 == 0) {
                logger.info("Processed: " + procesed + "/" + issues.size());
            }
        }

        // Shutdown
        return new EvaluationResult(countsMetrics, issuesWithFiles, BroccoliContext.getInstance().getProjectName());
    }
}
