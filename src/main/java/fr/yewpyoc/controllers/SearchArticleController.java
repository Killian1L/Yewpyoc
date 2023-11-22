package fr.yewpyoc.controllers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import fr.yewpyoc.mongodb.MongoUtils;
import fr.yewpyoc.redis.RedisUtils;
import io.lettuce.core.api.StatefulRedisConnection;
import jakarta.annotation.PreDestroy;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchArticleController {

    private final MongoClient mongoClient;
    private final StatefulRedisConnection<String, String> redisConnection;

    @Autowired
    public SearchArticleController(MongoClient mongoClient, StatefulRedisConnection<String, String> redisConnection) {
        this.mongoClient = mongoClient;
        this.redisConnection = redisConnection;
    }

    @GetMapping("/search-article")
    public String createArticle() {
        return "search-article";
    }

    @PostMapping("/search-article")
    public String searchArticle(@RequestParam String articleName, Model model) {
        System.out.println("Nom de l'article : " + articleName);

        MongoDatabase yewpyocDatabase = mongoClient.getDatabase("yewpyoc");
        MongoCollection<Document> articlesCollection = yewpyocDatabase.getCollection("articles");

        Bson searchFilter = Filters.regex("articleName", articleName, "i"); // "i" pour une recherche insensible à la casse

        // Exécuter la recherche
        List<Document> articles = new ArrayList<>();
        try (MongoCursor<Document> cursor = articlesCollection.find(searchFilter).iterator()) {
            while (cursor.hasNext()) {
                Document article = cursor.next();
                System.out.println("Article trouvé : " + article.toJson());
                articles.add(article);
            }
        }

        model.addAttribute("searchResult", articles);

        RedisUtils.setValue(redisConnection, "articleName", articleName);

        System.out.println("Valeur de l'article enregistrée dans Redis : " + RedisUtils.getValue(redisConnection, "articleName"));

        return "redirect:/search-article";
    }

    @PreDestroy
    public void closeMongoClient() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
