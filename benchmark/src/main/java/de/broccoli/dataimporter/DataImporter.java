package de.broccoli.dataimporter;

import de.broccoli.dataimporter.models.Bug;

import java.util.List;

public interface DataImporter {

    List<Bug> getBugs();

    void clearData();
}
