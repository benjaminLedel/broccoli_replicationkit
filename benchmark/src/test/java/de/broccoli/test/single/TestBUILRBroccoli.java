package de.broccoli.test.single;

import org.bluir.core.Core;
import org.junit.Test;

public class TestBUILRBroccoli extends TestBroccoliFramework {

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
