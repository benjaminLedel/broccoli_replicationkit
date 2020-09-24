package org.locus.main;

import de.broccoli.BLAlgorithm;
import de.broccoli.context.BroccoliContext;
import org.locus.miningChanges.CorpusCreation;
import org.locus.miningChanges.ProduceChangeLevelResults;
import org.locus.miningChanges.ProduceFileLevelResults;
import org.locus.preprocess.ExtractCommits;
import org.locus.utils.ChangeLocator;

import java.io.File;


public class Core implements BLAlgorithm {



    public void run() {
        try {
            boolean flag = false;
            System.out.println("Using default configuration file");
            ChangeLocator.getInstance().clear();
            flag = Main.getInstance().loadConfigure();

            if (flag == true) {
                System.out.println("working with " + BroccoliContext.getInstance().getProjectName());
                ChangeLocator.getInstance().getShortChangeMap().clear();
                Core.startTask();
            } else
                System.err.println("Error!, stop program..");
        } catch (Exception exception)
        {
            exception.printStackTrace();
            System.err.println("Error!, stop program..");
        }

    }

    public static void startTask() throws Exception {
        //make outputFile path.
        ExtractCommits commits = new ExtractCommits();
        CorpusCreation corpusCreation = new CorpusCreation();
        File dir = new File(BroccoliContext.getInstance().getWorkDir());
        if (!dir.exists())
            dir.mkdirs();

        if (org.locus.main.Main.getInstance().task.equals("indexHunks")) {
            commits.indexHunks();
        }
        else if (Main.getInstance().task.equals("corpusCreation")) {
            // Create the corpus change logs, hunks, and create the code like term corpus
            corpusCreation.createCorpus();
        }
        else if (org.locus.main.Main.getInstance().task.equals("produceChangeResults")) {
            ProduceChangeLevelResults rank = new ProduceChangeLevelResults();
            rank.getFinalResults();
        }
        else if (org.locus.main.Main.getInstance().task.equals("produceFileResults")) {
            ProduceFileLevelResults rank = new ProduceFileLevelResults();
            rank.getFinalResults();
        }
        else if (org.locus.main.Main.getInstance().task.equals("all")) {
            commits.indexHunks();
            System.out.println("Finish Indexing Files");
            corpusCreation.createCorpus();
            System.out.println("Finish Creating Corpus");
            ProduceChangeLevelResults rank1 = new ProduceChangeLevelResults();
            rank1.getFinalResults();
            System.out.println("Finish Creating Change Level Results");
            ProduceFileLevelResults rank2 = new ProduceFileLevelResults();
            rank2.getFinalResults();
            System.out.println("Finish Creating File Level Results");
        }
        else if (Main.getInstance().task.equals("fileall")) {
            commits.indexHunks();
            System.out.println("Finish Indexing Files");
            if (!corpusCreation.createCorpus()){
                System.err.println("Failed to create corpus!");
                return;
            }
            else
                System.out.println("Finish Creating Corpus");
            ProduceFileLevelResults rank2 = new ProduceFileLevelResults();
            rank2.getFinalResults();
            System.out.println("Finish Creating File Level Results");
        }
    }
}
