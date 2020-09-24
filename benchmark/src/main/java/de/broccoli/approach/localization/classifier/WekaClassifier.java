package de.broccoli.approach.localization.classifier;

import de.broccoli.approach.localization.api.Approach;
import de.broccoli.approach.localization.models.LocationResult;
import de.broccoli.approach.localization.models.LocationResultList;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.*;
import weka.filters.supervised.instance.ClassBalancer;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WekaClassifier {

    private Classifier classifier;

    public WekaClassifier() {
    }

    public void trainCrossSingle(int folds, Instances instancesFromFile, String modelfile) throws Exception {
        classifier = createClassifier();

        Random rand = new Random(232);
        Instances randData = new Instances(instancesFromFile);
        randData.randomize(rand);
        if (randData.classAttribute().isNominal())
            randData.stratify(folds);

        // perform cross-validation
        Evaluation eval = new Evaluation(randData);
        for (int n = 0; n < folds; n++) {
            Instances train = randData.trainCV(folds, n, rand);
            Instances test = randData.testCV(folds, n);
            // the above code is used by the StratifiedRemoveFolds filter, the
            // code below by the Explorer/Experimenter:
            // Instances train = randData.trainCV(folds, n, rand);

            // build and evaluate classifier
            Classifier clsCopy = AbstractClassifier.makeCopy(classifier);
            clsCopy.buildClassifier(train);
            eval.evaluateModel(clsCopy, test);
        }

        System.out.println(eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false));
    }

    public void train(Instances instancesFromFile, String modelfile) throws Exception {
        classifier = createClassifier();
        classifier.buildClassifier(instancesFromFile);

        System.out.println(classifier);

        // serialize model
        ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(modelfile));
        oos.writeObject(classifier);
        oos.flush();
        oos.close();
    }

    public void showMetrics()
    {

    }

    /**
     * Will return a sublist of valid data
     *
     * @param data
     * @return
     */
    public List<LocationResult> predict(LocationResultList data, List<Approach> approaches) {
        if(classifier == null)
        {
            throw new IllegalStateException("No classifier loaded!");
        }
        ArrayList<Attribute> attributes = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (Approach approach: approaches) {
            for (String label: approach.getApproachLabels()) {
                if (!names.contains(label)) {
                    attributes.add(new Attribute(label));
                    names.add(label);
                }
            }
        }
        // Ergebnis spalte ist nun ein wert
        List<String> options = new ArrayList<>();
        options.add("0");
        options.add("1");
        attributes.add(new Attribute("Result",options));

        Instances dataset = new Instances("Dataset", attributes, data.size());
        dataset.setClassIndex(dataset.numAttributes() - 1);
        for (LocationResult result: data ) {
            DenseInstance instance = new DenseInstance(dataset.numAttributes());
            int i = 0;
            for (String label: names) {
                instance.setValue(attributes.get(i),result.getScore(label));
                i++;
            }
            dataset.add(instance);
        }

        try {
            int i = 0;
            for (Instance instance: dataset ) {
                double result = classifier.classifyInstance(instance);
                String prediction = instance.classAttribute().value((int)result);
                System.out.println("Prediction: " + prediction);
                data.get(i).setClassifierScore(result);
                i++;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return data;
    }

    public boolean loadModel(String model) {
        if(classifier == null) {
            try {
                classifier = (Classifier) weka.core.SerializationHelper.read(model);
            } catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public Classifier createClassifier() {
        FilteredClassifier classifier = new FilteredClassifier();
        classifier.setFilter(new ClassBalancer());
        classifier.setClassifier(new LinearRegression());
        return classifier;
    }
}
