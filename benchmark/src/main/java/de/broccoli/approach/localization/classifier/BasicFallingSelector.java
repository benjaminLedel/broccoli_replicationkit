package de.broccoli.approach.localization.classifier;


import de.broccoli.approach.localization.models.LocationResult;

import java.util.List;

public class BasicFallingSelector {

    private int maxSize;

    public BasicFallingSelector(int maxSize)
    {
        this.maxSize = maxSize;
    }

    /**
     * Must be sorted!!!
     * @param list
     * @return
     */
    public List<LocationResult> select(List<LocationResult> list)
    {
        if(list.size() == 0 || list.size() == 1)
        {
            return list;
        }
        int iterations = Math.min(list.size(), maxSize+1);
        Double maxAbfall = 0.0D;
        int position = 0;
        for (int i = 1; i < iterations; i++)
        {
            LocationResult before = list.get(i-1);
            LocationResult actual = list.get(i);
            Double diff = Math.abs(before.getSimpleSum()-actual.getSimpleSum());
            if(maxAbfall <= diff)
            {
                maxAbfall = diff;
                position = i;
            }
        }
        return list.subList(0,position);
    }
}
