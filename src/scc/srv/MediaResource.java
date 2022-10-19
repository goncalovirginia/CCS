package scc.srv;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import scc.utils.AzureProperties;
import scc.utils.Hash;

import java.util.ArrayList;
import java.util.List;

/**
 * Resource for managing media files, such as images.
 */
@Path("/media")
public class MediaResource {
	
	private static final String STORAGE_CONNECTION_STRING = System.getenv(AzureProperties.STORAGE_CONNECTION_STRING);
	
	private static final BlobContainerClient containerClient = new BlobContainerClientBuilder()
			.connectionString(STORAGE_CONNECTION_STRING)
			.containerName("images")
			.buildClient();
	
	/**
	 * Post a new image. The id of the image is its hash.
	 */
	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public String upload(byte[] contents) {
		String key = Hash.of(contents);
		try {
			containerClient.getBlobClient(key).upload(BinaryData.fromBytes(contents), true);
		}
		catch (Exception ignored) {
		}
		return key;
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
			throw new NotFoundException();
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
