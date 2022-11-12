package scc.cache;

import scc.data.Auction;
import scc.data.Session;
import scc.data.User;

public class RedisLayer extends RedisCache{

    private static final String AUCTIONS = "auctions";
    private static final String SESSIONS = "usersession";
    private static final String USERS = "users";

    public static void putSession(Session session) {
		writeToHashmap(SESSIONS, session.getUid(), session);
	}

    public static Session getSession(String uid) {
        return readFromHashmap(SESSIONS, uid, Session.class);
	}

    public static Auction putAuction(Auction auction) {
		return writeToHashmap(AUCTIONS, auction.getTitle(), auction);
	}

    public static Auction getAuction(String aid) {
		return readFromHashmap(AUCTIONS, aid, Auction.class);
	}

    public static User putUser(User user) {
		return writeToHashmap(USERS, user.getId(), user);
	}

    public static User getUser(String usrid) {
		return readFromHashmap(USERS, usrid, User.class);
	}

    public static void delUser(String id){
        deleteFromHashmap(USERS, id);
    }
    
}
