package de.broccoli.approach.localization.api;

import de.broccoli.approach.localization.models.Document;
import de.broccoli.approach.localization.models.LocationResultList;
import de.broccoli.dataimporter.DataImporter;
import de.broccoli.dataimporter.models.Bug;

import java.util.List;

public interface Approach {

    String getApproachName();

    void buildCache(List<Document> files);
    void execute(Bug issue, List<Document> files, LocationResultList results);

    List<String> getApproachLabels();

    void shutdown();
}
