package scc.utils;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.util.SearchPagedIterable;

public class CognitiveSearchClient {
	
	private static final String SearchServiceQueryKey = System.getenv(AzureProperties.SearchServiceQueryKey);
	private static final String SearchServiceUrl = System.getenv(AzureProperties.SearchServiceUrl);
	private static final String IndexName = System.getenv(AzureProperties.IndexName);
	
	private static final SearchClient searchClient = new SearchClientBuilder()
			.credential(new AzureKeyCredential(SearchServiceQueryKey))
			.endpoint(SearchServiceUrl)
			.indexName(IndexName)
			.buildClient();
	
	public SearchPagedIterable queryAuctionDescription(String description) {
		SearchOptions options = new SearchOptions()
				.setIncludeTotalCount(true)
				.setSelect("id", "owner", "title", "description")
				.setSearchFields("description");
		
		return searchClient.search(description, options, null);
	}
	
}
