package de.broccoli.approach.localization;

import de.broccoli.context.BroccoliContext;
import de.broccoli.test.single.TestBroccoliGitOptimalFramework;
import org.junit.Test;
import org.nd4j.linalg.io.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;
import ml.dmlc.xgboost4j.java.DMatrix;

public class BroccoliLocalizationTrainerTest extends TestBroccoliGitOptimalFramework {

    private Logger logger = LoggerFactory.getLogger(BroccoliLocalizationTrainerTest.class.getName());
    private BroccoliLocalizationTrainer trainer = new BroccoliLocalizationTrainer();

    @Test
    public void simpleTrain() throws Exception {

        BroccoliContext.getInstance().setAlgorithm("broccoli");
        BroccoliContext.getInstance().setContextVar("realistic", 1);
        startupXML();

        List<String> projects = Collections.singletonList("gora");
        logger.info("Starting init....");
        trainer.initForProject(projects);
        logger.info("Training starting....");
        trainer.train("test.model");
        logger.info("Finish");
        // check if model is created
        Assert.notNull(new File("test.model"));
    }

    @Test
    public void simpleTrainCrossfalidated() throws Exception {
        List<String> projects = Collections.singletonList("gora");
        logger.info("Starting init....");
        trainer.initForProject(projects);
        logger.info("Training starting....");
        trainer.trainCross(10,"test2.model");
        logger.info("Finish");
        // check if model is created
        Assert.notNull(new File("test2.model"));
    }


   // @Test
   // public void xGBoostTrain()
   // {
   //     DMatrix dmat = new DMatrix("train.svm.txt");
   // }
}
