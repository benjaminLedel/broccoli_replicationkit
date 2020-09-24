package de.broccoli.approach.localization.approaches;

import de.broccoli.approach.localization.approaches.versionHistory.VersionHistoryContainer;
import de.broccoli.approach.localization.models.Document;
import de.broccoli.approach.localization.models.LocationResultList;
import de.broccoli.dataimporter.models.Bug;
import org.amalgam.AmaLgam;
import org.amalgam.analysis.BugReportParser;
import org.amalgam.analysis.CodeRepository;
import org.amalgam.analysis.VersionHistoryCalculator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionHistoryApproach extends AbstractApproach {

    private VersionHistoryCalculator historyCalc;
    private Map<String, List<VersionHistoryContainer>> dbToList;
    private final int days_back = 15;

    @Override
    public String getApproachName() {
        return "versionHistory";
    }

    @Override
    public void buildCache(List<Document> files) {
        try {
            AmaLgam.parseArgs();
            CodeRepository codeRepo = new CodeRepository();
            codeRepo.loadCommits();
            HashMap<String, org.amalgam.models.Bug> bugObjs = BugReportParser.loadBugReports();
            codeRepo.getCommitDateOfBugObj(bugObjs);
            historyCalc = new VersionHistoryCalculator(codeRepo.loadFileCommitHistory());
            for (org.amalgam.models.Bug bug : bugObjs.values()) {
                bug.historicalScores = historyCalc.computeBugSuspeciousScore(bug,  days_back, 0);
            }
            dbToList = historyCalc.storeScoresBroccoli(bugObjs, files);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(Bug issue, List<Document> files, LocationResultList results) {
        if(dbToList.get(issue.getBugId()) != null)
        {
            List<VersionHistoryContainer> documents = dbToList.get(issue.getBugId());
            for (VersionHistoryContainer d: documents) {
                results.addPoints("versionHistory",d.getScore(), d.getDocument());
            }
        }
    }


}
