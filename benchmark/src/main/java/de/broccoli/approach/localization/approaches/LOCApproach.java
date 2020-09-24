package de.broccoli.approach.localization.approaches;

import de.broccoli.approach.localization.models.Document;
import de.broccoli.approach.localization.models.LocationResultList;
import de.broccoli.dataimporter.models.Bug;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class LOCApproach extends AbstractApproach {

    private HashMap<Document, Double> map = new HashMap<>();

    @Override
    public String getApproachName() {
        return "loc";
    }

    @Override
    public void buildCache(List<Document> files) {
        int gesamt = files.size();
        List<Document> copy = new ArrayList<>(files);
        List<Document> sorted = copy.stream().sorted(new Comparator<Document>() {
            @Override
            public int compare(Document o1, Document o2) {
                return Integer.compare(o1.getContent().split("\n").length, o2.getContent().split("\n").length);
            }
        }).collect(Collectors.toList());
        int i = 0;
        for (Document d: sorted) {
            map.put(d,(double)i/(double)gesamt);
            i++;
        }
    }

    @Override
    public void execute(Bug issue, List<Document> files, LocationResultList results) {
        for (Document d: files) {
            results.addPoints("loc",map.get(d),d);
        }
    }
}
