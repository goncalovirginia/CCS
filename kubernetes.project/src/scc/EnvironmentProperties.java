package scc;

public class EnvironmentProperties {
	
	static public final String REDIS_HOSTNAME = System.getenv("REDIS_HOSTNAME");
	static public final String BLOB_PATH = System.getenv("BLOB_PATH");
	static public final String MONGO_CONN = System.getenv("MONGO_CONN");
	static public final String DB_NAME = System.getenv("DB_NAME");
	
}
