package scc.utils;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.SearchMode;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SearchPagedResponse;

/**
 * Based on the code from:
 * https://docs.microsoft.com/en-us/azure/search/search-get-started-java
 */
public class SearchExample {
	public static final String SEARCH_PROP_FILE = "search-azurekeys-westeurope.props";
	
	public static final String PROP_SERVICE_NAME = "SearchServiceName";
	public static final String PROP_SERVICE_URL = "SearchServiceUrl";
	public static final String PROP_INDEX_NAME = "IndexName";
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
			// WHEN READING FROM THE PROPERTIES FILE 
			SearchClient searchClient = new SearchClientBuilder()
					.credential(new AzureKeyCredential(props.getProperty(PROP_QUERY_KEY)))
					.endpoint(props.getProperty(PROP_SERVICE_URL)).indexName(props.getProperty(PROP_INDEX_NAME))
					.buildClient();
			// WHEN READING THE PROPERTIES SET IN AZURE 
/*			SearchClient searchClient = new SearchClientBuilder()
					.credential(new AzureKeyCredential(System.getenv(PROP_QUERY_KEY)))
					.endpoint(System.getenv(PROP_SERVICE_URL)).indexName(System.getenv(PROP_INDEX_NAME))
					.buildClient();
*/
			// SIMPLE QUERY
			// Check parameters at:
			// https://docs.microsoft.com/en-us/rest/api/searchservice/search-documents
			String queryText = "laboriosam";
			SearchOptions options = new SearchOptions().setIncludeTotalCount(true).setTop(5);

			SearchPagedIterable searchPagedIterable = searchClient.search(queryText, options, null);
			System.out.println("Number of results : " + searchPagedIterable.getTotalCount());

			for (SearchPagedResponse resultResponse : searchPagedIterable.iterableByPage()) {
				resultResponse.getValue().forEach(searchResult -> {
					for (Map.Entry<String, Object> res : searchResult.getDocument(SearchDocument.class).entrySet()) {
						System.out.printf("%s -> %s\n", res.getKey(), res.getValue());
					}
					System.out.println();
				});
			}

			System.out.println();
			System.out.println("=============== Second query ======================");
			queryText = "laboriosam";
			options = new SearchOptions().setIncludeTotalCount(true)
					.setFilter("owner eq 'Gardner.Labadie'")
					.setSelect("id", "owner", "title", "description")
					.setSearchFields("title")
					.setTop(5);

			searchPagedIterable = searchClient.search(queryText, options, null);
			System.out.println("Number of results : " + searchPagedIterable.getTotalCount());

			for (SearchPagedResponse resultResponse : searchPagedIterable.iterableByPage()) {
				resultResponse.getValue().forEach(searchResult -> {
					for (Map.Entry<String, Object> res : searchResult.getDocument(SearchDocument.class).entrySet()) {
						System.out.printf("%s -> %s\n", res.getKey(), res.getValue());
					}
					System.out.println();
				});
			}

			System.out.println();
			System.out.println("=============== Third query ======================");
			queryText = "Gardner";
			options = new SearchOptions().setIncludeTotalCount(true)
					.setSelect("id", "owner", "title", "description").setSearchFields("title", "description").setSearchMode(SearchMode.ALL)
					.setTop(5);

			searchPagedIterable = searchClient.search(queryText, options, null);
			System.out.println("Number of results : " + searchPagedIterable.getTotalCount());

			for (SearchPagedResponse resultResponse : searchPagedIterable.iterableByPage()) {
				resultResponse.getValue().forEach(searchResult -> {
					for (Map.Entry<String, Object> res : searchResult.getDocument(SearchDocument.class).entrySet()) {
						System.out.printf("%s -> %s\n", res.getKey(), res.getValue());
					}
					System.out.println();
				});
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
