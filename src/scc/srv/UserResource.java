package scc.srv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import redis.clients.jedis.Jedis;
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
	private final CosmosDBLayer users = CosmosDBLayer.getInstance();
	private static final ObjectMapper mapper = new ObjectMapper();
	
	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User createUser(User user) throws JsonProcessingException {
		users.putUser(new UserDAO(user));
		return writeToCache(getUser(user.getId()));
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser(@PathParam("id") String id) throws JsonProcessingException {
		String cacheResult = RedisCache.getCachePool().getResource().hget(USERS, id);
		
		if (cacheResult != null) {
			return mapper.readValue(cacheResult, User.class);
		}
		
		return writeToCache(new User(users.getUserById(id).stream().toList().get(0)));
	}
	
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User deleteUser(@PathParam("id") String id) {
		RedisCache.getCachePool().getResource().hdel(USERS, id);
		return new User((UserDAO) users.delUserById(id).getItem());
	}
	
	private User writeToCache(User user) {
		try {
			RedisCache.getCachePool().getResource().hset(USERS, user.getId(), mapper.writeValueAsString(user));
		}
		catch (Exception ignored) {
		}
		return user;
	}
	
}
