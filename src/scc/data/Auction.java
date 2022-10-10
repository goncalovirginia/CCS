package scc.data;

public class Auction {
	
	private String title;
	private String description;
	private String photoId;
	private String owner;
	private String endTime;
	private String winner;
	private String status;
	private int minPrice;
	private int winningBid;
	
	public Auction(String title, String description, String photoId, String owner, String endTime, String status, int minPrice) {
		this.title = title;
		this.description = description;
		this.photoId = photoId;
		this.owner = owner;
		this.endTime = endTime;
		this.status = status;
		this.minPrice = minPrice;
	}
	
	public Auction(AuctionDAO a) {
		this(a.getTitle(), a.getDescription(), a.getPhotoId(), a.getOwner(), a.getEndTime(), a.getStatus(), a.getMinPrice());
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getPhotoId() {
		return photoId;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public String getEndTime() {
		return endTime;
	}
	
	public String getWinner() {
		return winner;
	}
	
	public String getStatus() {
		return status;
	}
	
	public int getMinPrice() {
		return minPrice;
	}
	
	public int getWinningBid() {
		return winningBid;
	}
	
}
