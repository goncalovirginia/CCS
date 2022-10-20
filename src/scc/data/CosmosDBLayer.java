package scc.data;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import scc.utils.AzureProperties;

import javax.ws.rs.NotFoundException;

public class CosmosDBLayer {
	
	private static final String CONNECTION_URL = System.getenv(AzureProperties.COSMOSDB_URL);
	private static final String DB_KEY = System.getenv(AzureProperties.COSMOSDB_KEY);
	private static final String DB_NAME = System.getenv(AzureProperties.COSMOSDB_NAME);
	
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
	
	public CosmosItemResponse<Object> delUserById(String id) {
		return users.deleteItem(id, new PartitionKey(id), new CosmosItemRequestOptions());
	}
	
	public CosmosItemResponse<Object> delUser(UserDAO user) {
		return users.deleteItem(user, new CosmosItemRequestOptions());
	}
	
	public CosmosItemResponse<UserDAO> putUser(UserDAO user) {
		return users.createItem(user);
	}
	
	public UserDAO getUserById(String id) {
		try {
			return users.queryItems("SELECT * FROM users WHERE users.id=\"" + id + "\"", new CosmosQueryRequestOptions(), UserDAO.class).stream().toList().get(0);
		}
		catch (Exception e) {
			throw new NotFoundException("User does not exist.");
		}
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
	
	public AuctionDAO getAuctionByTitle(String title) {
		try {
			return auctions.queryItems("SELECT * FROM auctions WHERE auctions.title=\"" + title + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class).stream().toList().get(0);
		}
		catch (Exception e) {
			throw new NotFoundException("Auction does not exist.");
		}
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
