package de.broccoli.approach.localization.util;


import de.broccoli.approach.localization.models.Document;
import de.broccoli.approach.localization.models.LocationResult;

import java.util.List;

public class ScoreMatcher {

    /**
     * Returns true, if the file is equally to the Document
     * @return
     */
    public static boolean isSameFile(String file, Document document)
    {
        String filename = document.getProjectPath().toLowerCase();
        return file != null && file.toLowerCase().equals(filename);
    }

    public static boolean isSameFile(String file, LocationResult result)
    {
        return isSameFile(file, result.getDocument());
    }

    public static boolean isContained(List<String> files, Document document)
    {
        return files.stream().anyMatch(s ->isSameFile(s, document));
    }

    public static boolean isContained(List<String> files, LocationResult document)
    {
        return files.stream().anyMatch(s ->isSameFile(s, document));
    }

    public static boolean isContained(String file, List<LocationResult> document)
    {
        return document.stream().anyMatch(d -> isSameFile(file, d));
    }
}
