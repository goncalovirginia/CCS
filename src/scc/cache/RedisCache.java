package scc.cache;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisCache {
	
	private static final String REDIS_HOSTNAME = "scc56773.redis.cache.windows.net";
	private static final String REDIS_KEY = "KAvpM7YzqoxVHFfz28hkuPvYsdSivDNRrAzCaGmhREs=";
	
	private static JedisPool instance;
	
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
			instance = new JedisPool(poolConfig, REDIS_HOSTNAME, 6380, 1000, REDIS_KEY, true);
		}
		
		return instance;
	}
	
}
