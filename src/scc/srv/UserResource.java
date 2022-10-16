package scc.srv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;
import scc.data.Auction;
import scc.data.CosmosDBLayer;
import scc.data.User;
import scc.data.UserDAO;

/**
 * Resource for managing user database.
 */
@Path("/user")
public class UserResource {
	
	private static final String USERS = "users";
	private final CosmosDBLayer users = CosmosDBLayer.getInstance();
	
	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User createUser(User user) {
		users.putUser(new UserDAO(user));
		User dbUser = getUser(user.getId());
		return RedisCache.writeToHashmap(USERS, dbUser.getId(), dbUser);
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser(@PathParam("id") String id) {
		User cacheResult = RedisCache.readFromHashmap(USERS, id, User.class);
		
		if (cacheResult != null) {
			return cacheResult;
		}
		
		UserDAO dbUser = users.getUserById(id);
		
		if (dbUser == null) {
			throw new NotFoundException();
		}
		
		return RedisCache.writeToHashmap(USERS, id, new User(dbUser));
	}
	
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User deleteUser(@PathParam("id") String id) {
		RedisCache.getCachePool().getResource().hdel(USERS, id);
		Object dbUser = users.delUserById(id).getItem();
		
		if (dbUser == null) {
			throw new NotFoundException();
		}
		
		return new User((UserDAO) dbUser);
	}
	
}
