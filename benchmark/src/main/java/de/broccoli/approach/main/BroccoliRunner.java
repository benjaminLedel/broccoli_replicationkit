package de.broccoli.approach.main;

import de.broccoli.BLAlgorithm;
import de.broccoli.approach.localization.api.Runner;
import de.broccoli.context.BroccoliContext;
import org.reflections.Reflections;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BroccoliRunner implements BLAlgorithm {

    private String project;
    private final Set<Class<? extends Runner>> plugins;
    private Logger logger = Logger.getLogger(BroccoliRunner.class.getName());

    public BroccoliRunner()
    {
        this.project = BroccoliContext.getInstance().getProjectName();

        // Load provider

        // Load plugins
        Reflections reflections = new Reflections("de.broccoli.approach");
        plugins = reflections.getSubTypesOf(Runner.class);
    }

    public void run() {
        if(plugins.size() == 0)
        {
            logger.log(Level.WARNING, "No plugins are executable");
            return;
        }

        for (Class<? extends Runner> plugin : plugins) {
            try {
                Runner runner = plugin.newInstance();
                try {
                    runner.init(project, BroccoliContext.getInstance().getModel());
                    runner.run();
                    logger.log(Level.FINE, "Successful to executed Plugin " + runner.getClass());
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to execute Plugin " + runner.getClass() + ": " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
