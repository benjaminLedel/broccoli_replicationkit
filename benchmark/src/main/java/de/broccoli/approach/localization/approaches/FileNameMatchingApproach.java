package de.broccoli.approach.localization.approaches;

import de.broccoli.approach.localization.models.Document;
import de.broccoli.approach.localization.models.LocationResultList;
import de.broccoli.approach.localization.util.PreProcessingUtils;
import de.broccoli.dataimporter.models.Bug;
import de.broccoli.utils.smartshark.SmartSHARKProjectDataProvider;
import de.ugoe.cs.smartshark.model.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class FileNameMatchingApproach extends AbstractApproach {

    private List<String> stopwords;
    private Logger logger = LoggerFactory.getLogger(FileNameMatchingApproach.class.getName());

    public static String LABEL_REGEX_FILE = "regex_file_matching";
    public static String LABEL_DESCRIPTION_MATCHING = "description_matching";


    public FileNameMatchingApproach() {
        stopwords = PreProcessingUtils.instance.getStopwords();
    }

    @Override
    public String getApproachName() {
        return "FileMatching";
    }

    @Override
    public void buildCache(List<Document> files) {

    }

    public void execute(Bug issue, List<Document> files, LocationResultList results) {
        // 1. Check the revision to the creation date of the issue
        String completeString = issue.getBugSummary() + " " + issue.getBugDescription();
        List<String> detectedFiles = PreProcessingUtils.instance.findFileNames(completeString);
        workListAndAddPoints(LABEL_REGEX_FILE, files, detectedFiles, results);

        List<String> wordsToCheck = PreProcessingUtils.instance.preProcessNaturalLanguage(completeString);
        workListAndAddPoints(LABEL_DESCRIPTION_MATCHING, files, wordsToCheck, results);
    }

    private void workListAndAddPoints(String label, List<Document> files, List<String> wordsToCheck, LocationResultList results) {
        for (Document file : files) {
            double points = 0.0D;
            for (String word : wordsToCheck) {
                if (file.getFileName(false).contains(word)) {
                    points++;
                }
            }
            if (points > 0) {
                results.addPoints(label, points, file);
            }
        }
    }

    @Override
    public List<String> getApproachLabels() {
        return Arrays.asList(LABEL_REGEX_FILE, LABEL_DESCRIPTION_MATCHING);
    }
}
