package de.broccoli.approach.localization.util;

import de.broccoli.approach.localization.eval.EvaluationResult;
import de.broccoli.approach.localization.models.LocationResult;
import de.ugoe.cs.smartshark.model.Issue;

import java.util.ArrayList;
import java.util.List;

public class PrintUtils {

    public static void printLocationResults(List<LocationResult> resultList)
    {
        TableGenerator tableGenerator = new TableGenerator();

        List<String> headersList = new ArrayList<>();
        headersList.add("Filename");
        headersList.add("Score");

        List<List<String>> rowsList = new ArrayList<>();
        for (LocationResult result: resultList ) {
            List<String> row = new ArrayList<>();
            row.add(result.getDocument().getFileName(false));
            row.add(result.getClassifierScore().toString());
            rowsList.add(row);
        }

        System.out.println(tableGenerator.generateTable(headersList, rowsList));
    }

    public static void printLocationResults(List<LocationResult> resultList, int limit)
    {
        List<LocationResult> subList = resultList.subList(0, limit);
        printLocationResults(subList);
    }

    public static void printLocationResult(LocationResult result)
    {
        System.out.format("%32s%16s", result.getDocument(), result.getClassifierScore());
    }

    public static void printIssue(Issue issue) {
        TableGenerator tableGenerator = new TableGenerator();

        List<String> headersList = new ArrayList<>();
        headersList.add("Title");
        headersList.add(issue.getTitle());

        List<List<String>> rowsList = new ArrayList<>();

        List<String> row = new ArrayList<>();
        row.add("Description");
        row.add(issue.getDesc());
        rowsList.add(row);

        System.out.println(tableGenerator.generateTable(headersList, rowsList));
    }

    public static void printEvaluationResults(List<EvaluationResult> results, int[] metrics) {
        System.out.println("+++ PRINT RESULT +++");
        TableGenerator tableGenerator = new TableGenerator();

        List<String> headersList = new ArrayList<>();
        headersList.add("Project");
        headersList.add("# Issues");
        for (int i = 0; i < metrics.length; i++) {
            headersList.add("TOP " + metrics[i]);
        }

        List<List<String>> rowsList = new ArrayList<>();
        for (EvaluationResult result : results) {
            List<String> row = new ArrayList<>();
            row.add(result.getProject());
            row.add(String.valueOf(result.getIssuesWithFiles()));
            // METRICS
            for (int i = 0; i < metrics.length; i++) {
                row.add(String.valueOf((float) result.getCountsMetrics()[i] / (float) result.getIssuesWithFiles()));
            }
            rowsList.add(row);
        }

        System.out.println(tableGenerator.generateTable(headersList, rowsList));
    }
}
