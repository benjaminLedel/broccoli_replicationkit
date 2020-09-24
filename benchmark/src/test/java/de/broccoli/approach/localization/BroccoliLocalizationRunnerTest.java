package de.broccoli.approach.localization;

import de.broccoli.approach.localization.eval.EvaluationResult;
import de.broccoli.approach.localization.eval.EvaluationRunner;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class BroccoliLocalizationRunnerTest {

    private Logger logger = LoggerFactory.getLogger(BroccoliLocalizationRunnerTest.class.getName());

    private BroccoliLocalizationRunner runner = new BroccoliLocalizationRunner();

    @Test
    public void testScore() throws Exception {
        EvaluationRunner evaluationRunner = new EvaluationRunner(runner, null);
        List<EvaluationResult> result = evaluationRunner.evaluateProjects("gora");
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testScoreWithClassifier() throws Exception {
        EvaluationRunner evaluationRunner = new EvaluationRunner(runner, "test.model");
        List<EvaluationResult> result = evaluationRunner.evaluateProjects("gora");
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
    }

}
