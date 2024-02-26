package fr.yewpyoc.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.yewpyoc.model.Article;
import org.bson.Document;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RedisUtils {

    private static final int TTL_SECONDS = 3600; // Durée de vie en secondes (1 heure)

    private static void resetTime(Jedis jedis, String articleKey) {
        jedis.expire(articleKey, TTL_SECONDS);
    }

    public static void saveArticlesToRedis(Jedis jedis, Document article) {

        String articleJson = article.toJson();

        String articleName = article.getString("articleName").toLowerCase();
        String articleKey = "article:" + articleName;

        // Vérifier si la clé existe déjà dans Redis
        if (jedis.exists(articleKey)) {
            // Si la clé existe, actualiser le TTL à TTL_SECONDS
            resetTime(jedis, articleKey);
        } else {
            // Si la clé n'existe pas, sauvegarder l'article avec un nouveau TTL
            jedis.setex(articleKey, TTL_SECONDS, articleJson);
        }
    }

    public static List<Article> searchArticlesByName(Jedis jedis, String name) {
        List<Article> result = new ArrayList<>();

        // Utiliser SCAN pour récupérer toutes les clés commençant par "article:"
        ScanParams scanParams = new ScanParams().match("article:*" + name.toLowerCase() + "*");
        String cursor = "0";

        try {
            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                List<String> keys = scanResult.getResult();

                for (String key : keys) {
                    // Récupérer la valeur associée à la clé (l'article au format JSON)
                    String articleJson = jedis.get(key);

                    // Convertir la chaîne JSON en objet Article
                    Article article = convertJsonToArticle(articleJson);
                    System.out.println("Article trouvé REDIS : " + articleJson);

                    result.add(article);
                    resetTime(jedis, key);
                }

                cursor = scanResult.getCursor();
            } while (!"0".equals(cursor));
        } catch (IOException e) {
            System.out.println("Une erreur est survenue lors de la conversion de l'article JSON en objet Article : " + e.getMessage());
        }

        return result;
    }

    private static Article convertJsonToArticle(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Document document = objectMapper.readValue(json, Document.class);

        if (document.containsKey("articleName")) {
            String articleName = document.getString("articleName");
            return new Article(articleName);
        }

        return null;
    }
}
