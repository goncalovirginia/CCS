package scc.cache;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.utils.AzureProperties;

public class RedisCache {
	
	private static final String REDIS_URL = System.getenv(AzureProperties.REDIS_URL);
	private static final String REDIS_KEY = System.getenv(AzureProperties.REDIS_KEY);
	
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
			instance = new JedisPool(poolConfig, REDIS_URL, 6380, 1000, REDIS_KEY, true);
		}
		
		return instance;
	}
	
}
