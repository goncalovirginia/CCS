package dblayer;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosQueryRequestOptions;

import data.AuctionDAO;
import data.UserDAO;

import java.util.List;

import javax.ws.rs.NotFoundException;

public class CosmosDBLayer {
	
	private static final String CONNECTION_URL = "https://cosmosdb56773.documents.azure.com:443/";
	private static final String DB_KEY = "llmIHJgQO0ynhDNaZqeTO9YIV5dEAkpf8zzfoPth2cJeoMQgpKRkvmzvb4VUnKeXI4ETFRKBbKPXsrpUzapbCA==";
	private static final String DB_NAME = "cosmosdb56773";
	
	private static CosmosDBLayer instance;
	
	public static synchronized CosmosDBLayer getInstance() {
		if (instance == null) {
			CosmosClient client = new CosmosClientBuilder()
					.endpoint(CONNECTION_URL)
					.key(DB_KEY)
					.directMode()
					//.gatewayMode()
					// replace by .directMode() for better performance
					.consistencyLevel(ConsistencyLevel.SESSION)
					.connectionSharingAcrossClientsEnabled(true)
					.contentResponseOnWriteEnabled(true)
					.buildClient();
			
			instance = new CosmosDBLayer(client);
		}
		
		return instance;
	}
	
	private final CosmosClient client;
	private CosmosDatabase db;
	private CosmosContainer users;
	private CosmosContainer auctions;
	private CosmosContainer bids;
	private CosmosContainer questions;
	
	public CosmosDBLayer(CosmosClient client) {
		this.client = client;
		init();
	}
	
	private synchronized void init() {
		if (db == null) {
			db = client.getDatabase(DB_NAME);
			users = db.getContainer("users");
			auctions = db.getContainer("auctions");
			bids = db.getContainer("bids");
			questions = db.getContainer("questions");
		}
	}
	
	public CosmosContainer getAuctions() { return auctions; }
	
	public UserDAO getUserById(String id) {
		try {
			return users.queryItems("SELECT * FROM users WHERE users.id=\"" + id + "\"", new CosmosQueryRequestOptions(), UserDAO.class).stream().toList().get(0);
		}
		catch (Exception e) {
			throw new NotFoundException();
		}
	}
	
	public void closeAuctions(String id) {
		auctions.queryItems("UPDATE auctions SET auctions.status=\"closed\" WHERE auctions.owner=\"" + getUserById(id).getName() + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class);			
	}
	
	
	public List<String> getTop10Auctions() {
		return bids.queryItems(
				"SELECT bids.auction FROM bids GROUP BY bids.auction ORDER BY COUNT(bids.auction) DESC LIMIT 10", new CosmosQueryRequestOptions(), String.class
				).stream().map(String::new).toList();
	}
	
	public List<String> getTop5Questions() {
		return questions.queryItems(
				"SELECT questions.auction FROM questions ORDER BY questions.id DESC LIMIT 5", new CosmosQueryRequestOptions(), String.class
				).stream().map(String::new).toList();
	}
	
}
