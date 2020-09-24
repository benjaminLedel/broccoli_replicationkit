package de.broccoli.approach.localization.classifier;

import de.broccoli.approach.localization.api.Approach;
import de.broccoli.approach.localization.models.LocationResult;
import de.broccoli.approach.localization.models.LocationResultList;
import de.broccoli.context.BroccoliContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataReaderWriter {

    private String file;

    public DataReaderWriter()
    {
        // random filename
        this.file = "model_data\\" + BroccoliContext.getInstance().getProjectName() + "_data.csv";
    }

    public DataReaderWriter(String path)
    {
        this.file = path;
    }

    public PrintContext startCSV(List<Approach> approaches) {
        // calculate header first (this is the obermenge)
        List<String> attributes = new ArrayList<>();
        for (Approach approach : approaches) {
            for(String label : approach.getApproachLabels()) {
                if (!attributes.contains(label)) {
                    attributes.add(label);
                }
            }
        }
        try {
            // write data
            FileWriter in = new FileWriter(this.file, false);
            CSVPrinter printer = CSVFormat.DEFAULT.print(in);
            List<String> headLines = new ArrayList<>(attributes);
            headLines.add("Result");
            printer.printRecord(headLines);

            return new PrintContext(in, printer, attributes);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    public boolean appendToCSV(String bugId, List<LocationResult> data, List<Integer> result, PrintContext context) {
        try {
            int i = 0;
            for (LocationResult dataEntry : data) {
                java.util.List<String> toPrint = new ArrayList<>();
                toPrint.add(BroccoliContext.getInstance().getProjectName());
                toPrint.add(bugId);
                for (String approach : context.getApproaches()) {
                    toPrint.add(String.valueOf(dataEntry.getScore(approach)));
                }
                toPrint.add(String.valueOf(result.get(i)));
                context.getPrinter().printRecord(toPrint);
                i++;
            }
        } catch (Exception e )
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean closeCSV(PrintContext context) {
        try {
            context.getIn().close();
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Instances getInstancesFromFile() throws IOException {
        CSVLoader loader = new CSVLoader();
        File file = new File(this.file);
        InputStream inputStream = new FileInputStream(file);
        loader.setSource(inputStream);
        Instances data = loader.getDataSet();

        data.setClassIndex(data.numAttributes()-1);

        return data;
    }

    public String getFile() {
        return file;
    }
}
