package de.broccoli.approach.localization.classifier;

import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.util.List;

public class PrintContext {

    private FileWriter in;
    private CSVPrinter printer;
    private List<String> approaches;

    public PrintContext(FileWriter in, CSVPrinter printer, List<String> approaches) {
        this.in = in;
        this.printer = printer;
        this.approaches = approaches;
    }

    public FileWriter getIn() {
        return in;
    }

    public CSVPrinter getPrinter() {
        return printer;
    }

    public List<String> getApproaches() {
        return approaches;
    }
}
