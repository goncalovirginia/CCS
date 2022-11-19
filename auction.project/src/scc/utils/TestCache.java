package scc.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;
import scc.data.UserDAO;

import java.util.List;
import java.util.Locale;

/**
 * Standalone program for accessing Redis cache
 */
public class TestCache {
	
	public static void main(String[] args) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			
			Locale.setDefault(Locale.US);
			String id = "0:" + System.currentTimeMillis();
			UserDAO u = new UserDAO();
			u.setId(id);
			u.setName("SCC " + id);
			u.setPwd("super_secret");
			u.setPhotoId("0:34253455");
			
			String user = mapper.writeValueAsString(u);
			System.out.println(user);
			UserDAO usr = mapper.readValue(user, UserDAO.class);
			System.out.println(usr);
			
			try (Jedis jedis = RedisCache.getCachePool().getResource()) {
				jedis.hset("aaa", "bbb", "ccc");
				System.out.println(jedis.hget("aaa", "bbb"));
				System.out.println(jedis.hget("aaa", "bb"));
				
				jedis.set("user:" + id, mapper.writeValueAsString(u));
				String res = jedis.get("user:" + id);
				System.out.println("GET value = " + res);
				
				long cnt = jedis.lpush("MostRecentUsers", mapper.writeValueAsString(u));
				if (cnt > 5)
					jedis.ltrim("MostRecentUsers", 0, 4);
				
				List<String> lst = jedis.lrange("MostRecentUsers", 0, -1);
				System.out.println("MostRecentUsers");
				for (String s : lst)
					System.out.println(s);
				
				cnt = jedis.incr("NumUsers");
				System.out.println("Num users : " + cnt);
				System.out.println(jedis.flushAll());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}


