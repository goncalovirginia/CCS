package scc.data;

public class Question {
	
	private String auction;
	private String user;
	private String text;
	
	public Question() {
	}
	
	public Question(String auction, String user, String text) {
		this();
		this.auction = auction;
		this.user = user;
		this.text = text;
	}
	
	public Question(QuestionDAO q) {
		this(q.getAuction(), q.getUser(), q.getText());
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
}
