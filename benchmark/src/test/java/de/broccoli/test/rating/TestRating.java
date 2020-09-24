package de.broccoli.test.rating;

import de.broccoli.context.BroccoliContext;
import de.broccoli.rating.ClassicRating;
import de.broccoli.test.single.TestBroccoliFramework;
import org.junit.Test;

public class TestRating extends TestBroccoliFramework {

    @Test
    public void testXML()
    {
        String[] algorithms = new String[]{"amalgam","blia", "brtracer","buglocator","bluir","locus"};
        for (String algorithm: algorithms) {

        BroccoliContext.getInstance().setAlgorithm(algorithm);
        startupXML();
        // Performs iteration with the static context
        ClassicRating rating = new ClassicRating();
        rating.calcAndPrint();
        }

    }

    @Test
    public void testXML2()
    {
        String[] algorithms = new String[]{"amalgam","blia"};
        for (String algorithm: algorithms) {

            BroccoliContext.getInstance().setAlgorithm(algorithm);
            startupXML();
            // Performs iteration with the static context
            ClassicRating rating = new ClassicRating();
            rating.calcAndPrint();

        }
    }
}
