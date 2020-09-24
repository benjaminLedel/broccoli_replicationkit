package de.broccoli.test.multi;

import de.broccoli.BLAlgorithm;
import de.broccoli.approach.main.BroccoliRunner;
import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.DataImporter;
import de.broccoli.dataimporter.smartshark.SmartSharkDataImporter;
import de.broccoli.dataimporter.xml.XMLDataImporter;
import de.broccoli.rating.MultiRatingCollector;
import org.amalgam.AmaLgam;
import org.apache.commons.io.FileUtils;
import org.blia.Core;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestMultiBroccoliFramework {

    private DataImporter dataImporter;
    private HashMap<String,Class> algorithmList;

    public TestMultiBroccoliFramework()
    {
        try {
            FileUtils.deleteDirectory(new File("tmp"));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        try {
         //   FileUtils.deleteDirectory(new File("workspace"));
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        // add different algs
        algorithmList = new HashMap<>();
        algorithmList.put("amalgam", AmaLgam.class);
        algorithmList.put("blia", Core.class);
        algorithmList.put("brtracer", org.brtracer.Core.class);
        algorithmList.put("buglocator", org.buglocator.Core.class);
        algorithmList.put("bluir", org.bluir.core.Core.class);
        algorithmList.put("locus", org.locus.main.Core.class);

        // my approach
        algorithmList.put("broccoli", BroccoliRunner.class);
    }

    protected void startupSmartShark() {
        // Creates a Broccoli Context
        dataImporter = new SmartSharkDataImporter("gora");
    }

    protected void startupXML() {
        File testBug = new File("example/AspectJ/bugrepo/repository.xml");
        File testSource = new File("example/AspectJ/sources/AspectJ_1_6_0_M2");
        File testGit = new File("example/AspectJ/gitrepo");

        dataImporter = new XMLDataImporter(testBug.getAbsolutePath(), testSource.getAbsolutePath(), testGit.getAbsolutePath() , "ASPECTJ");

    }

    protected void run(String[] algorithms, String mode)
    {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (String algorithm: algorithms) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                BroccoliContext.getInstance().setAlgorithm(algorithm);
                if(mode.equals("xml"))
                {
                    startupXML();
                } else {
                    startupSmartShark();
                }
                System.out.println("Hello from algorithm " + algorithm + ", Workspace " + BroccoliContext.getInstance().getWorkDir());
                Class clazz = algorithmList.get(algorithm);
                if(clazz == null)
                {
                    System.out.println("Algorithm " + algorithm + " not found!");
                    return;
                }
                try {
                    BLAlgorithm algorithmInstace = (BLAlgorithm) clazz.newInstance();
                    algorithmInstace.run();

                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                MultiRatingCollector.getInstance().calculateAdd();
            }
        });
        }
            executorService.shutdown();
        try {
            executorService.awaitTermination(100, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MultiRatingCollector.getInstance().writeToFile();
    }

    protected void shutdown() {
        //dataImporter.clearData();
    }

    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}
