package scc.data;

/**
 * Represents a Bid to a certain auction
 */

public class Bid {
	
	private String id;
	private String auction;
	private String user;
	private int amount;
	
	public Bid() {
	}
	
	public Bid(String id, String auction, String user, int amount) {
		this();
		this.id = id;
		this.auction = auction;
		this.user = user;
		this.amount = amount;
	}
	
	public Bid(BidDAO b) {
		this(b.getId(), b.getAuction(), b.getUser(), b.getAmount());
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

	@Override
	public String toString() {
		return "Bid [id=" + id + ", auction=" + auction + ", user=" + user + ", amount=" + amount + "]";
	}
	
}
