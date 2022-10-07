package scc.srv;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import scc.data.CosmosDBLayer;
import scc.data.User;
import scc.data.UserDAO;

/**
 * Resource for managing user database.
 */
@Path("/user")
public class UserResource {
	
	private final CosmosDBLayer db = CosmosDBLayer.getInstance();
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User createUser(User user) {
		return new User(db.putUser(new UserDAO(user)).getItem());
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser(@PathParam("id") String id) {
		return new User(db.getUserById(id).stream().toList().get(0));
	}
	
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Object deleteUser(@PathParam("id") String id) {
		return new User((UserDAO) db.delUserById(id).getItem());
	}
	
}