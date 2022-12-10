package scc.srv;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import scc.cache.RedisLayer;
import scc.data.*;
import scc.utils.Hash;

import jakarta.ws.rs.core.Response;
import java.util.UUID;

@Path("/user")
public class AccessControl {
	
	@POST
	@Path("/auth")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response authLogin(Login login) {
		if (login == null || login.userId() == null || login.pwd() == null) {
			throw new IllegalArgumentException("Empty login credentials.");
		}
			
		User user = RedisLayer.getUser(login.userId());
		String pw = Hash.of(login.pwd());

		if(user != null && !user.getPwd().equals(pw)){
			throw new NotAuthorizedException("Incorrect login credentials");
		}else{
			UserDAO usr = CosmosDBLayer.getInstance().getUserByIdForAC(login.userId());
			if(usr == null || usr != null && !usr.getPwd().equals(pw)){
				throw new NotAuthorizedException("Incorrect login credentials");
			}
		}
		
		String uuid = UUID.randomUUID().toString();
		NewCookie cookie = new NewCookie.Builder("scc:session")
				.value(uuid)
				.path("/")
				.comment("sessionid")
				.maxAge(3600)
				.secure(false)
				.httpOnly(true)
				.build();
		
		Session s = new Session(uuid, login.userId());
		boolean redisActive = RedisLayer.putSession(s);
		
		if (!redisActive)
			CosmosDBLayer.getInstance().putSession(new SessionDAO(s));

		return Response.ok().cookie(cookie).build();
	}

	@DELETE
	@Path("/auth")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response authLogout(Login login) {
		if (login == null || login.userId() == null || login.pwd() == null) {
			throw new IllegalArgumentException("Empty logout credentials.");
		}
			
		User user = RedisLayer.getUser(login.userId());

		if (!(user != null && user.getPwd().equals(Hash.of(login.pwd()))) &&
				!CosmosDBLayer.getInstance().getUserById(login.userId()).getPwd().equals(Hash.of(login.pwd()))) {
			throw new NotAuthorizedException("Incorrect logout credentials");
		}

		Session s = new Session(null, login.userId());

		boolean check = RedisLayer.delSession(s);

		if(!check)
			CosmosDBLayer.getInstance().delSession(new SessionDAO(s));

		return Response.ok().build();

	}
	
	public static Session checkSessionCookie(Cookie session, String id) throws NotAuthorizedException {
		if (session == null || session.getValue() == null) {
			throw new NotAuthorizedException("No session initialized");
		}
		
		Session s = RedisLayer.getSession(session.getValue());
		
		if (s == null) {
			try {
				s = new Session(CosmosDBLayer.getInstance().getSession(session.getValue()));
			}
			catch (NotFoundException ignored) {}
		}
		
		if (s == null || s.getUser() == null || s.getUser().length() == 0) {
			throw new NotAuthorizedException("No valid session initialized");
		}
		if (!s.getUser().equals(id) && !s.getUser().equals("admin")) {
			throw new NotAuthorizedException("Invalid user : " + s.getUser());
		}
		
		return s;
	}
	
}
