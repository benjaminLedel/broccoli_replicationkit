package de.broccoli.approach.localization.approaches;

import de.broccoli.approach.localization.models.Document;
import de.broccoli.approach.localization.models.LocationResultList;
import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.models.Bug;
import org.brtracer.bug.BugCorpusCreator;
import org.brtracer.bug.BugSimilarity;
import org.brtracer.bug.BugVector;
import org.brtracer.bug.SimilarityDistribution;
import org.brtracer.evaluation.Evaluation;
import org.brtracer.sourcecode.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class BRTracerApproach extends AbstractApproach {

    private String recommandedPath =  BroccoliContext.getInstance().getWorkDir() + BroccoliContext.getInstance().getSeparator()+ "recommended" +BroccoliContext.getInstance().getSeparator();
    private Logger logger = LoggerFactory.getLogger(BRTracerApproach.class.getName());

    @Override
    public String getApproachName() {
        return "brTracer";
    }

    @Override
    public void buildCache(List<Document> files) {
        try {
            logger.info("Create bug corpus");
            new BugCorpusCreator().create();
            logger.info("Create bug vector");
            new BugVector().create();
            logger.info("compute bug similarity...");
            new BugSimilarity().computeSimilarity();
            logger.info("create code corpus and segmented code corpus...");
            new CodeCorpusCreator().create();
            logger.info("compute SimiScore...");
            new SimilarityDistribution().distribute();
            logger.info("create index...");
            new Indexer().index();
            logger.info("create index origin...");
            new Indexer_OriginClass().index();
            logger.info("create vector...");
            new CodeVectorCreator().create();
            logger.info("compute VSMScore...");
            new Similarity().compute();
            logger.info("compute LengthScore...");
            new LenScore_OriginClass().computeLenScore();
            logger.info("count LoC...");
            new LineofCode().beginCount();
            logger.info("evaluate...");
            new Evaluation().evaluate(false);
            File f = new File(BroccoliContext.getInstance().getOutputFile());
            f.delete();
            logger.info("done...");
        } catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public void execute(Bug issue, List<Document> files, LocationResultList results) {
        try {
            Map<String, Document> mapper = new HashMap<>();
            Map<String, Document> mapper2 = new HashMap<>();
            for (Document d: files) {
                String name = d.getProjectPath();
                mapper.put(name.toLowerCase(),d);
                mapper2.put((d.getJavaName() + ".java").toLowerCase(),d);
            }
            BufferedReader reader = new BufferedReader(new FileReader(recommandedPath + issue.getBugId() + ".txt"));
            String line = null;
            Hashtable<String, Integer> table = new Hashtable<String, Integer>();
            while ((line = reader.readLine()) != null) {
                String[] elements = line.split("\t");
                Document d = mapper.get(elements[2].toLowerCase());
                    if(d != null)
                    {
                        results.addPoints("brTracer", Double.valueOf(elements[1]),d);
                    } else if(mapper2.get(elements[2].toLowerCase()) != null)
                    {

                        results.addPoints("brTracer", Double.valueOf(elements[1]),mapper2.get(elements[2].toLowerCase()));
                    }
            }
        } catch ( Exception e)
        {
            e.printStackTrace();
        }
    }
}
