package de.broccoli.test.single;

import org.amalgam.AmaLgam;
import org.blizzard.BlizzardExporter;
import org.junit.Test;

public class TestBlizzardBroccoli extends TestBroccoliFramework {

    @Test
    public void testSmartShark()
    {
        startupSmartShark();
        // Performs iteration with the static context
        BlizzardExporter amaLgam = new BlizzardExporter();
        amaLgam.run();

        shutdown();
    }


    @Test
    public void testXML()
    {
        startupXML();
        // Performs iteration with the static context
        BlizzardExporter amaLgam = new BlizzardExporter();
        amaLgam.run();

        shutdown();
    }

}
