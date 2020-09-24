package de.broccoli.test.single;

import de.broccoli.approach.main.BroccoliRunner;
import de.broccoli.context.BroccoliContext;
import org.junit.Test;

public class TestBroccoliApproach extends TestBroccoliGitOptimalFramework {

    @Test
    public void testSmartShark()
    {
        BroccoliContext.getInstance().setAlgorithm("broccoli");
        BroccoliContext.getInstance().setContextVar("realistic", 1);
        run(BroccoliRunner.class, "smartshark");
    }

    @Test
    public void testXML()
    {
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
    public void testXMLOptimal()
    {
        BroccoliContext.getInstance().setAlgorithm("broccoli");
        BroccoliContext.getInstance().setContextVar("realistic", 1);
        run(BroccoliRunner.class, "xml");
    }

}
