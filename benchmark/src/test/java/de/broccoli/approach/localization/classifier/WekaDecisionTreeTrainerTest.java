package de.broccoli.approach.localization.classifier;

import org.junit.Test;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomTree;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.supervised.instance.ClassBalancer;
import weka.filters.unsupervised.attribute.NumericToNominal;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;

public class WekaDecisionTreeTrainerTest {


    @Test
    public void testWekaGeneralDescionTree() throws Exception {
        // load data from CSV
        DataReaderWriter writer = new DataReaderWriter("wekadata.csv");
        //now we build and show the decision tree
        Classifier tree = createClassifier();
        Instances instances = writer.getInstancesFromFile();
        NumericToNominal numericToNominal = new NumericToNominal();
        String[] opts = new String[]{ "-R", "13"};
        numericToNominal.setOptions(opts);
        numericToNominal.setInputFormat(instances);
        Instances filterd = Filter.useFilter(instances,numericToNominal);
        tree.buildClassifier(filterd);

        System.out.println(tree);

        // serialize model
        ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("tree2.model"));
        oos.writeObject(tree);
        oos.flush();
        oos.close();
    }


    public Classifier createClassifier() {
        FilteredClassifier classifier = new FilteredClassifier();
        classifier.setFilter(new ClassBalancer());
        ///classifier.setClassifier(new J48());
        RandomTree tree = new RandomTree();
        tree.setKValue(2);
        return tree;
    }
}
