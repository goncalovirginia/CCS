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
import utils.AzureProperties;

/*
 * Azure Functions with Blob Trigger.
 */
public class ThumbnailFunction {
	
	private static final String STORAGE_CONNECTION_STRING = System.getenv(AzureProperties.STORAGE_CONNECTION_STRING);
	
	private static final BlobContainerClient thumbnailContainer = new BlobContainerClientBuilder()
			.connectionString(STORAGE_CONNECTION_STRING)
			.containerName("thumbnails")
			.buildClient();
	
	@FunctionName("Thumbnail")
	public void setThumbnail(
			@BlobTrigger(
				name = "blobThumbnail",
				dataType = "binary",
				path = "images",
				connection = "STORAGE_CONNECTION_STRING")
			byte[] content,
			final ExecutionContext context) throws ImageException {
		byte[] compressedContents = new IVCompressor().resizeImage(content, ImageFormats.JPG, ResizeResolution.R720P);
		thumbnailContainer.getBlobClient(Hash.of(compressedContents)).upload(BinaryData.fromBytes(compressedContents), true);
	}
	
}

