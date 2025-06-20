package org.datacrow.server.web.api.service;

import org.datacrow.core.security.SecuredUser;
import org.datacrow.server.security.SecurityCenter;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/module")
public class ModuleService extends DataCrowApiService {

    @GET
    @Path("/editing_allowed/{moduleIndex}")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean canEdit(
    		@HeaderParam("authorization") String token,
    		@PathParam("moduleIndex") int moduleIndex) {
    	
    	checkAuthorization(token);
    	SecuredUser su = SecurityCenter.getInstance().getUser(token);
    	return su.isEditingAllowed(moduleIndex);
    }
}