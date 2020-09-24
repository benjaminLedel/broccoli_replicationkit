package de.broccoli.utils.smartshark;

import com.mongodb.MongoClient;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Common {

    public static String loadRepoFromMongoDB(String projectName, MongoClient client) throws IOException {
        GridFSBucket gridFSBucket = GridFSBuckets.create(client.getDatabase("smartshark"),"repository_data");
        File directory = new File("tmp");
        if (! directory.exists()){
            directory.mkdir();
        }
        File yourFile = new File("tmp/"+ projectName + ".tar.gz");
        yourFile.createNewFile();
        FileOutputStream streamToDownloadTo = new FileOutputStream(yourFile);
        gridFSBucket.downloadToStream(projectName + ".tar.gz", streamToDownloadTo);
        streamToDownloadTo.close();
        extractTarGZ(yourFile,"tmp");
        yourFile.delete();
        return "tmp/" + projectName;
    }

    public static void extractTarGZ(File archive, String out) throws IOException {
        File destination = new File(out);
        Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
        archiver.extract(archive, destination);
    }
}
