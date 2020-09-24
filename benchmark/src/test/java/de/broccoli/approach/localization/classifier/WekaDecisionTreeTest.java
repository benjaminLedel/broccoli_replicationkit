package de.broccoli.approach.localization.classifier;

import org.junit.Test;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.InputStream;


public class WekaDecisionTreeTest {

    @Test
    public void testWekaGeneralDescionTree() throws Exception {
        // load data from CSV
        CSVLoader loader = new CSVLoader();
        InputStream inputStream = WekaDecisionTreeTest.class.getResourceAsStream("/wekadata.csv");
        loader.setSource(inputStream);
        Instances data = loader.getDataSet();

        data.setClassIndex(data.numAttributes()-1);

        //now we build and show the decision tree
        J48 tree = new J48();
        tree.buildClassifier(data);

        System.out.println(tree);
    }

    @Test
    public void testWekaGeneralLinear() throws Exception {
        // load data from CSV
        CSVLoader loader = new CSVLoader();
        InputStream inputStream = WekaDecisionTreeTest.class.getResourceAsStream("/linearWeather.csv");
        loader.setSource(inputStream);
        Instances data = loader.getDataSet();

        data.setClassIndex(data.numAttributes()-1);

        //now we build and show the decision tree
        LinearRegression regression = new LinearRegression();
        regression.buildClassifier(data);

        System.out.println(regression);
    }
}
