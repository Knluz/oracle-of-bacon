package com.serli.oracle.of.bacon.repository;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.bson.Document;

import java.util.Optional;

public class MongoDbRepository {

    private final MongoClient mongoClient;

    public MongoDbRepository() {
        mongoClient = new MongoClient("localhost", 27017);
        // OU mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
    }

    public Optional<Document> getActorByName(String name) {
        DB database = mongoClient.getDB("workshop");
        DBCollection collection = database.getCollection("actors");

        DBObject query = new BasicDBObject("name", name);
        DBCursor cursor = collection.find(query);

        DBObject actor = cursor.one(); // if we're sure the name is unique, we can use cursor.one();

        return Optional.ofNullable(Document.parse(JSON.serialize(actor)));
    }
}
