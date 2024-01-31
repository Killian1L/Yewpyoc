package fr.yewpyoc.redis;

import org.bson.Document;
import org.bson.types.ObjectId;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.List;

public class RedisUtils {

    private static final int TTL_SECONDS = 3600; // Durée de vie en secondes (1 heure)

    public static void saveArticlesToRedis(Jedis jedis, List<Document> articles) {
        for (Document article : articles) {
            String articleJson = article.toJson();
            System.out.println(articleJson);
            ObjectId articleId = article.getObjectId("_id");
            String articleKey = "article:" + articleId.toString();

            // Vérifier si la clé existe déjà dans Redis
            if (jedis.exists(articleKey)) {
                // Si la clé existe, actualiser le TTL à TTL_SECONDS
                jedis.expire(articleKey, TTL_SECONDS);
            } else {
                // Si la clé n'existe pas, sauvegarder l'article avec un nouveau TTL
                jedis.setex(articleKey, TTL_SECONDS, articleJson);
            }
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
}
