package scc.srv;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import scc.cache.RedisLayer;
import scc.data.CosmosDBLayer;
import scc.data.User;
import scc.data.UserDAO;

/**
 * Resource for managing user database.
 */
@Path("/user")
public class UserResource {
	
	private final CosmosDBLayer db = CosmosDBLayer.getInstance();
	
	@Context
	ResourceContext resourceContext;
	
	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User createUser(User user) {
		resourceContext.getResource(MediaResource.class).fileExists(user.getPhotoId());
		User dbUser = new User(db.putUser(new UserDAO(user)).getItem());
		return RedisLayer.putUser(dbUser);
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser(@PathParam("id") String id) {
		User cacheResult = RedisLayer.getUser(id);
		return cacheResult != null ? cacheResult : new User(db.getUserById(id));
	}
	
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User deleteUser(@PathParam("id") String id) {
		RedisLayer.delUser(id);
		Object dbUser = db.delUserById(id).getItem();
		
		if (dbUser == null) {
			throw new NotFoundException();
		}
		
		return new User((UserDAO) dbUser);
	}
	
	private void validateUser(User user) {
	
	}
	
}
