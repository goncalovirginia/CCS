package scc.srv;

import java.util.UUID;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import scc.data.CosmosDBLayer;
import scc.data.Login;
import scc.data.Session;
import scc.data.SessionDAO;
import scc.cache.RedisLayer;
import scc.utils.Hash;

@Path("/user")
public class AccessControl {

    @POST
    @Path("/auth")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response auth(Login login) {
        if (login == null || login.userId() == null || login.pwd() == null ||
                !RedisLayer.getUser(login.userId()).getPwd().equals(Hash.of(login.pwd())) ||
                !CosmosDBLayer.getInstance().getUserById(login.userId()).getPwd().equals(Hash.of(login.pwd()))) {
            throw new NotAuthorizedException("Incorrect login credentials");
        }
        
        String uuid = UUID.randomUUID().toString();
        NewCookie cookie = new NewCookie("scc:session", uuid,"/", null, 1, "sessionid",3600, false);
        Session s = new Session(uuid, login.userId());
        Boolean flag = RedisLayer.putSession(s);
        if(!flag)
            CosmosDBLayer.getInstance().putSession(new SessionDAO(s));
        return Response.ok().cookie(cookie).build();
    }

    public void checkSessionCookie(Cookie session, String id) throws NotAuthorizedException {
        if (session == null || session.getValue() == null) {
            throw new NotAuthorizedException("No session initialized");
        }

        Session s = RedisLayer.getSession(session.getValue());

        if(s == null){
            s = new Session(CosmosDBLayer.getInstance().getSession(session.getValue()));
        }

        if (s.getUser() == null || s.getUser().length() == 0) {
            throw new NotAuthorizedException("No valid session initialized");
        }
        if (!s.getUser().equals(id) && !s.getUser().equals("admin")) {
            throw new NotAuthorizedException("Invalid user : " + s.getUser  ());
        }
    }
    
}