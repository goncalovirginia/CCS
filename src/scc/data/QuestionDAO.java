package scc.data;

public class QuestionDAO {
	
	private String _rid;
	private String _ts;
	private String auction;
	private String user;
	private String text;
	
	public QuestionDAO() {
	}
	
	public QuestionDAO(String auction, String user, String text) {
		this();
		this.auction = auction;
		this.user = user;
		this.text = text;
	}
	
	public QuestionDAO(Question q) {
		this(q.getAuction(), q.getUser(), q.getText());
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
}
