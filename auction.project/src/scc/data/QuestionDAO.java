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
	private String answer;
	
	public QuestionDAO() {
	}
	
	public QuestionDAO(String id, String auction, String user, String text, String answer) {
		this();
		this.id = id;
		this.auction = auction;
		this.user = user;
		this.text = text;
		this.answer = answer;
	}
	
	public QuestionDAO(Question q) {
		this(q.getId(), q.getAuction(), q.getUser(), q.getText(), q.getAnswer());
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
	
	public String getAnswer() {
		return answer;
	}
	
	public String getId() {
		return id;
	}
}
