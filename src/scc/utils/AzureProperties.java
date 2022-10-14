package scc.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AzureProperties {
	
	public static final String STORAGE_CONNECTION_STRING = "STORAGE_CONNECTION_STRING";
	public static final String COSMOSDB_KEY = "COSMOSDB_KEY";
	public static final String COSMOSDB_URL = "COSMOSDB_URL";
	public static final String COSMOSDB_NAME = "COSMOSDB_NAME";
	public static final String REDIS_URL = "REDIS_URL";
	public static final String REDIS_KEY = "REDIS_KEY";

	public static final String PROPS_FILE = "azurekeys-westeurope.props";
	private static Properties props;
	
	public static synchronized Properties getProperties() {
		if (props == null) {
			props = new Properties();
			
			try {
				props.load(new FileInputStream(PROPS_FILE));
			}
			catch (IOException ignored) {}
		}
		
		return props;
	}

}
