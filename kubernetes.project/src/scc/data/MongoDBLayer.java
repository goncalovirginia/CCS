package scc.data;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bson.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import redis.clients.jedis.Jedis;
import scc.EnvironmentProperties;

import javax.ws.rs.NotFoundException;

public class MongoDBLayer {

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(4);

    private static final MongoClient client = MongoClients.create(EnvironmentProperties.MONGO_CONN);
    private static final MongoDatabase db = client.getDatabase(EnvironmentProperties.DB_NAME);
	private static final MongoCollection<Document> users = db.getCollection("users");
	private static final MongoCollection<Document> auctions = db.getCollection("auctions");
	private static final MongoCollection<Document> bids = db.getCollection("bids");
	private static final MongoCollection<Document> questions = db.getCollection("questions");
	private static final ObjectMapper mapper = new ObjectMapper();

    public MongoDBLayer() {
		
    }

    private MongoCollection<Document> getCollection(MongoDBCollection collection) {
	    return switch (collection) {
		    case USERS -> users;
		    case AUCTIONS -> auctions;
		    case BIDS -> bids;
		    case QUESTIONS -> questions;
	    };
    }
	
	public void insertDocument(MongoDBCollection collection, Document insert) {
		getCollection(collection).insertOne(insert);
	}
	
	public void updateDocument(MongoDBCollection collection, Document filter, Document update) {
		getCollection(collection).updateOne(filter, update);
	}
	
	public Document getDocument(MongoDBCollection collection, Document filter) {
		Document document = getCollection(collection).find(filter).first();
		
		if (document == null) {
			throw new NotFoundException();
		}
		
		return document;
	}
	
	public void deleteDocument(MongoDBCollection collection, Document filter) {
		getCollection(collection).deleteOne(filter);
	}

}