package scc.data;

import java.util.UUID;

/**
 * Represents a Bid, as stored in the database
 */

public class BidDAO {
	
	private String _rid;
	private String _ts;
	private String id;
	private String auction;
	private String user;
	private int amount;
	
	public BidDAO() {
	}
	
	public BidDAO(String id, String auction, String user, int amount) {
		this();
		this.id = id;
		this.auction = auction;
		this.user = user;
		this.amount = amount;
	}
	
	public BidDAO(Bid b) {
		this(UUID.randomUUID().toString(), b.getAuction(), b.getUser(), b.getAmount());
	}
	
	public String get_rid() {
		return _rid;
	}
	
	public String get_ts() {
		return _ts;
	}
	
	public String getAuction() {
		return auction;
	}
	
	public String getUser() {
		return user;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public String getId() {
		return id;
	}
}
