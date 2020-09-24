package de.broccoli.utils.smartshark;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

public class SmartSHARKDatabaseProvider {

    private final Datastore datastore;
    private final MongoClient mongoClient;

    public SmartSHARKDatabaseProvider()
    {
        final Morphia morphia = new Morphia();
        morphia.mapPackage("de.ugoe.cs.smartshark.model");

        // Creating a connection to the database
        //
        MongoClientURI uri = new MongoClientURI("mongodb://bledel:Q6QTZHhF@mongoshark.informatik.uni-goettingen.de:27017/?authSource=smartshark");
        mongoClient = new MongoClient(uri);
        datastore = morphia.createDatastore(mongoClient, "smartshark");
    }

    public Datastore getDatastore() {
        return datastore;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }
}
