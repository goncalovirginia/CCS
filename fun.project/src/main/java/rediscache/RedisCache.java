package rediscache;

import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisCache {
	
	private static final String REDIS_URL = "rediswesteurope56773.redis.cache.windows.net";
	private static final String REDIS_KEY = "fXHdSWcINecLDrOvsR2ztshTLB1UI0eKpAzCaF6zigU=";
	
	private static JedisPool instance;
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	public static synchronized JedisPool getCachePool() {
		if (instance == null) {
			final JedisPoolConfig poolConfig = new JedisPoolConfig();
			poolConfig.setMaxTotal(128);
			poolConfig.setMaxIdle(128);
			poolConfig.setMinIdle(16);
			poolConfig.setTestOnBorrow(true);
			poolConfig.setTestOnReturn(true);
			poolConfig.setTestWhileIdle(true);
			poolConfig.setNumTestsPerEvictionRun(3);
			poolConfig.setBlockWhenExhausted(true);
			instance = new JedisPool(poolConfig, REDIS_URL, 6380, 1000, REDIS_KEY, true);
		}
		
		return instance;
	}
	
	public static <T> T writeToHashmap(String key, String field, T value) {
		try {
			RedisCache.getCachePool().getResource().hset(key, field, mapper.writeValueAsString(value));
		}
		catch (Exception ignored) {
		}
		return value;
	}
	
	public static <T> T readFromHashmap(String key, String field, Class<T> type) {
		try {
			String cacheResult = RedisCache.getCachePool().getResource().hget(key, field);
			
			if (cacheResult != null) {
				return mapper.readValue(cacheResult, type);
			}
		}
		catch (Exception ignored) {
		}
		return null;
	}

	public static <T> T deleteFromHashmap(String key, String field) {
		try {
			RedisCache.getCachePool().getResource().hdel(key, field);
		}
		catch (Exception ignored) {
		}
		return null;
	}
	
}
