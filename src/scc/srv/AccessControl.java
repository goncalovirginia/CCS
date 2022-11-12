

package scc.srv;

import java.util.UUID;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import scc.data.Login;
import scc.data.Session;
import scc.cache.RedisLayer;

@Path("/user")
public class AccessControl {

    @POST
    @Path("/auth")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response auth(Login user) {
        boolean pwdOk = false;
        
        if(user.getPwd() != null){
            pwdOk = true;
        }

        if(pwdOk) {
            String uid = UUID.randomUUID().toString();
            /*
            NewCookie cookie = new NewCookie.Builder("scc:session")
            .value(uid)
            .path("/")
            .comment("sessionid")
            .maxAge(3600)
            .secure(false)
            .httpOnly(true)
            .build();
            */

            NewCookie cookie = new NewCookie("scc:session", uid, 
            "/", null, 1, "sessionid", 
            3600, false);

            RedisLayer.putSession(new Session(uid, user.getUser()));
            return Response.ok().cookie(cookie).build();
        } else
            throw new NotAuthorizedException("Incorrect login");
    }


    public Session checkCookieUser(Cookie session, String id) throws NotAuthorizedException {
        
        if (session == null || session.getValue() == null)
            throw new NotAuthorizedException("No session initialized");

        Session s;
        try {
            s = RedisLayer.getSession(session.getValue());
        } catch (Exception e) {
            throw new NotAuthorizedException("No valid session initialized");
        }

        if (s == null || s.getUser() == null || s.getUser().length() == 0)
            throw new NotAuthorizedException("No valid session initialized");

        if (!s.getUser().equals(id) && !s.getUser().equals("admin"))
            throw new NotAuthorizedException("Invalid user : " + s.getUser());

        return s;
    }
    
}