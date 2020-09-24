package de.broccoli.test.single;

import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.DataImporter;
import de.broccoli.dataimporter.smartshark.SmartSharkDataImporter;
import de.broccoli.dataimporter.xml.XMLDataImporter;
import de.broccoli.rating.ClassicRating;
import de.broccoli.utils.ProjectConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class TestBroccoliFramework {

    protected DataImporter dataImporter;

    public TestBroccoliFramework()
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

    protected void startupXML(ProjectConfiguration configuration) {
        dataImporter = new XMLDataImporter(configuration.getBugRepo().getAbsolutePath(), configuration.getSources().getAbsolutePath(), configuration.getGitRepo().getAbsolutePath() , configuration.getProject() + "_" + configuration.getVersion());
    }

    protected void shutdown() {
        ClassicRating classicRating = new ClassicRating();
        classicRating.calcAndPrint();
        System.out.println("done. :) ");
        //dataImporter.clearData();
    }

    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}
