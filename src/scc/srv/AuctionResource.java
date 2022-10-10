package scc.srv;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import scc.data.User;
import scc.data.UserDAO;

@Path("/auction")
public class AuctionResource {
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User createAuction(User user) {
		return null;
	}
	
}
