package scc.cache;

import scc.data.Auction;
import scc.data.Session;
import scc.data.User;

public class RedisLayer extends RedisCache {
	
	private static final String AUCTIONS = "auctions";
	private static final String USER_SESSIONS = "usersessions";
	private static final String USERS = "users";
	
	public static boolean putSession(Session session) {
		boolean flag = false;
		if (getCachePool() != null) {
			writeToHashmap(USER_SESSIONS, session.getid(), session);
			flag = true;
		}
		return flag;
	}
	
	public static Session getSession(String uid) {
		return (getCachePool() != null ? readFromHashmap(USER_SESSIONS, uid, Session.class) : null);
	}
	
	public static Auction putAuction(Auction auction) {
		return (getCachePool() != null ? writeToHashmap(AUCTIONS, auction.getTitle(), auction) : auction);
	}
	
	public static Auction getAuction(String aid) {
		return (getCachePool() != null ? readFromHashmap(AUCTIONS, aid, Auction.class) : null);
	}
	
	public static User putUser(User user) {
		return (getCachePool() != null ? writeToHashmap(USERS, user.getId(), user) : user);
	}
	
	public static User getUser(String usrid) {
		return (getCachePool() != null ? readFromHashmap(USERS, usrid, User.class) : null);
	}
	
	public static void delUser(String id) {
		if (getCachePool() != null)
			deleteFromHashmap(USERS, id);
	}
	
}
