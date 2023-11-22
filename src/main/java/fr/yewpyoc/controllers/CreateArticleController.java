package fr.yewpyoc.controllers;

import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import fr.yewpyoc.mongodb.MongoUtils;
import jakarta.annotation.PreDestroy;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CreateArticleController {

    private final MongoClient mongoClient;

    @Autowired
    public CreateArticleController(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @GetMapping("/create-article")
    public String createArticle() {
        MongoDatabase yewpyocDatabase = mongoClient.getDatabase("yewpyoc");
        MongoUtils.printAllDocuments(yewpyocDatabase.getCollection("articles"));
        return "create-article";
    }

    @PostMapping("/create-article")
    public String createArticle(@RequestParam String articleName, Model model) {
        System.out.println("Nom de l'article : " + articleName);

        MongoDatabase yewpyocDatabase = mongoClient.getDatabase("yewpyoc");
        MongoCollection<Document> articlesCollection = yewpyocDatabase.getCollection("articles");

        Document newCollection = new Document("articleName", articleName);
        articlesCollection.insertOne(newCollection);

        return "redirect:/create-article";
    }

    @PreDestroy
    public void closeMongoClient() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
