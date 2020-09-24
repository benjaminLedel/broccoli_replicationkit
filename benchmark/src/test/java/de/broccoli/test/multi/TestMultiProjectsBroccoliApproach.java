package de.broccoli.test.multi;

import de.broccoli.BLAlgorithm;
import de.broccoli.approach.localization.BroccoliLocalizationTrainer;
import de.broccoli.approach.localization.BroccoliLocalizationTrainerTest;
import de.broccoli.approach.localization.util.FileUtils;
import de.broccoli.approach.main.BroccoliRunner;
import de.broccoli.context.BroccoliContext;
import de.broccoli.rating.MultiRatingCollector;
import de.broccoli.test.single.TestBroccoliGitOptimalFramework;
import de.broccoli.utils.ProjectConfiguration;
import org.amalgam.AmaLgam;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.blia.Core;
import org.junit.Test;
import org.nd4j.linalg.io.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestMultiProjectsBroccoliApproach extends TestBroccoliGitOptimalFramework {

    private Logger logger = LoggerFactory.getLogger(TestMultiProjectsBroccoliApproach.class.getName());

    @Test
    public void testSmartShark() {
        BroccoliContext.getInstance().setAlgorithm("broccoli");
        BroccoliContext.getInstance().setContextVar("realistic", 1);
        run(BroccoliRunner.class, "smartshark");
    }

    @Test
    public void testXML() {
        BroccoliContext.getInstance().setAlgorithm("broccoli");
        BroccoliContext.getInstance().setContextVar("realistic", 1);
        BroccoliContext.getInstance().setModel("tree2.model");
        startupXML();
        // Performs iteration with the static context
        BroccoliRunner core = new BroccoliRunner();
        core.run();

        shutdown();
    }

    @Test
    public void trainMultiProjects() {
        String startAt = "smartshark_kylin_1.6.0";
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        boolean found = false;
        boolean modeSingle = true;
        List<ProjectConfiguration> configurations = TestFindProjectConfiguration.getConfigurationList();
        int i = 0;
        for (ProjectConfiguration configuration : configurations) {
            if (startAt.equals(configuration.getProject() + "_" + configuration.getVersion()) || startAt.equals(configuration.getProject()))
                found = true;
            if (!found)
                continue;
            if (modeSingle && !(startAt.equals(configuration.getProject() + "_" + configuration.getVersion()) || startAt.equals(configuration.getProject())))
                continue;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        BroccoliLocalizationTrainer trainer = new BroccoliLocalizationTrainer();
                        BroccoliContext.getInstance().setAlgorithm("broccoli_" + configuration.getProject() + "_" + configuration.getVersion());
                        BroccoliContext.getInstance().setContextVar("realistic", 0);
                        startupXML(configuration);

                        List<String> projects = Collections.singletonList(configuration.getProject() + "_" + configuration.getVersion());
                        logger.info("Starting init....");
                        trainer.initForProject(projects);
                        logger.info("Training starting....");
                        trainer.createCSVIndexFile();
                        logger.info("Finish" + BroccoliContext.getInstance().getProjectName());
                    } catch (Exception e) {
                        logger.error("Error:" + BroccoliContext.getInstance().getProjectName());
                        e.printStackTrace();
                    }
                }
            });
            i++;
            System.out.println("+++++++++++++++++");
            System.out.println(i + "/" + configurations.size() + " Projects done");
            System.out.println("+++++++++++++++++");
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(100, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void compress() throws IOException {
        File folder = new File("C:\\Users\\blede\\Documents\\smartshark\\broccoli_dataimporter\\model_data");
        File output = new File("C:\\Users\\blede\\Documents\\smartshark\\broccoli_dataimporter\\model_data_compress.csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(output, false));
        writer.write("project,bug,regex_file_matching,description_matching,java_search_match,dot_words_match,class_and_method_match,elastic_full,elastic_method,elastic_pfad,loc,similarReports,versionHistory,brTracer,Result");
        writer.newLine();
        for (File dataFile : folder.listFiles()) {
            System.out.println(dataFile.getAbsolutePath());
            String line = null;
            BufferedReader reader = new BufferedReader(new FileReader(dataFile));
            // First line ignore
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                if (StringUtils.countMatches(line, ",") != 14) {
                    System.out.println("error!");
                    return;
                }
                writer.write(line);
                writer.newLine();
            }
            reader.close();
        }
        writer.close();
    }

    @Test
    public void testOtherApproaches() throws IOException {
        //    add different algs
        List<String> attributes = new ArrayList<>();
        attributes.add("Project");
        attributes.add("Version");
        attributes.add("Bugs");
        FileWriter in = new FileWriter("statistic.csv", false);
        CSVPrinter printer = CSVFormat.DEFAULT.print(in);
        List<String> headLines = new ArrayList<>(attributes);
        headLines.add("Result");
        printer.printRecord(headLines);

        Map<String, Class> algorithmList = new HashMap<>();
        algorithmList.put("amalgam", AmaLgam.class);
        algorithmList.put("blia", Core.class);
        algorithmList.put("brtracer", org.brtracer.Core.class);
        algorithmList.put("buglocator", org.buglocator.Core.class);
        algorithmList.put("bluir", org.bluir.core.Core.class);
        algorithmList.put("locus", org.locus.main.Core.class);
        algorithmList.put("blizzard", org.blizzard.BlizzardExporter.class);
// "locus","blia", "amalgam",
        String[] algorithms = {"bluir"};

        String startAt = "Apache_HIVE_HIVE_0_10_0";
        //String startAt = "Apache_CAMEL_CAMEL_1_6_1";
        boolean found = true;
        boolean modeSingle = false;
        List<ProjectConfiguration> configurations = TestFindProjectConfiguration.getConfigurationList();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        int i = 0;
        for (ProjectConfiguration configuration : configurations) {
            if (startAt.equals(configuration.getProject() + "_" + configuration.getVersion()) || startAt.equals(configuration.getProject()))
                found = true;
            if (!found)
                continue;
            if (modeSingle && !startAt.equals(configuration.getProject() + "_" + configuration.getVersion()))
                continue;
            for (String algorithm : algorithms) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            BroccoliContext.getInstance().setAlgorithm(algorithm);
                            BroccoliContext.getInstance().setContextVar("realistic", 0);
                            BroccoliContext.getInstance().setSingleBug(configuration.getProject() + "_" + configuration.getVersion());
                            startupXML(configuration);

                            System.out.println("Hello from algorithm " + algorithm + ", Workspace " + BroccoliContext.getInstance().getWorkDir());
                            Class clazz = algorithmList.get(algorithm);
                            if (clazz != null) {
                                //System.out.println("Algorithm " + algorithm + " not found!");
                                java.util.List<String> toPrint = new ArrayList<>();
                                toPrint.add(configuration.getProject());
                                toPrint.add(configuration.getVersion());
                                toPrint.add(String.valueOf(BroccoliContext.getInstance().getBugList().size()));
                                printer.printRecord(toPrint);

                                //return;
                            }

                           BLAlgorithm algorithmInstace = (BLAlgorithm) clazz.newInstance();
                           algorithmInstace.run();

                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            MultiRatingCollector.getInstance().calculateAdd();
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        dataImporter.clearData();
                    }
                });
            }

            i++;
            System.out.println("+++++++++++++++++");
            System.out.println(i + "/" + configurations.size() + " Projects done");
            System.out.println("+++++++++++++++++");
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(100, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        printer.close();
    }


    @Test
    public void testOtherApproaches2() {
        //    add different algs
        Map<String, Class> algorithmList = new HashMap<>();
        algorithmList.put("amalgam", AmaLgam.class);
        algorithmList.put("blia", Core.class);
        algorithmList.put("brtracer", org.brtracer.Core.class);
        algorithmList.put("buglocator", org.buglocator.Core.class);
        algorithmList.put("bluir", org.bluir.core.Core.class);
        algorithmList.put("locus", org.locus.main.Core.class);

        String[] algorithms = {"brtracer","buglocator", "bluir"};

        String startAt = "Apache_HBASE_HBASE_0_98_5";
        boolean found = false;
        boolean modeSingle = false;
        List<ProjectConfiguration> configurations = TestFindProjectConfiguration.getConfigurationList();
        int i = 0;
        for (ProjectConfiguration configuration : configurations) {
            if (startAt.equals(configuration.getProject() + "_" + configuration.getVersion()))
                found = true;
            if (!found)
                continue;
            if (modeSingle && !startAt.equals(configuration.getProject() + "_" + configuration.getVersion()))
                continue;
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            for (String algorithm : algorithms) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            BroccoliContext.getInstance().setAlgorithm(algorithm);
                            BroccoliContext.getInstance().setContextVar("realistic", 0);
                            BroccoliContext.getInstance().setSingleBug(configuration.getProject() + "_" + configuration.getVersion());
                            startupXML(configuration);

                            System.out.println("Hello from algorithm " + algorithm + ", Workspace " + BroccoliContext.getInstance().getWorkDir());
                            Class clazz = algorithmList.get(algorithm);
                            if (clazz == null) {
                                System.out.println("Algorithm " + algorithm + " not found!");
                                // return;
                            }
                            BLAlgorithm algorithmInstace = (BLAlgorithm) clazz.newInstance();
                            algorithmInstace.run();

                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        try {
                            MultiRatingCollector.getInstance().calculateAdd();
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        dataImporter.clearData();
                    }
                });
            }
            executorService.shutdown();
            try {
                executorService.awaitTermination(100, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
            System.out.println("+++++++++++++++++");
            System.out.println(i + "/" + configurations.size() + " Projects done");
            System.out.println("+++++++++++++++++");
        }
    }
}
