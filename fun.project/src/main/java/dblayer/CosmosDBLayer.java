package dblayer;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;

import data.Auction;
import data.AuctionDAO;
import data.BidDAO;
import rediscache.RedisLayer;
import utils.AzureProperties;

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
	private CosmosContainer auctions;
	
	public CosmosDBLayer(CosmosClient client) {
		this.client = client;
		init();
	}
	
	private synchronized void init() {
		if (db == null) {
			db = client.getDatabase(DB_NAME);
			auctions = db.getContainer("auctions");
		}
	}
	
	public CosmosPagedIterable<AuctionDAO> getAuctionsToClose() {
		return auctions.queryItems("SELECT * FROM auctions WHERE auctions.endTime < GETCURRENTDATETIME()", new CosmosQueryRequestOptions(), AuctionDAO.class);
	}
	
	public BidDAO getTopBid(String auctionTitle) {
		CosmosPagedIterable<BidDAO> bids = auctions.queryItems("SELECT * FROM bids WHERE bids.auction=\"" + auctionTitle + "\"", new CosmosQueryRequestOptions(), BidDAO.class);
		
		int highestAmount = 0;
		BidDAO highestBid = null;
		
		for (BidDAO bid : bids) {
			if (bid.getAmount() > highestAmount) {
				highestBid = bid;
			}
		}
		
		return highestBid;
	}

	public void closeAuction(AuctionDAO auction) {
		auctions.deleteItem(auction, new CosmosItemRequestOptions());
		BidDAO topBid = getTopBid(auction.getTitle());
		
		auction.setStatus("closed");
		
		if (topBid == null) {
			auction.setWinningBid(0);
			auction.setWinner(null);
		}
		else {
			auction.setWinningBid(topBid.getAmount());
			auction.setWinner(topBid.getUser());
		}
		
		auctions.createItem(auction);
		RedisLayer.putAuction(new Auction(auction));
	}
	
}

