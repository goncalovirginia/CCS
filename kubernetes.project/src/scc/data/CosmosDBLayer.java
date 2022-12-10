package scc.data;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;

import javax.ws.rs.NotFoundException;

public class CosmosDBLayer {
	
	private static final String CONNECTION_URL = "https://cosmosdb64813.documents.azure.com:443/";
	private static final String DB_KEY = "Dm2Qw8cZgDwMtBxbVusNrTonmT1DUEvtDbJtkt3HcCA8GFjmim7UJn5jofi9Y2dElHqQfBukgQ0pACDbDxj91A==";
	private static final String DB_NAME = "cosmosdb64813";
	private static CosmosDBLayer instance;
	
	public static synchronized CosmosDBLayer getInstance() {
		if (instance == null) {
			CosmosClient client = new CosmosClientBuilder()
					.endpoint(CONNECTION_URL)
					.key(DB_KEY)
					.directMode()
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
			if(sessionDAO.getUser().equals(session.getUser()))
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
		}catch (Exception e) {
			throw new NotFoundException();
		}
	}

	public UserDAO getUserByIdForAC(String id) {
		try {
			return users.queryItems("SELECT * FROM users WHERE users.id=\"" + id + "\"", new CosmosQueryRequestOptions(), UserDAO.class).stream().toList().get(0);
		}catch (Exception e) {
			return null;
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
	
}
