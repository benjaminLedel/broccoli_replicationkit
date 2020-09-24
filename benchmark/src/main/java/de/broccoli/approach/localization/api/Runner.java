package de.broccoli.approach.localization.api;

import java.util.List;

public interface Runner {

    void init(String projectName, String modelName) throws Exception;
    void run() throws Exception;

    List<Approach> getApproaches();

    void prepare();
}
