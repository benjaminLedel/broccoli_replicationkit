package de.broccoli.test.single;

import de.broccoli.BLAlgorithm;
import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.DataImporter;
import de.broccoli.dataimporter.models.Bug;
import de.broccoli.dataimporter.smartshark.SmartSharkDataImporter;
import de.broccoli.dataimporter.xml.XMLDataImporter;
import de.broccoli.rating.MultiRatingCollector;
import de.broccoli.utils.git.GitService;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestBroccoliGitOptimalFramework extends TestBroccoliFramework {


    protected void run(Class algorithm, String mode)
    {
        ExecutorService executorService = Executors.newFixedThreadPool(15);
        setupInfrastructure(mode);
        String algorithmName = BroccoliContext.getInstance().getAlgorithm();
        List<Bug> bugs = BroccoliContext.getInstance().getBugList();
        Map<String, Integer> contextVars = BroccoliContext.getInstance().getContextVars();
        for (Bug bug: bugs) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    BroccoliContext.getInstance().getContextVars().putAll(contextVars);
                    BroccoliContext.getInstance().setAlgorithm(algorithmName);
//                    BroccoliContext.getInstance().setSingleBug(bug);
                    setupInfrastructure(mode);
                    System.out.println("Hello from algorithm " + algorithm + ", Workspace " + BroccoliContext.getInstance().getWorkDir());
                    try {
                        BLAlgorithm algorithmInstace = (BLAlgorithm) algorithm.newInstance();
                        algorithmInstace.run();

                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    GitService.deleteFolder(new File(BroccoliContext.getInstance().getSourceCodeDir()));
                }
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(100, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setupInfrastructure(String mode)
    {
        if(mode.equals("xml"))
        {
            startupXML();
        } else {
            startupSmartShark();
        }
    }
}
