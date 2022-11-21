package scc.data;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import scc.utils.AzureProperties;

import javax.ws.rs.NotFoundException;

import java.util.ArrayList;
import java.util.Arrays;

public class CosmosDBLayer {
	
	private static final String CONNECTION_URL = System.getenv(AzureProperties.COSMOSDB_URL);
	private static final String DB_KEY = System.getenv(AzureProperties.COSMOSDB_KEY);
	private static final String DB_NAME = System.getenv(AzureProperties.COSMOSDB_NAME);
	private static CosmosDBLayer instance;
	private static ArrayList<String> regions = new ArrayList<String>(Arrays.asList("West Europe", "North Europe"));
	
	public static synchronized CosmosDBLayer getInstance() {
		if (instance == null) {
			CosmosClient client = new CosmosClientBuilder()
					.endpoint(CONNECTION_URL)
					.key(DB_KEY)
					.multipleWriteRegionsEnabled(true)
					.preferredRegions(regions)
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
	private CosmosContainer sessions;
	private CosmosContainer auctionsFreq;
	private CosmosContainer auctionsTrending;
	
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
			sessions = db.getContainer("sessions");
			auctionsFreq = db.getContainer("auctionsFreq");
			auctionsTrending = db.getContainer("auctionsTrending");
		}
	}
	
	public void close() {
		client.close();
	}

	public CosmosItemResponse<SessionDAO> putSession(SessionDAO session) {
		return sessions.createItem(session);
	}
	
	public SessionDAO getSession(String id) {
		try {
			return sessions.queryItems("SELECT * FROM sessions WHERE sessions.id=\"" + id + "\"", new CosmosQueryRequestOptions(), SessionDAO.class).stream().toList().get(0);
		}
		catch (Exception e) {
			throw new NotFoundException();
		}
	}

	public CosmosPagedIterable<SessionDAO> getAllSessions() {
		try {
			return sessions.queryItems("SELECT * FROM sessions", new CosmosQueryRequestOptions(), SessionDAO.class);
		}
		catch (Exception e) {
			throw new NotFoundException();
		}
	}

	public CosmosItemResponse<Object> delSession(SessionDAO session) {
		CosmosPagedIterable<SessionDAO> allSessions = getAllSessions();
		for (SessionDAO sessionDAO : allSessions) {
			if(sessionDAO.getUser() == session.getUser())
				return sessions.deleteItem(sessionDAO, new CosmosItemRequestOptions());
		}
		return null;
	}
	
	public CosmosItemResponse<UserDAO> putUser(UserDAO user) {
		return users.createItem(user);
	}
	
	public UserDAO getUserById(String id) {
		try {
			return users.queryItems("SELECT * FROM users WHERE users.id=\"" + id + "\"", new CosmosQueryRequestOptions(), UserDAO.class).stream().toList().get(0);
		}
		catch (Exception e) {
			throw new NotFoundException();
		}
	}
	
	public CosmosPagedIterable<UserDAO> getUsers() {
		return users.queryItems("SELECT * FROM users", new CosmosQueryRequestOptions(), UserDAO.class);
	}
	
	public CosmosItemResponse<Object> delUser(UserDAO user) {
		return users.deleteItem(user, new CosmosItemRequestOptions());
	}
	
	public CosmosItemResponse<Object> delUserById(String id) {
		questions.queryItems("UPDATE questions SET questions.user=\"Deleted.User\" WHERE questions.user=\"" + id + "\"", new CosmosQueryRequestOptions(), QuestionDAO.class);
		bids.queryItems("UPDATE bids SET bids.user=\"Deleted.User\" WHERE bids.user=\"" + id + "\"", new CosmosQueryRequestOptions(), BidDAO.class);
		auctions.queryItems("UPDATE auctions SET auctions.owner=\"Deleted.User\" WHERE auctions.owner=\"" + id + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class);
		return users.deleteItem(id, new PartitionKey(id), new CosmosItemRequestOptions());
	}
	
	public CosmosItemResponse<AuctionDAO> putAuction(AuctionDAO auction) {
		return auctions.createItem(auction);
	}
	
	public AuctionDAO getAuctionById(String id) {
		try {
			return auctions.queryItems("SELECT * FROM auctions WHERE auctions.id=\"" + id + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class).stream().toList().get(0);
		}
		catch (Exception e) {
			throw new NotFoundException();
		}
	}
	
	public CosmosPagedIterable<AuctionDAO> getAuctions() {
		return auctions.queryItems("SELECT * FROM auctions", new CosmosQueryRequestOptions(), AuctionDAO.class);
	}
	
	public CosmosPagedIterable<AuctionDAO> getAuctionsEndingSoon() {
		return auctions.queryItems("SELECT * FROM auctions WHERE auctions.status=\"open\" AND DATETIMEDIFF(\"day\", GETCURRENTDATETIME(), auctions.endTime)<=2 AND DATETIMEDIFF(\"day\", GETCURRENTDATETIME(), auctions.endTime)>=0", new CosmosQueryRequestOptions(), AuctionDAO.class);
	}
	
	public CosmosPagedIterable<AuctionDAO> getAuctionsByOwner(String id) {
		return auctions.queryItems("SELECT * FROM auctions WHERE auctions.owner=\"" + id + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class);
	}
	
	public AuctionDAO getAuctionsByOwnerAndName(String id, String name) {
		return auctions.queryItems("SELECT * FROM auctions WHERE auctions.owner=\"" + id + "\" AND auctions.title=\"" + name + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class).stream().toList().get(0);
	}
	
	public CosmosItemResponse<BidDAO> putBid(BidDAO bid) {
		return bids.createItem(bid);
	}
	
	public CosmosPagedIterable<BidDAO> getBids(String auction) {
		return bids.queryItems("SELECT * FROM bids WHERE bids.auction=\"" + auction + "\"", new CosmosQueryRequestOptions(), BidDAO.class);
	}
	
	public CosmosPagedIterable<BidDAO> getBidsByOwner(String id) {
		return bids.queryItems("SELECT * FROM bids WHERE bids.user=\"" + id + "\"", new CosmosQueryRequestOptions(), BidDAO.class);
	}
	
	public CosmosItemResponse<QuestionDAO> putQuestion(QuestionDAO question) {
		try {
			questions.deleteItem(question, new CosmosItemRequestOptions());
		}
		catch (Exception ignored) {}
		
		return questions.createItem(question);
	}
	
	public CosmosPagedIterable<QuestionDAO> getQuestions(String auction) {
		return questions.queryItems("SELECT * FROM questions WHERE questions.auction=\"" + auction + "\"", new CosmosQueryRequestOptions(), QuestionDAO.class);
	}
	
	public CosmosPagedIterable<QuestionDAO> getQuestionsByOwner(String id) {
		return questions.queryItems("SELECT * FROM questions WHERE questions.owner=\"" + id + "\"", new CosmosQueryRequestOptions(), QuestionDAO.class);
	}
	
	public QuestionDAO getQuestion(String id) {
		try {
			return questions.queryItems("SELECT * FROM questions WHERE questions.id=\"" + id + "\"", new CosmosQueryRequestOptions(), QuestionDAO.class).stream().toList().get(0);
		}
		catch (Exception e) {
			throw new NotFoundException();
		}
	}
	
	public CosmosPagedIterable<Object> getAuctionCountPerUser() {
		return auctionsFreq.queryItems("SELECT * FROM auctionsFreq", new CosmosQueryRequestOptions(), Object.class);
	}
	
	public CosmosPagedIterable<Object> getTrendingAuctions() {
		return auctionsTrending.queryItems("SELECT * FROM auctionsTrending", new CosmosQueryRequestOptions(), Object.class);
	}
	
}
