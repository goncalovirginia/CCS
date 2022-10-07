package scc.srv;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import scc.data.CosmosDBLayer;
import scc.utils.Hash;

import java.util.*;

/**
 * Resource for managing media files, such as images.
 */
@Path("/media")
public class MediaResource {
	
	private static final String STORAGE_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=scc56773;AccountKey=SptJlI8OAt6QVBrEYGwaK1irSSPFC2ZJOQKBl4bTCOwgJH1F0CPtB9jmLxrbtZLmKcE0LlyxaWhV+AStBdrt8A==;EndpointSuffix=core.windows.net";
	
	private static final BlobContainerClient containerClient = new BlobContainerClientBuilder()
			.connectionString(STORAGE_CONNECTION_STRING)
			.containerName("images")
			.buildClient();
	
	/**
	 * Post a new image. The id of the image is its hash.
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public String upload(byte[] contents) {
		try {
			String key = Hash.of(contents);
			containerClient.getBlobClient(key).upload(BinaryData.fromBytes(contents));
			return key;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceUnavailableException();
		}
	}
	
	/**
	 * Return the contents of an image. Throw an appropriate error message if
	 * id does not exist.
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] download(@PathParam("id") String id) {
		try {
			return containerClient.getBlobClient(id).downloadContent().toBytes();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceUnavailableException();
		}
	}
	
	/**
	 * Lists the ids of images stored.
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> list() {
		return new ArrayList<>(containerClient.listBlobs().stream().map(BlobItem::getName).toList());
	}
}
