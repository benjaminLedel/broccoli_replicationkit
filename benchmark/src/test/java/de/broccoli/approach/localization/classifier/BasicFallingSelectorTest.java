package de.broccoli.approach.localization.classifier;

import de.broccoli.approach.localization.models.LocationResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BasicFallingSelectorTest {


    @Test
    public void testFallingBasic() {
        BasicFallingSelector selector = new BasicFallingSelector(10);
        List<LocationResult> resultList = new ArrayList<>();
        for(int i = 0; i < 100; i++)
        {
            resultList.add(new LocationResult("test", i, null));
        }
        Assert.assertEquals(10, selector.select(resultList).size());
    }

    @Test
    public void testFallingBasic2() {
        BasicFallingSelector selector = new BasicFallingSelector(10);
        List<LocationResult> resultList = new ArrayList<>();
        for(int i = 0; i < 4; i++)
        {
            resultList.add(new LocationResult("test", i + 10, null));
        }
        for(int i = 0; i < 100; i++)
        {
            resultList.add(new LocationResult("test", 1, null));
        }
        Assert.assertEquals(4, selector.select(resultList).size());
    }

    @Test
    public void testFallingSmallList() {
        BasicFallingSelector selector = new BasicFallingSelector(10);
        List<LocationResult> resultList = new ArrayList<>();
        Assert.assertEquals(0, selector.select(resultList).size());
        resultList.add(new LocationResult("test", 1, null));
        Assert.assertEquals(1, selector.select(resultList).size());
    }
}
