package de.broccoli.test.single;

import org.amalgam.AmaLgam;
import org.junit.Test;

public class TestAmalgamBroccoli extends TestBroccoliFramework {

    @Test
    public void testSmartShark()
    {
        startupSmartShark();
        // Performs iteration with the static context
        AmaLgam amaLgam = new AmaLgam();
        amaLgam.run();

        shutdown();
    }


    @Test
    public void testXML()
    {
        startupXML();
        // Performs iteration with the static context
        AmaLgam amaLgam = new AmaLgam();
        amaLgam.run();

        shutdown();
    }

}
