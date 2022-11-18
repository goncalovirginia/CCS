package pt.unl.fct.di.scc.fun.project;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import dblayer.CosmosDBLayer;

import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class CloseAuctionsFunction {
    @FunctionName("CloseAuctions")
    public void closeAuctionsOnUserDelete(
            @HttpTrigger(
                name = "closeAuctions",
                methods = {HttpMethod.DELETE},
                route = "rest/user/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {
    	CosmosDBLayer db = CosmosDBLayer.getInstance();
    	db.closeAuctions(id);
    }
}
