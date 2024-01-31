package fr.yewpyoc.controllers;

import com.mongodb.client.MongoClient;
import fr.yewpyoc.model.Article;
import fr.yewpyoc.mongodb.MongoUtils;
import fr.yewpyoc.redis.RedisUtils;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import redis.clients.jedis.Jedis;

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
    public String searchArticle(Model model, HttpSession session) {
        // RedisUtils.afficherArticlesSurRedis(jedis);

        List<Article> searchResult = (List<Article>) session.getAttribute("searchResult");
        if(searchResult != null) {
            model.addAttribute("searchResult", searchResult);
            System.out.println("Résultat de la recherche : ");
            for(Article article : searchResult) {
                System.out.println("- " + article);
            }
        }

        return "search-article";
    }

    @PostMapping("/search-article")
    public String searchArticle(@RequestParam String articleName, Model model, HttpSession session) {
        System.out.println("Nom de l'article recherché : " + articleName);;

        // On recherche les articles dans Redis
        List<Article> articles = RedisUtils.searchArticlesByName(jedis, articleName);

        // On recherche les articles dans MongoDB
        articles.addAll(MongoUtils.searchArticlesByName(mongoClient, jedis, articles, articleName));

        session.setAttribute("searchResult", articles);
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
