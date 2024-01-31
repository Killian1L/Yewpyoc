package fr.yewpyoc.controllers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import fr.yewpyoc.redis.RedisUtils;
import jakarta.annotation.PreDestroy;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchArticleController {

    private final MongoClient mongoClient;
    private final Jedis jedis;
    @Autowired
    public SearchArticleController(MongoClient mongoClient, Jedis jedis) {
        this.mongoClient = mongoClient;
        this.jedis = jedis;
    }

    @GetMapping("/search-article")
    public String createArticle() {
        RedisUtils.afficherArticlesSurRedis(jedis);
        return "search-article";
    }

    @PostMapping("/search-article")
    public String searchArticle(@RequestParam String articleName, Model model) {
        System.out.println("Nom de l'article recherché : " + articleName);

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

        // Sauvegarder les articles dans Redis
        RedisUtils.saveArticlesToRedis(jedis, articles);

        model.addAttribute("searchResult", articles);

        return "redirect:/search-article";
    }

    @PreDestroy
    public void closeMongoClient() {
        if (mongoClient != null) {
            mongoClient.close();
        }
        if(jedis != null) {
            jedis.close();
        }
    }
}
