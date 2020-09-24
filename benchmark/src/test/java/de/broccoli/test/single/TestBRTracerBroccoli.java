package de.broccoli.test.single;

import org.brtracer.Core;
import org.junit.Test;

public class TestBRTracerBroccoli extends TestBroccoliGitOptimalFramework {

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
        startupXML();
        // Performs iteration with the static context
        Core core = new Core();
        core.run();

        shutdown();
    }
}
