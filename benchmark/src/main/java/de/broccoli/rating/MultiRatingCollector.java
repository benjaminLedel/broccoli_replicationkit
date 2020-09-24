package de.broccoli.rating;

import de.broccoli.context.BroccoliContext;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class MultiRatingCollector {

    private static ThreadLocal<MultiRatingCollector> threadLocal = new ThreadLocal<MultiRatingCollector>();
    private HashMap<String, AlgorithmResult> results = new HashMap<>();

    private static MultiRatingCollector instance = new MultiRatingCollector();

    public static MultiRatingCollector getInstance() {
        if(threadLocal.get() == null)
        {
            threadLocal.set(new MultiRatingCollector());
        }
        return threadLocal.get();
    }

    public synchronized void calculateAdd()
    {
        ClassicRating rating = new ClassicRating();
        results.put(BroccoliContext.getInstance().getAlgorithm(),rating.calc());
    }

    public void writeToFile()
    {

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
            for (String algorithm: results.keySet()) {
                AlgorithmResult result = results.get(algorithm);
                writer.write(algorithm + "\t" + result.getProject() + "\t" + result.getTop1() + "\t" + result.getTop5() + "\t" + result.getTop10() + "\t" + result.getMap() + "\t" + result.getMrr() + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
