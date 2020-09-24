package org.blia;

import de.broccoli.BLAlgorithm;
import org.blia.blia.analysis.BLIA;
import org.blia.db.dao.BaseDAO;
import org.blia.evaluation.Evaluator;

public class Core implements BLAlgorithm {

    private Property prop;

    public Core()
    {

    }

    public void run() {

        try {
            prop = Property.loadInstance();
            //preparing BLIA working folder
            BLIA blia = new BLIA();

            // initialize DB and create all tables.
            BLP.initializeDB();

            // Run BLIA algorithm
            blia.run();

            String algorithmDescription = "[BLIA] alpha: " + prop.alpha
                    + ", beta: " + prop.beta + ", pastDays: "
                    + prop.pastDays + ", cadidateLimitRate: "
                    + prop.candidateLimitRate;

            // Evaluate the accuracy result of BLIA
            Evaluator evaluator = new Evaluator(prop.productName,
                    Evaluator.ALG_BLIA, algorithmDescription, prop.alpha,
                    prop.beta, prop.pastDays,
                    prop.candidateLimitRate);
            evaluator.evaluate();
            BaseDAO.closeConnection();
            Property.clear();
        } catch (Exception exeception)
        {
            exeception.printStackTrace();
        }
    }
}
