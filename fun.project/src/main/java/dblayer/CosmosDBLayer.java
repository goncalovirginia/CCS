package dblayer;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;

import data.Auction;
import data.AuctionDAO;
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

	public void closeAuction(AuctionDAO auction) {
		auction.setStatus("closed");
		auctions.deleteItem(auction, new CosmosItemRequestOptions());
		auctions.createItem(auction);
		RedisLayer.putAuction(new Auction(auction));
	}
	
}

