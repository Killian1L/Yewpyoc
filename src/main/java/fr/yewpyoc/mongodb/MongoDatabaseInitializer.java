package fr.yewpyoc.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MongoDatabaseInitializer {

    private final MongoClient mongoClient;

    @Autowired
    public MongoDatabaseInitializer(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @PostConstruct
    public void init() {
        MongoDatabase yewpyocDatabase = mongoClient.getDatabase("yewpyoc");

        yewpyocDatabase.getCollection("articles").drop();

        createArticle(yewpyocDatabase);
    }

    private void createArticle(MongoDatabase yewpyocDatabase) {
        MongoCollection<Document> articlesCollection = yewpyocDatabase.getCollection("articles");

        Document testArticle = new Document("articleName", "Test Article");
        articlesCollection.insertOne(testArticle);
    }
}
