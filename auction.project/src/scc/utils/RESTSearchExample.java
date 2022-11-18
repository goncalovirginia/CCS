package scc.utils;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Based on the code from:
 * https://docs.microsoft.com/en-us/azure/search/search-get-started-java
 */
public class RESTSearchExample
{
	public static final String SEARCH_PROP_FILE = "search-azurekeys-westeurope.props";
	public static final String PROP_SERVICE_NAME = "SearchServiceName";
	public static final String PROP_QUERY_KEY = "SearchServiceQueryKey";

	private static Properties loadProperties(String propsFile) throws IOException {
		Properties props = new Properties();
		props.load(new FileReader(propsFile));
		return props;
	}
	public static void main(String[] args) {
		String propFile = SEARCH_PROP_FILE;
		if (args.length > 0) {
			propFile = args[0];
		}
		Properties props;
		try {
			props = loadProperties(propFile);

			// In the server, you can use System.getenv(property) if you set
			// the properties in the application using the commands in .sh file
			// created by SearchDumpProperties
			String serviceName = props.getProperty(PROP_SERVICE_NAME);
			String queryKey = props.getProperty(PROP_QUERY_KEY);

			String hostname = "https://" + serviceName + ".search.windows.net/";
			ClientConfig config = new ClientConfig();
			Client client = ClientBuilder.newClient(config);

			URI baseURI = UriBuilder.fromUri(hostname).build();

			WebTarget target = client.target(baseURI);

			String index = "cosmosdb-index";

			// SIMPLE QUERY
			// Check parameters at: https://docs.microsoft.com/en-us/rest/api/searchservice/search-documents
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode obj = mapper.createObjectNode();
			obj.put("count", "true");
			obj.put("search", "laboriosam");

			String resultStr = target.path("indexes/" + index + "/docs/search").queryParam("api-version", "2020-06-30")
					.request().header("api-key", queryKey)
					.accept(MediaType.APPLICATION_JSON).post(Entity.entity(obj.toString(), MediaType.APPLICATION_JSON))
					.readEntity(String.class);
			
			JsonNode resultObj = mapper.readTree(resultStr);
			
			System.out.println( "Number of results : " + resultObj.get("@odata.count").asInt());
			Iterator<JsonNode> it = resultObj.withArray("value").elements();
			while( it.hasNext()) {
				JsonNode el = it.next();
				System.out.println();
				Iterator<Entry<String, JsonNode>> fields = el.fields();
				while( fields.hasNext()) {
					Entry<String, JsonNode> val = fields.next();
					System.out.println( val.getKey() + "->" + val.getValue());
				}
			}
			
			
			System.out.println();
			System.out.println("=============== Second query ======================");
			obj = mapper.createObjectNode();
			obj.put("count", "true");
			obj.put("search", "laboriosam");
			obj.put("searchFields", "title");
			obj.put("select", "id,owner,title,decription");
			obj.put("filter", "owner eq 'Gardner.Labadie'");

			resultStr = target.path("indexes/" + index + "/docs/search").queryParam("api-version", "2019-05-06")
					.request().header("api-key", queryKey)
					.accept(MediaType.APPLICATION_JSON).post(Entity.entity(obj.toString(), MediaType.APPLICATION_JSON))
					.readEntity(String.class);
			
			System.out.println( "Number of results : " + resultObj.get("@odata.count").asInt());
			it = resultObj.withArray("value").elements();
			while( it.hasNext()) {
				JsonNode el = it.next();
				System.out.println();
				Iterator<Entry<String, JsonNode>> fields = el.fields();
				while( fields.hasNext()) {
					Entry<String, JsonNode> val = fields.next();
					System.out.println( val.getKey() + "->" + val.getValue());
				}
			}

			System.out.println();
			System.out.println("=============== Third query ======================");
			obj = mapper.createObjectNode();
			obj.put("count", "true");
			obj.put("search", "Gardner");
			obj.put("searchFields", "title,description");
			obj.put("searchMode", "all");
			obj.put("queryType", "full");
			obj.put("select", "id,owner,title,decription");

			resultStr = target.path("indexes/" + index + "/docs/search").queryParam("api-version", "2019-05-06")
					.request().header("api-key", queryKey)
					.accept(MediaType.APPLICATION_JSON).post(Entity.entity(obj.toString(), MediaType.APPLICATION_JSON))
					.readEntity(String.class);
			
			System.out.println( "Number of results : " + resultObj.get("@odata.count").asInt());
			it = resultObj.withArray("value").elements();
			while( it.hasNext()) {
				JsonNode el = it.next();
				System.out.println();
				Iterator<Entry<String, JsonNode>> fields = el.fields();
				while( fields.hasNext()) {
					Entry<String, JsonNode> val = fields.next();
					System.out.println( val.getKey() + "->" + val.getValue());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
