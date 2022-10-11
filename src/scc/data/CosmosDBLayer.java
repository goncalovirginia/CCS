package scc.data;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;

public class CosmosDBLayer {
	
	private static final String CONNECTION_URL = "https://scc56773.documents.azure.com:443/";
	private static final String DB_KEY = "DhOA3a9yd6wy2aBU1m3ayanUHB1bge4iK3a8CyYEQdFYyrEXE7UDdSldznpI8LsyWACfn5zPRzoYbBkc8WdFoA==";
	private static final String DB_NAME = "scc56773db";
	
	private static CosmosDBLayer instance;
	
	public static synchronized CosmosDBLayer getInstance() {
		if (instance == null) {
			CosmosClient client = new CosmosClientBuilder()
					.endpoint(CONNECTION_URL)
					.key(DB_KEY)
					//.directMode()
					.gatewayMode()
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
	
	public CosmosItemResponse<Object> delUserById(String id) {
		PartitionKey key = new PartitionKey(id);
		return users.deleteItem(id, key, new CosmosItemRequestOptions());
	}
	
	public CosmosItemResponse<Object> delUser(UserDAO user) {
		return users.deleteItem(user, new CosmosItemRequestOptions());
	}
	
	public CosmosItemResponse<UserDAO> putUser(UserDAO user) {
		return users.createItem(user);
	}
	
	public CosmosPagedIterable<UserDAO> getUserById(String id) {
		return users.queryItems("SELECT * FROM users WHERE users.id=\"" + id + "\"", new CosmosQueryRequestOptions(), UserDAO.class);
	}
	
	public CosmosPagedIterable<UserDAO> getUsers() {
		return users.queryItems("SELECT * FROM users", new CosmosQueryRequestOptions(), UserDAO.class);
	}
	
	public void close() {
		client.close();
	}
	
	public CosmosItemResponse<AuctionDAO> putAuction(AuctionDAO auction) {
		return auctions.createItem(auction);
	}
	
	public CosmosPagedIterable<AuctionDAO> getAuctionByTitle(String title) {
		return auctions.queryItems("SELECT * FROM auctions WHERE auctions.title=\"" + title + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class);
	}
	
	public CosmosItemResponse<BidDAO> putBid(BidDAO bid) {
		return bids.createItem(bid);
	}
	
	public CosmosPagedIterable<BidDAO> getBids(String auction) {
		return bids.queryItems("SELECT * FROM bids WHERE bids.auction=\"" + auction + "\"", new CosmosQueryRequestOptions(), BidDAO.class);
	}
	
	public CosmosItemResponse<QuestionDAO> putQuestion(QuestionDAO question) {
		return questions.createItem(question);
	}
	
	public CosmosPagedIterable<QuestionDAO> getQuestions(String auction) {
		return questions.queryItems("SELECT * FROM questions WHERE questions.auction=\"" + auction + "\"", new CosmosQueryRequestOptions(), QuestionDAO.class);
	}
	
}
