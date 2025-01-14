package org.datacrow.server.web.api.service;

import java.util.List;

import org.datacrow.server.web.api.manager.ModuleManager;
import org.datacrow.server.web.api.model.Module;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/modules")
public class ModuleService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Module> getMainModules() {
    	return ModuleManager.getInstance().getMainModules();
    }
}