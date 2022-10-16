package scc.data;

public class Bid {
	
	private String auction;
	private String user;
	private int amount;
	
	public Bid() {
	}
	
	public Bid(String auction, String user, int amount) {
		this();
		this.auction = auction;
		this.user = user;
		this.amount = amount;
	}
	
	public Bid(BidDAO b) {
		this(b.getAuction(), b.getUser(), b.getAmount());
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
	
}
