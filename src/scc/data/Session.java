package scc.data;

public class Session {

    private String uid;
    private String user;

    public Session(String uid, String user){
        this.uid = uid;
        this.user = user;
    }

    public String getUid() {
        return uid;
    }

    public String getUser() {
        return user;
    }

}
