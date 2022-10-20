package scc.srv;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import scc.cache.RedisCache;
import scc.data.CosmosDBLayer;
import scc.data.User;
import scc.data.UserDAO;

/**
 * Resource for managing user database.
 */
@Path("/user")
public class UserResource {
	
	private static final String USERS = "users";
	private final CosmosDBLayer db = CosmosDBLayer.getInstance();
	
	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User createUser(User user) {
		User dbUser = new User(db.putUser(new UserDAO(user)).getItem());
		return RedisCache.writeToHashmap(USERS, dbUser.getId(), dbUser);
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser(@PathParam("id") String id) {
		User cacheResult = RedisCache.readFromHashmap(USERS, id, User.class);
		return cacheResult != null ? cacheResult :
				RedisCache.writeToHashmap(USERS, id, new User(db.getUserById(id)));
	}
	
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User deleteUser(@PathParam("id") String id) {
		RedisCache.getCachePool().getResource().hdel(USERS, id);
		Object dbUser = db.delUserById(id).getItem();
		
		if (dbUser == null) {
			throw new NotFoundException();
		}
		
		return new User((UserDAO) dbUser);
	}
	
}
