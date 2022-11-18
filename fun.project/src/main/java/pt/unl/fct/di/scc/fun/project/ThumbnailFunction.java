package pt.unl.fct.di.scc.fun.project;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;

import data.Hash;

import com.microsoft.azure.functions.annotation.BlobTrigger;

import io.github.techgnious.IVCompressor;
import io.github.techgnious.dto.ImageFormats;
import io.github.techgnious.dto.ResizeResolution;
import io.github.techgnious.exception.ImageException;

/*
 * Azure Functions with Blob Trigger.
 */
public class ThumbnailFunction {
	private static final String STORAGE_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=storagewesteurope56773;AccountKey=51a8nEq5n7roROGz4qdo2frhSXNveYqW5mJNEzcQpPRupPDcqLWRXIvRXBI7ZAAJJlsZ6vPHSgxG+AStOY6vaQ==;EndpointSuffix=core.windows.net";
	
	private static final BlobContainerClient thumbnailContainer = new BlobContainerClientBuilder()
			.connectionString(STORAGE_CONNECTION_STRING)
			.containerName("thumbnails")
			.buildClient();
			
	private void uploadThumbnail(byte[] contents) {
		String key = Hash.of(contents);
		thumbnailContainer.getBlobClient(key).upload(BinaryData.fromBytes(contents), true);
	}
	
	@FunctionName("Thumbnail")
	public void setThumbnail(
			@BlobTrigger(
					name = "blobThumbnail",
					path = "images",
					connection = STORAGE_CONNECTION_STRING)
			byte[] content,
			final ExecutionContext context) throws ImageException {
		byte[] thumbnail = new IVCompressor().resizeImage(content, ImageFormats.JPG, ResizeResolution.R720P);
		uploadThumbnail(thumbnail);
	}
}
