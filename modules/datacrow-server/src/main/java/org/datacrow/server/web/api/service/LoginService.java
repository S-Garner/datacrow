package org.datacrow.server.web.api.service;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.server.security.SecurityCenter;
import org.datacrow.server.web.api.model.WebUser;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/login")
public class LoginService {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(LoginService.class.getName());

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public WebUser login(
    		@HeaderParam("password") String password, 
    		@PathParam("username") String username) {
    	
    	try {
    		SecuredUser user = SecurityCenter.getInstance().login(username, password);
    		
    		if (user != null)
    			return new WebUser(username, user.getSecurityToken().getToken(), user.isAdmin());
    		
    	} catch (Exception e) {
    		logger.debug(e, e);
    	}
    	
    	return null;
    }
}