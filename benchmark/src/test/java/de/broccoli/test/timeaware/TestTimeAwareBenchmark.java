package de.broccoli.test.timeaware;

import de.broccoli.approach.localization.BroccoliLocalizationRunner;
import de.broccoli.approach.localization.BroccoliLocalizationTrainer;
import de.broccoli.approach.localization.models.Document;
import de.broccoli.approach.localization.models.LocationResultList;
import de.broccoli.approach.localization.util.FileUtils;
import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.models.Bug;
import de.broccoli.test.multi.TestFindProjectConfiguration;
import de.broccoli.test.single.TestBroccoliGitOptimalFramework;
import de.broccoli.utils.ProjectConfiguration;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class TestTimeAwareBenchmark extends TestBroccoliGitOptimalFramework {

    /**
     * Computes the basic score, based on the
     */
    @Test
    public void testDefaultScore() {
        String startAt = "smartshark_kylin_1.6.0";
        boolean found = true;
        boolean modeSingle = false;
        List<Integer> filesTotal = new ArrayList<>();
        List<Integer> filePossible = new ArrayList<>();
        List<Integer> bugReports = new ArrayList<>();
        List<Integer> bugReportsWithoutASingleFile = new ArrayList<>();

        List<ProjectConfiguration> configurations = TestFindProjectConfiguration.getConfigurationList();
        int done = 0;
        for (ProjectConfiguration configuration : configurations) {
            if (startAt.equals(configuration.getProject() + "_" + configuration.getVersion()) || startAt.equals(configuration.getProject()))
                found = true;
            if (!found)
                continue;
            if (modeSingle && !(startAt.equals(configuration.getProject() + "_" + configuration.getVersion()) || startAt.equals(configuration.getProject())))
                continue;
            startupXML(configuration);

            int filesTotalProject = 0;
            int filesPossibleProject = 0;
            int bugReportsWithoutASingleFileProject = 0;
            BroccoliContext context = BroccoliContext.getInstance();
            List<Bug> bugs =  context.getBugList();
            bugReports.add(bugs.size());
            LocationResultList fileIndex = createFileIndex();
            for (Bug bug: bugs) {
                filesTotalProject += bug.getSet().size();
                boolean findSingle = false;
                for (String file: bug.getSet()) {
                    if(BroccoliLocalizationRunner.isInResultSet(file, fileIndex))
                    {
                        findSingle = true;
                        filesPossibleProject++;
                    }
                }
                if(!findSingle)
                {
                    bugReportsWithoutASingleFileProject++;
                }
            }
            filesTotal.add(filesTotalProject);
            filePossible.add(filesPossibleProject);
            bugReportsWithoutASingleFile.add(bugReportsWithoutASingleFileProject);
            done++;
            System.out.println("(" + done + "/" + configurations.size() +")");
        }

        int total = filesTotal.stream().mapToInt(value -> value).sum();
        int possible = filePossible.stream().mapToInt(value -> value).sum();

        System.out.println("Total: " + total);
        System.out.println("Possible: " + possible);
        System.out.println("Percent: " + (float)possible/(float)total);


        int totalBugReports = bugReports.stream().mapToInt(value -> value).sum();
        int notPossibleBugReport = bugReportsWithoutASingleFile.stream().mapToInt(value -> value).sum();

        System.out.println("Total: " + totalBugReports);
        System.out.println("Possible: " + notPossibleBugReport);
        System.out.println("Percent: " + (float)notPossibleBugReport/(float)totalBugReports);
    }

    @Test
    public void testTimeAwareScore() throws IOException {
        String startAt = "smartshark_ant-ivy";
        boolean found = false;
        boolean modeSingle = false;
        List<Integer> filesTotal = new ArrayList<>();
        List<Integer> filePossible = new ArrayList<>();
        List<Integer> bugReports = new ArrayList<>();
        List<Integer> bugReportsWithoutASingleFile = new ArrayList<>();

        List<ProjectConfiguration> configurations = TestFindProjectConfiguration.getConfigurationList();
        int done = 0;
        List<LocationResultList> filesIndexes = new ArrayList<>();
        Map<LocationResultList, ProjectConfiguration> configurationMap = new HashMap<>();
        String lastProject = "";
        Map<String, List<String>> bugToVersion = new HashMap<>();
        for (ProjectConfiguration configuration : configurations) {
            if (startAt.equals(configuration.getProject() + "_" + configuration.getVersion()) || startAt.equals(configuration.getProject()))
                found = true;
            if (!found)
                continue;
            if (modeSingle && !(startAt.equals(configuration.getProject() + "_" + configuration.getVersion()) || startAt.equals(configuration.getProject())))
                continue;
            startupXML(configuration);
            if(!lastProject.equals(configuration.getProject()))
            {
                filesIndexes.clear();
                if(!lastProject.equals("")) {
                    FileWriter in = new FileWriter("timeaware/" + lastProject + ".csv", false);
                    CSVPrinter printer = CSVFormat.DEFAULT.print(in);
                    for (String key : bugToVersion.keySet()) {
                        java.util.List<String> toPrint = new ArrayList<>();
                        toPrint.add(key);
                        List<String> versions =  bugToVersion.get(key).stream().distinct().collect(Collectors.toList());
                        for (String version : versions) {
                            toPrint.add(version);
                        }
                        printer.printRecord(toPrint);
                    }
                    printer.close();
                }
                bugToVersion.clear();
                configurationMap.clear();
            }
            if(filesIndexes.size() > 20)
            {
                filesIndexes.remove(20);
            }
            lastProject = configuration.getProject();

            int filesTotalProject = 0;
            int filesPossibleProject = 0;
            int bugReportsWithoutASingleFileProject = 0;
            BroccoliContext context = BroccoliContext.getInstance();
            List<Bug> bugs =  context.getBugList();
            bugReports.add(bugs.size());
            LocationResultList fileIndex = createFileIndex();
            configurationMap.put(fileIndex,configuration);
            filesIndexes.add(fileIndex);
            for (Bug bug: bugs) {
                filesTotalProject += bug.getSet().size();
                boolean findSingle = false;
                // base
                    for (String file : bug.getSet()) {
                        boolean foundFile = false;
                        int index = 0;
                        while (!foundFile && index < filesIndexes.size()) {
                            LocationResultList currentIndex = filesIndexes.get(filesIndexes.size() - 1 - index);
                            if (BroccoliLocalizationRunner.isInResultSet(file, currentIndex)) {
                                ProjectConfiguration configActual = configurationMap.get(currentIndex);
                                findSingle = true;
                                foundFile = true;
                                if(!bugToVersion.containsKey(configuration.getVersion() + "_" + bug.getBugId()))
                                    bugToVersion.put(configuration.getVersion() + "_" + bug.getBugId(), new ArrayList<String>());
                                bugToVersion.get(configuration.getVersion() + "_" + bug.getBugId()).add(configActual.getVersion());
                                filesPossibleProject++;
                        }

                            index++;
                    }
                }
                if(!findSingle)
                {
                    bugReportsWithoutASingleFileProject++;
                }
            }
            filesTotal.add(filesTotalProject);
            filePossible.add(filesPossibleProject);
            bugReportsWithoutASingleFile.add(bugReportsWithoutASingleFileProject);
            done++;
            System.out.println("(" + done + "/" + configurations.size() +")");
        }

        if(!lastProject.equals("")) {
            FileWriter in = new FileWriter("timeaware/" + lastProject + ".csv", false);
            CSVPrinter printer = CSVFormat.DEFAULT.print(in);
            for (String key : bugToVersion.keySet()) {
                java.util.List<String> toPrint = new ArrayList<>();
                toPrint.add(key);
                List<String> versions =  bugToVersion.get(key).stream().distinct().collect(Collectors.toList());
                for (String version : versions) {
                    toPrint.add(version);
                }
                printer.printRecord(toPrint);
            }
            printer.close();
        }

        int total = filesTotal.stream().mapToInt(value -> value).sum();
        int possible = filePossible.stream().mapToInt(value -> value).sum();

        System.out.println("Total: " + total);
        System.out.println("Possible: " + possible);
        System.out.println("Percent: " + (float)possible/(float)total);


        int totalBugReports = bugReports.stream().mapToInt(value -> value).sum();
        int notPossibleBugReport = bugReportsWithoutASingleFile.stream().mapToInt(value -> value).sum();

        System.out.println("Total: " + totalBugReports);
        System.out.println("Possible: " + notPossibleBugReport);
        System.out.println("Percent: " + (float)notPossibleBugReport/(float)totalBugReports);
    }

    private LocationResultList createFileIndex() {
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

        LocationResultList results = new LocationResultList();
        // add all files to the resultset, since it is possible, that a approach does not rate a file, so we add here the fileexenstion
        results.addAll(documents);
        return results;
    }

    @Test
    public void testBroccoliScore() throws IOException {
        String startAt = "smartshark_nutch";
        boolean found = false;
        boolean modeSingle = false;

        List<ProjectConfiguration> configurations = TestFindProjectConfiguration.getConfigurationList();
        // key Project, Key Version -> Configuration
        Map<String, Map<String, ProjectConfiguration>> projectToVersionToConfiguration = new HashMap<>();
        for (ProjectConfiguration configuration : configurations) {
            if (startAt.equals(configuration.getProject() + "_" + configuration.getVersion()) || startAt.equals(configuration.getProject()))
                found = true;
            if (!found)
                continue;
            if (modeSingle && !(startAt.equals(configuration.getProject() + "_" + configuration.getVersion()) || startAt.equals(configuration.getProject())))
                continue;
            if(!projectToVersionToConfiguration.containsKey(configuration.getProject()))
                projectToVersionToConfiguration.put(configuration.getProject(), new HashMap<>());
            projectToVersionToConfiguration.get(configuration.getProject()).put(configuration.getVersion(), configuration);
        }

        for (String project: projectToVersionToConfiguration.keySet().stream().sorted((p1, p2) -> p1.compareTo(p2)).collect(Collectors.toList())) {
            System.out.println("project " + project);
            Map<String, List<String>> bugToVersion = new HashMap<>();

            FileReader in = new FileReader("timeaware/" + project + ".csv");
            CSVParser printer = CSVFormat.DEFAULT.parse(in);
            for (CSVRecord record: printer) {
                bugToVersion.put(record.get(0), new ArrayList<>());
                for(int i = 1; i < record.size(); i++)
                {
                    bugToVersion.get(record.get(0)).add(record.get(i));
                }
            }

            List<Bug> bugsAllOfProject = new ArrayList<>();
            int i = 1;
            for (String version: projectToVersionToConfiguration.get(project).keySet()) {
                System.out.println("Build file index " + (i) + " of " + projectToVersionToConfiguration.get(project).keySet().size());
                ProjectConfiguration configuration = projectToVersionToConfiguration.get(project).get(version);
                startupXML(configuration);

                List<Bug> bugs = BroccoliContext.getInstance().getBugList();
                for (Bug bug: bugs) {
                    String key = configuration.getVersion() + "_" + bug.getBugId();
                    if(bugToVersion.containsKey(key)) {
                        bug.setBugId(i + "0000" + bug.getBugId());
                        bug.setVersions(bugToVersion.get(key));
                        bugsAllOfProject.add(bug);
                    } else {
                        System.out.println(key);
                    }
                }
                i++;
            }

            for (String version: projectToVersionToConfiguration.get(project).keySet()) {
                ProjectConfiguration configuration = projectToVersionToConfiguration.get(project).get(version);
                BroccoliLocalizationTrainer trainer = new BroccoliLocalizationTrainer();
                BroccoliContext.getInstance().setAlgorithm("broccoli_" + configuration.getProject() + "_" + configuration.getVersion());
                BroccoliContext.getInstance().setContextVar("realistic", 0);
                startupXML(configuration);

                // Modifi the bug list
                System.out.println("size before" + BroccoliContext.getInstance().getBugList().size());
                BroccoliContext.getInstance().getBugList().clear();
                for (Bug bug: bugsAllOfProject) {
                    if(bug.getVersions().contains(configuration.getVersion()))
                    {
                        BroccoliContext.getInstance().getBugList().add(bug);
                    }
                }
                System.out.println("size after" + BroccoliContext.getInstance().getBugList().size());

                List<String> projects = Collections.singletonList(configuration.getProject() + "_" + configuration.getVersion());
                System.out.println("Starting init....");
                trainer.initForProject(projects);
                System.out.println("Training starting....");
                trainer.createCSVIndexFile();
                System.out.println("Finish" + BroccoliContext.getInstance().getProjectName());
            }
        }
    }
}
