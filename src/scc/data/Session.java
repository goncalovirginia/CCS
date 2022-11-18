package scc.data;

/**
 * Represents a Session for authentication
 */

public class Session {

    private String id;
    private String user;

    public Session(){};

    public Session(String id, String user) {
        this.id = id;
        this.user = user;
	}

    public Session(SessionDAO u) {
		this(u.getId(), u.getUser());
	}

	public String getid() {
		return id;
	}
	
	public void setid(String id) {
		this.id = id;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}

    @Override
	public String toString() {
		return "Session [id=" + id + ", user=" + user + "]";
	}
	

}
