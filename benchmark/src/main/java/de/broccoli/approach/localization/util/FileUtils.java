package de.broccoli.approach.localization.util;


import de.broccoli.approach.localization.models.Document;
import org.brtracer.sourcecode.ast.FileParser;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public static Document getDocumentOfFile(File toFile) {
        Document d = new Document();
        d.setPath(toFile.toString());
        d.setContent(new ArrayList<>());
        try(BufferedReader br = new BufferedReader(new FileReader(toFile))) {
            for(String line; (line = br.readLine()) != null; ) {
                // process the line.
                d.getContentList().add(line);
            }
            // line is not visible here.
        } catch (MalformedInputException e) {
            System.out.println("WARN:" + toFile.toString() + " " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FileParser parser = new FileParser(toFile);

            // make file full name.
            String fileName = parser.getPackageName();
            if (fileName.trim().equals("")) {
                fileName = toFile.getName();
            } else {
                fileName += "." + toFile.getName();
            }
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
            d.setJavaName(fileName);
        } catch (Exception e)
        {
            System.out.println("WARN:" + toFile.toString() + " " + e.getMessage());
            e.printStackTrace();
        }
        d.setContent(d.getContentList());
        return d;
    }

    public static boolean isValidFile(Path path) {
        String filename = path.getFileName().toString();
        return isValidFile(filename);
    }

    public static boolean isValidFile(String filename) {
        return filename.contains(".java"); // || filename.contains(".java");
    }

    public static Document applyRootPath(Path start, Document d) {
        d.setRoot(start.toString());
        return  d;
    }
}
