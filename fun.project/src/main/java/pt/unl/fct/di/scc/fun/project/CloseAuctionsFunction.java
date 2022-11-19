package pt.unl.fct.di.scc.fun.project;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

import data.Auction;
import dblayer.CosmosDBLayer;

/**
 * Azure Functions with Timer Trigger.
 */
public class CloseAuctionsFunction {
	
    @FunctionName("CloseAuctions")
    public void closeAuctions(
            @TimerTrigger(name = "closeAuctions", schedule = "0 0 0 * * *")
            String timer,
            final ExecutionContext context) {
    	        CosmosDBLayer db = CosmosDBLayer.getInstance();
		
    	        for (Auction auction : db.getAuctionsToClose()) {
					db.closeAuction(auction);
	            }
            }
}
