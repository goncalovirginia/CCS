package data;

public class AuctionDAO {
	
	private String _rid;
	private String _ts;
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
	
	public AuctionDAO() {
	}
	
	public AuctionDAO(Auction a) {
		this(a.getId(), a.getTitle(), a.getDescription(), a.getPhotoId(), a.getOwner(), a.getEndTime(), a.getStatus(), a.getMinPrice());
	}
	
	public AuctionDAO(String id, String title, String description, String photoId, String owner, String endTime, String status, int minPrice) {
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
	
	public String get_rid() {
		return _rid;
	}
	
	public String get_ts() {
		return _ts;
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
	
	public void setStatus(String status) {
		this.status = status;
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
	
	public void setWinningBid(int amount) {
		this.winningBid = amount;
	}
	
	public void setWinner(String winner) {
		this.winner = winner;
	}
	
}
