package fr.yewpyoc.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.yewpyoc.model.Article;
import org.bson.Document;
import org.bson.types.ObjectId;
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

    public static void afficherArticlesSurRedis(Jedis jedis) {
        System.out.println("--------------------");
        System.out.println("Affichage de toutes les clés Redis : ");
        // Utiliser SCAN pour récupérer toutes les clés commençant par "article:"
        ScanParams scanParams = new ScanParams().match("article:*");
        String cursor = "0";

        do {
            ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
            List<String> keys = scanResult.getResult();

            for (String key : keys) {
                long ttl = jedis.ttl(key); // Temps restant avant expiration en secondes

                // Récupérer la valeur associée à la clé (l'article au format JSON)
                String articleJson = jedis.get(key);

                // Afficher les détails de l'article
                System.out.println("Clé : " + key);
                System.out.println("Article JSON : " + articleJson);
                System.out.println("Temps restant avant expiration : " + ttl + " secondes");
                System.out.println();
            }

            cursor = scanResult.getCursor();
        } while (!"0".equals(cursor));
        System.out.println("--------------------");
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
            //
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
