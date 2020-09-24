package de.broccoli.test.multi;

import org.junit.Test;

public class TestBasicParrallelBroccoli extends TestMultiBroccoliFramework {

    @Test
    public void testSmartShark() {
        run(new String[]{"amalgam","blia", "brtracer","buglocator","bluir","locus", "broccoli"}, "smartshark");

        shutdown();
    }

    @Test
    public void testXML()
    {
        run(new String[]{"amalgam","blia", "brtracer","buglocator","bluir","locus","broccoli"},"xml");

        shutdown();
    }

}
