package scc.data;

/**
 * Represents a Auction made by a user
 */

public class Auction {
	
	private String id;
	private String title;
	private String description;
	private String photoId;
	private String owner;
	private String endTime;
	private String winner;
	private String status;
	private int minPrice;
	private int winningBid;
	
	public Auction() {
	}
	
	public Auction(String id, String title, String description, String photoId, String owner, String endTime, String status, int minPrice) {
		this();
		this.id = id;
		this.title = title;
		this.description = description;
		this.photoId = photoId;
		this.owner = owner;
		this.endTime = endTime;
		this.status = status;
		this.minPrice = minPrice;
	}
	
	public Auction(AuctionDAO a) {
		this(a.getId(), a.getTitle(), a.getDescription(), a.getPhotoId(), a.getOwner(), a.getEndTime(), a.getStatus(), a.getMinPrice());
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
	
	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Auction [id=" + id + ", title=" + title + ", description=" + description + ", photoId=" + photoId + 
		", owner=" + owner + ", endTime=" + endTime + ", status=" + status + ", minPrice=" + minPrice + "]";
	}
}
