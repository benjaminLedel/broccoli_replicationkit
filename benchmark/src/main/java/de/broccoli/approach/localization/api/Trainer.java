package de.broccoli.approach.localization.api;

import java.util.List;

public interface Trainer {

    void initForProject(List<String> projects);
    void train(String modelFile) throws Exception;
    void shutdown();
}
