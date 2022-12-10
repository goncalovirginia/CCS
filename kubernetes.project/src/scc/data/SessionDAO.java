package scc.data;

/**
 * Represents a Session, as stored in the database
 */

public class SessionDAO {
	
	private String _rid;
	private String _ts;
	private String id;
	private String user;
	
	public SessionDAO() {
	}
	
	public SessionDAO(Session u) {
		this(u.getid(), u.getUser());
	}
	
	public SessionDAO(String id, String user) {
		this();
		this.id = id;
		this.user = user;
	}
	
	public String get_rid() {
		return _rid;
	}
	
	public void set_rid(String _rid) {
		this._rid = _rid;
	}
	
	public String get_ts() {
		return _ts;
	}
	
	public void set_ts(String _ts) {
		this._ts = _ts;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
}
