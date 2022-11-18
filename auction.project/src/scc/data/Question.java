package scc.data;

/**
 * Represents a Question made in an action
 */

public class Question {
	
	private String id;
	private String auction;
	private String user;
	private String text;
	private String answer;
	
	public Question() {
	}
	
	public Question(String id, String auction, String user, String text, String answer) {
		this();
		this.id = id;
		this.auction = auction;
		this.user = user;
		this.text = text;
		this.answer = answer;
	}
	
	public Question(QuestionDAO q) {
		this(q.getId(), q.getAuction(), q.getUser(), q.getText(), q.getAnswer());
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

	public String getAnswer(){
		return answer;
	}

	@Override
	public String toString() {
		return "Question [id=" + id + ", auction=" + auction + ", user=" + user + ", text=" + text + ", answer=" + answer + "]";
	}
	
}
