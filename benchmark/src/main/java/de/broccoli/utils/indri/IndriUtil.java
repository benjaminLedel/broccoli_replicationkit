package de.broccoli.utils.indri;

import de.broccoli.dataimporter.models.Bug;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class IndriUtil {

    public static void createParametersFile(String outputPath, String index, String corpus) throws IOException {

        BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));
        bw.write("<parameters>");
        bw.newLine();
        bw.write("<index>" + index +"</index>");
        bw.newLine();
        bw.write("<memory>2000M</memory>");
        bw.newLine();
        bw.write("<corpus>");
        bw.newLine();
        bw.write("<path>" + corpus +"</path>");
        bw.newLine();
        bw.write("<class>trectext</class>");
        bw.newLine();
        bw.write("</corpus>");
        bw.newLine();
        bw.write("<stemmer><name>Krovetz</name></stemmer>");
        bw.newLine();
        bw.write("<field>");
        bw.write("<name>class</name>");
        bw.write("<name>method</name>");
        bw.write("<name>identifier</name>");
        bw.write("<name>comments</name>");
        bw.write("</field>");
        bw.newLine();
        bw.write("</parameters>");


        bw.close();

    }
}
