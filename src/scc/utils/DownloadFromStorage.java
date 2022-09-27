package scc.utils;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

public class DownloadFromStorage {
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Use: java scc.utils.DownloadFromStorage filename");
		}
		
		String filename = args[0];
		
		// Get connection string in the storage access keys page
		String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=scc56773;AccountKey=MbBOrQgpW5oePKpSsEqqYFU3bCOSBIOTCd8+xhs7D40fVREzaS0q2NarLjS/z/Tl5ry/xR4kqvQE+AStFOE1/w==;EndpointSuffix=core.windows.net";
		
		try {
			// Get container client
			BlobContainerClient containerClient = new BlobContainerClientBuilder()
					.connectionString(storageConnectionString)
					.containerName("images")
					.buildClient();
			
			// Get client to blob
			BlobClient blob = containerClient.getBlobClient(filename);
			
			// Download contents to BinaryData (check documentation for other alternatives)
			BinaryData data = blob.downloadContent();
			
			byte[] arr = data.toBytes();
			
			System.out.println("Blob size : " + arr.length);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
