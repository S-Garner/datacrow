package org.datacrow.server.web.api.service;

import java.util.List;

import org.datacrow.core.security.SecuredUser;
import org.datacrow.server.security.SecurityCenter;
import org.datacrow.server.web.api.manager.ModuleManager;
import org.datacrow.server.web.api.model.Module;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/modules")
public class ModulesService extends DataCrowApiService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Module> getModules(@HeaderParam("authorization") String token) {
    	checkAuthorization(token);
    	
    	SecuredUser su = SecurityCenter.getInstance().getUser(token);
    	return ModuleManager.getInstance().getModules(su);
    }
}