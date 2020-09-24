package de.broccoli.test.single;

import org.locus.main.Core;
import org.junit.Test;

public class TestLocusBroccoli extends TestBroccoliFramework {

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
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        System.out.println("heapmaxsize"+formatSize(heapMaxSize));

        startupXML();
        // Performs iteration with the static context
        Core core = new Core();
        core.run();

        shutdown();
    }

}
