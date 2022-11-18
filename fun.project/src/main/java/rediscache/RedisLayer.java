package rediscache;

import data.Auction;

public class RedisLayer extends RedisCache{

    private static final String AUCTIONS = "auctions";

    public static Auction putAuction(Auction auction) {
		return (getCachePool() != null ? writeToHashmap(AUCTIONS, auction.getTitle(), auction) : auction);
	}
    
}
