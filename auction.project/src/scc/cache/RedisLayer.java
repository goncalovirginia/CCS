package scc.cache;

import scc.data.Auction;
import scc.data.Session;
import scc.data.User;

public class RedisLayer extends RedisCache{

    private static final String AUCTIONS = "auctions";
    private static final String USER_SESSIONS = "usersessions";
    private static final String USERS = "users";

    public static void putSession(Session session) {
		writeToHashmap(USER_SESSIONS, session.uuid(), session);
	}

    public static Session getSession(String uid) {
        return readFromHashmap(USER_SESSIONS, uid, Session.class);
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

    public static User getUser(String userId) {
		return readFromHashmap(USERS, userId, User.class);
	}

    public static void delUser(String id){
        deleteFromHashmap(USERS, id);
    }
    
}
