package scc.utils;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SearchPagedResponse;

import java.util.Map;

public class CognitiveSearchClient {
	
	private static final String SearchServiceQueryKey = "4P5F6wWD9JV496Gha3ahPVP4FFBktRoa7Stb3n0GS9AzSeCdUC9M"; //System.getenv(AzureProperties.SearchServiceQueryKey);
	private static final String SearchServiceUrl = "https://cognitivesearchwesteurope56773.search.windows.net"; //System.getenv(AzureProperties.SearchServiceUrl);
	private static final String IndexName = "cosmosdb-index"; //System.getenv(AzureProperties.IndexName);
	
	private static final SearchClient searchClient = new SearchClientBuilder()
			.credential(new AzureKeyCredential(SearchServiceQueryKey))
			.endpoint(SearchServiceUrl)
			.indexName(IndexName)
			.buildClient();
	
	public static String queryAuctionDescription(String description) {
		SearchOptions options = new SearchOptions()
				.setIncludeTotalCount(true)
				.setSelect("id", "owner", "title", "description")
				.setSearchFields("description");
		
		StringBuilder out = new StringBuilder();
		
		for (SearchPagedResponse resultResponse : searchClient.search(description, options, null).iterableByPage()){
			resultResponse.getValue().forEach(searchResult -> {
				for (Map.Entry<String, Object> res : searchResult.getDocument(SearchDocument.class).entrySet()) {
					out.append(res.getKey() + " -> " + res.getValue() + "\n");
				}
				out.append("\n");
			});
		}
		
		return out.toString();
	}
	
}
