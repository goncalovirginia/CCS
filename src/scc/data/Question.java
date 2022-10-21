package scc.data;

public class Question {
	
	private String id;
	private String auction;
	private String user;
	private String text;
	
	public Question() {
	}
	
	public Question(String id, String auction, String user, String text) {
		this();
		this.id = id;
		this.auction = auction;
		this.user = user;
		this.text = text;
	}
	
	public Question(QuestionDAO q) {
		this(q.getId(), q.getAuction(), q.getUser(), q.getText());
	}
	
	public String getAuction() {
		return auction;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getText() {
		return text;
	}
	
	public String getId() {
		return id;
	}
	
}
