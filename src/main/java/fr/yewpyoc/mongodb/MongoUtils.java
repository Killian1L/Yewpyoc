package fr.yewpyoc.mongodb;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class MongoUtils {

    public static void printAllDocuments(MongoCollection<Document> collection) {
        System.out.println("--------------------");
        System.out.println("Affichage de tous les documents de la collection " + collection.getNamespace());
        FindIterable<Document> documents = collection.find();
        for (Document document : documents)
            System.out.println(document.toJson());
        System.out.println("--------------------");
    }
}
