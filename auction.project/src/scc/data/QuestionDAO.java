package scc.data;

/**
 * Represents a Question, as stored in the database
 */

public class QuestionDAO {
	
	private String _rid;
	private String _ts;
	private String id;
	private String auction;
	private String user;
	private String text;
	
	public QuestionDAO() {
	}
	
	public QuestionDAO(String id, String auction, String user, String text) {
		this();
		this.id = id;
		this.auction = auction;
		this.user = user;
		this.text = text;
	}
	
	public QuestionDAO(Question q) {
		this(q.getId(), q.getAuction(), q.getUser(), q.getText());
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
	
	public String getText() {
		return text;
	}
	
	public String getId() {
		return id;
	}
}
