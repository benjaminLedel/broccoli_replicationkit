package de.broccoli.approach.localization.classifier;

import de.broccoli.approach.localization.BroccoliLocalizationRunner;
import de.broccoli.approach.localization.approaches.AbstractApproach;
import de.broccoli.approach.localization.models.Document;
import de.broccoli.approach.localization.models.LocationResult;
import de.broccoli.approach.localization.models.LocationResultList;
import de.broccoli.dataimporter.models.Bug;
import de.broccoli.utils.smartshark.SmartSHARKProjectDataProvider;
import de.ugoe.cs.smartshark.model.Issue;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class DataWriterReaderTest {

    private Logger logger = LoggerFactory.getLogger(DataWriterReaderTest.class.getName());
    private BroccoliLocalizationRunner runner = new BroccoliLocalizationRunner();

    @Test
    public void testSaveToCSV() throws Exception {
        DataReaderWriter dataWriterReader = new DataReaderWriter("test.csv");
        // Mock
        PrintContext context = dataWriterReader.startCSV(Collections.singletonList(new AbstractApproach() {
            @Override
            public String getApproachName() {
                return "test";
            }

            @Override
            public void buildCache(List<Document> files) {

            }

            @Override
            public void execute(Bug issue, List<Document> files, LocationResultList results) {

            }
        }));
        Assert.assertNotNull(context);

        LocationResultList list = new LocationResultList();
        list.add(new LocationResult(new Document()));

        Assert.assertTrue(dataWriterReader.appendToCSV("",list, Collections.singletonList(1),context));

        Assert.assertTrue(dataWriterReader.closeCSV(context));
        Assert.assertTrue(  org.apache.commons.io.FileUtils.deleteQuietly(new File("test.csv")));
    }

}
