package scc.srv;

import scc.utils.Hash;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.io.*;
import java.nio.file.Files;

import java.util.ArrayList;
import java.util.List;

import static scc.EnvironmentProperties.BLOB_PATH;

/**
 * Resource for managing media files, such as images.
 */
@Path("/media")
public class MediaResource {
	
	/**
	 * Post a new image. The id of the image is its hash.
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public String upload(byte[] contents) {
		String id = Hash.of(contents);
		try {
			FileOutputStream outputStream = new FileOutputStream(BLOB_PATH + id);
			outputStream.write(contents);
			outputStream.close();
		} catch (Exception ignored) {}

		return id;
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
			File file = new File(BLOB_PATH + id);
			if(!file.exists()){
				throw new NotFoundException();
			}

			return Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Lists the ids of images stored.
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> list() {
		File folder = new File(BLOB_PATH);
		File[] listOfFiles = folder.listFiles();
		List<String> allFiles = new ArrayList<String>();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				allFiles.add(listOfFiles[i].getName());
			}
		  }
		return allFiles;
	}
	
	public void fileExists(String id) {
		try {
			File file = new File(BLOB_PATH + id);
			if(!file.exists()){
				throw new NotFoundException();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
