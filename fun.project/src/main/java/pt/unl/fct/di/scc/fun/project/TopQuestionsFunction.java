package pt.unl.fct.di.scc.fun.project;

import java.util.List;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;

import dblayer.CosmosDBLayer;
import redis.clients.jedis.Jedis;
import rediscache.RedisCache;

import com.microsoft.azure.functions.annotation.CosmosDBTrigger;

/**
 * Azure Functions with CosmosDB Trigger.
 */
public class TopQuestionsFunction {	
	private static final String COSMOS_CONNECTION_STRING = "AccountEndpoint=https://scc2364813.documents.azure.com:443/;AccountKey=yWSKeUBtMbLQT7PNYiXoWH2AcJlox5IOzrqkEugPeSqqKwjQapCktmV33KmEZqE7vQH2MlLTtle2ACDbdeY4Lw==;";
	
    @FunctionName("TopQuestions")
    public void Top5Questions(
    		@CosmosDBTrigger(
                    name = "top5Questions",
                    databaseName = "cosmosdb56773",
                    collectionName = "questions",
                    createLeaseCollectionIfNotExists = true,
                    connectionStringSetting = COSMOS_CONNECTION_STRING)
                String[] questions,
            final ExecutionContext context) {
        try (Jedis jedis = RedisCache.getCachePool().getResource()) {
        	CosmosDBLayer db = CosmosDBLayer.getInstance();
        	List<String> data = db.getTop5Questions();
        	for (String question : data) jedis.lpush("serverless::cosmos::questions", question);
        	jedis.ltrim("serverless::cosmos::questions", 0, 4);
        }
    }
}
