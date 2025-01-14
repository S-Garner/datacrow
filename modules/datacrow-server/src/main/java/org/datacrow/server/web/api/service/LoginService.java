package org.datacrow.server.web.api.service;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.server.security.SecurityCenter;
import org.datacrow.server.web.api.model.WebUser;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/login")
public class LoginService {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(LoginService.class.getName());

    @GET
    @Path("/{username}/{password}")
    @Produces(MediaType.APPLICATION_JSON)
    public WebUser login(@PathParam("username") String username, @PathParam("password") String password) {
    	try {
    		SecuredUser user = SecurityCenter.getInstance().login(username, password);
    		
    		if (user != null)
    			return new WebUser(username, user.getSecurityToken().getToken());
    		
    	} catch (Exception e) {
    		logger.debug(e, e);
    	}
    	
    	return null;
    }
}