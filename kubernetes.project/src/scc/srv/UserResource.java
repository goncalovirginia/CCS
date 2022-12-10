package scc.srv;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import org.bson.Document;
import scc.cache.RedisLayer;
import scc.data.MongoDBCollection;
import scc.data.MongoDBLayer;
import scc.data.User;
import scc.data.UserDAO;

import java.util.Arrays;
import java.util.List;

/**
 * Resource for managing user database.
 */
@Path("/user")
public class UserResource {
	
	private final MongoDBLayer db = new MongoDBLayer();
	
	@Context
	ResourceContext resourceContext;
	
	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User createUser(User user) {
		validateUser(user);
		db.insertDocument(MongoDBCollection.USERS, user.toDocument());
		return RedisLayer.putUser(new User(db.getDocument(MongoDBCollection.USERS, user.toDocument())));
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser(@PathParam("id") String id) {
		User cacheResult = RedisLayer.getUser(id);
		return cacheResult != null ? cacheResult : new User(db.getDocument(MongoDBCollection.USERS, new Document().append("id", id)));
	}
	
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User deleteUser(@CookieParam("scc:session") Cookie session, @PathParam("id") String id) {
		AccessControl.checkSessionCookie(session, id);
		RedisLayer.delUser(id);
		db.deleteDocument(MongoDBCollection.USERS, new Document().append("id", id));
		return new User(id);
	}
	
	private void validateUser(User user) {
		List<String> list = Arrays.asList("", null);
		
		if (list.contains(user.getId())) {
			throw new IllegalArgumentException("User id must not be empty!");
		}
		if (list.contains(user.getPhotoId())) {
			throw new IllegalArgumentException("User photo ID must not be empty!");
		}
		if (list.contains(user.getName())) {
			throw new IllegalArgumentException("User name must not be empty!");
		}
		if (list.contains(user.getPwd())) {
			throw new IllegalArgumentException("User password time must not be empty!");
		}
		resourceContext.getResource(MediaResource.class).fileExists(user.getPhotoId());
	}
	
}
