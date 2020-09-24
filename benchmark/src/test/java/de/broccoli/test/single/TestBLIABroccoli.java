package de.broccoli.test.single;

import org.blia.Core;
import org.junit.Test;

public class TestBLIABroccoli extends TestBroccoliFramework {

    @Test
    public void testSmartShark()
    {
        startupSmartShark();
        // Performs iteration with the static context
        try {
            Core core = new Core();
            core.run();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        shutdown();
    }


    @Test
    public void testXML()
    {
        startupXML();
        // Performs iteration with the static context
        try {
            Core core = new Core();
            core.run();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        shutdown();
    }

}
