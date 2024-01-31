package fr.yewpyoc.mongodb;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import fr.yewpyoc.model.Article;
import fr.yewpyoc.redis.RedisUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MongoUtils {

    public static void printAllDocuments(MongoCollection<Document> collection) {
        System.out.println("--------------------");
        System.out.println("Affichage de tous les documents de la collection " + collection.getNamespace());
        FindIterable<Document> documents = collection.find();
        for (Document document : documents)
            System.out.println(document.toJson());
        System.out.println("--------------------");
    }

    public static List<Article> searchArticlesByName(MongoClient mongoClient, Jedis jedis, List<Article> articlesAlreadyFound, String articleName) {
        List<Article> foundArticles = new ArrayList<>();

        MongoDatabase yewpyocDatabase = mongoClient.getDatabase("yewpyoc");
        MongoCollection<Document> articlesCollection = yewpyocDatabase.getCollection("articles");

        Bson searchFilter = Filters.regex("articleName", articleName, "i"); // "i" pour une recherche insensible à la casse

        // Extraire les noms des articles déjà trouvés
        List<String> namesToExclude = articlesAlreadyFound.stream()
                .map(Article::getArticleName)
                .toList();

        // Créer le filtre $nin pour exclure les articles dont les noms sont dans la liste
        Bson exclusionFilter = Filters.nin("articleName", namesToExclude.toArray(new String[0]));

        // Combinez les filtres avec un filtre $and
        Bson finalFilter = Filters.and(searchFilter, exclusionFilter);

        // Exécuter la recherche
        try (MongoCursor<Document> cursor = articlesCollection.find(finalFilter).iterator()) {
            while (cursor.hasNext()) {
                Document articleDocument = cursor.next();

                // Convertir le Document MongoDB en objet Article
                Article article = convertDocumentToArticle(articleDocument);
                System.out.println("Article trouvé dans MongoDB : " + article);

                // Sauvegarder l'article dans Redis
                RedisUtils.saveArticlesToRedis(jedis, articleDocument);

                // Ajouter l'article à la liste des articles trouvés
                foundArticles.add(article);
            }
        }

        return foundArticles;

    }

    private static Article convertDocumentToArticle(Document document) {
        String articleName = document.getString("articleName");
        return new Article(articleName);
    }
}
