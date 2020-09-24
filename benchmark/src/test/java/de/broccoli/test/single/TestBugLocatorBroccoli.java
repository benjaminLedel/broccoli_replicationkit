package de.broccoli.test.single;

import de.broccoli.context.BroccoliContext;
import org.buglocator.Core;
import org.junit.Test;

public class TestBugLocatorBroccoli extends TestBroccoliGitOptimalFramework {

    @Test
    public void testSmartShark()
    {
        startupSmartShark();
        // Performs iteration with the static context
        Core core = new Core();
        core.run();

        shutdown();
    }


    @Test
    public void testXML()
    {
        BroccoliContext.getInstance().setAlgorithm("buglocator");
        startupXML();
        // Performs iteration with the static context
        Core core = new Core();
        core.run();

        shutdown();
    }

    @Test
    public void testXMLOptimal()
    {
        BroccoliContext.getInstance().setAlgorithm("buglocator");
        run(Core.class, "xml");
    }
}
