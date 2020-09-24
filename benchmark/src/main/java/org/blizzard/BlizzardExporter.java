package org.blizzard;

import de.broccoli.BLAlgorithm;
import de.broccoli.approach.localization.BroccoliLocalizationRunner;
import de.broccoli.approach.localization.models.Document;
import de.broccoli.approach.localization.models.LocationResult;
import de.broccoli.approach.localization.models.LocationResultList;
import de.broccoli.approach.localization.util.FileUtils;
import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.models.Bug;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class BlizzardExporter implements BLAlgorithm {

    private String folder = "C:\\Users\\blede\\Downloads\\BLIZZARD-Replication-Package-ESEC-FSE2018-master\\BLIZZARD-Replication-Package-ESEC-FSE2018-master\\";
    private HashMap<Document, String> map;

    @Override
    public void run() {
        List<Bug> bugs = BroccoliContext.getInstance().getBugList();
        map = new HashMap<>();
        FileWriter in = null;
        try {
            in = new FileWriter(folder + "sample-input\\" + BroccoliContext.getInstance().getProjectName() + ".txt", false);

            File folder2 = new File(folder + "BR-Raw\\" + BroccoliContext.getInstance().getProjectName());
            folder2.mkdirs();
            folder2 = new File(folder + "Goldset\\" + BroccoliContext.getInstance().getProjectName());
            folder2.mkdirs();

            List<Document> documents = createFileIndex();
            folder2 = new File(folder + "Corpus\\" + BroccoliContext.getInstance().getProjectName());
            folder2.mkdirs();
            int i = 0;
            for (Document d: documents) {
                String fileName = folder + "Corpus\\" + BroccoliContext.getInstance().getProjectName() + "\\" + i + ".java";
                FileWriter in2 = new FileWriter(fileName, false);
                in2.write(d.getContent());
                in2.close();
                map.put(d,fileName);
                i++;
            }

            for (Bug bug: bugs) {
                in.write(bug.getBugId() + "\n");
                FileWriter in2 = new FileWriter(folder + "BR-Raw\\" + BroccoliContext.getInstance().getProjectName() + "\\" + bug.getBugId() + ".txt", false);
                in2.write(bug.getBugSummary() + " " + bug.getBugDescription());
                in2.close();

                in2 = new FileWriter(folder + "Goldset\\" + BroccoliContext.getInstance().getProjectName() + "\\" + bug.getBugId() + ".txt", false);
                List<String> filenamesOfCommit = new ArrayList<>(bug.getSet());
                filenamesOfCommit = filenamesOfCommit.stream().filter(FileUtils::isValidFile).collect(Collectors.toList());
                if (filenamesOfCommit.size() > 0) {
                    for (Document bugLocation: documents) {
                        boolean match = filenamesOfCommit.stream().filter(s -> BlizzardExporter.isInResultSet(s, bugLocation)).count() > 0;
                        if(match)
                            in2.write(map.get(bugLocation).replace("\\", "/")+ "\n");
                    }
                }
                in2.close();
            }
            in.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private List<Document> createFileIndex() {
        Path start = Paths.get(BroccoliContext.getInstance().getSourceCodeDir());
        List<Document> documents = new ArrayList<>();
        Collection<File> files = org.apache.commons.io.FileUtils.listFiles(start.toFile(), new String[]{"java"}, true);
        for (File file : files) {
            if (FileUtils.isValidFile(file.getName())) {
                Document d = FileUtils.getDocumentOfFile(file);
                FileUtils.applyRootPath(start, d);
                documents.add(d);
            }
        }
//        try (Stream<Path> stream = Files.walk(start, Integer.MAX_VALUE)) {
//            return stream
//                    .filter(Files::isRegularFile)
//                    .filter(FileUtils::isValidFile)
//                    .map(FileUtils::getDocumentOfFile)
//                    .map(d -> FileUtils.applyRootPath(start,d))
//                    .collect(Collectors.toList());
//
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//        }
        return documents;
    }

    public static boolean isInResultSet(String filename, Document locationResult)
    {
        filename = filename.toLowerCase();
        String path = locationResult.getProjectPath().toLowerCase();
        String packageName = locationResult.getJavaName().toLowerCase();
        return filename.equals(path) || filename.equals(packageName) || filename.equals(packageName + ".java");
    }
}
