package scc.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;
import scc.data.User;
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
			
			User user = mapper.readValue("{\"id\":\"Sanford.Kassulke\",\"name\":\"Sanford Kassulke\",\"pwd\":\"28PhQtyhHi63xBa\",\"photoId\":\"C60A30F49144A2830D451D30AE97B3061077C9FF\",\"channelIds\":[]}", User.class);
			System.out.println(user);
			
			Locale.setDefault(Locale.US);
			String id = "0:" + System.currentTimeMillis();
			UserDAO u = new UserDAO();
			u.setId(id);
			u.setName("SCC " + id);
			u.setPwd("super_secret");
			u.setPhotoId("0:34253455");
			u.setChannelIds(new String[0]);
			
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
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}


