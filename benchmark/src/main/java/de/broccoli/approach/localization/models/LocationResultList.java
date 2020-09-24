package de.broccoli.approach.localization.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationResultList extends ArrayList<LocationResult> {

    Map<Document, LocationResult> fastSearchMap = new HashMap<>();

    public void addPoints(String approach, double points, Document file) {
        if(fastSearchMap.containsKey(file))
        {
            fastSearchMap.get(file).addScore(approach, points);
            return;
        }
        add(new LocationResult(approach, points, file));
    }

    public boolean add(LocationResult result)
    {
        fastSearchMap.put(result.getDocument(), result);
        return super.add(result);
    }

    public void addAll(List<Document> files) {
        for (Document file: files) {
            if(!fastSearchMap.containsKey(file))
            {
                add(new LocationResult("base", 0.0, file));
            }
        }
    }
}
