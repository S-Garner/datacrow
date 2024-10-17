package org.datacrow.server.web.api.service;

import java.util.List;

import org.datacrow.server.web.api.model.Modules;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/modules")
public class ModuleService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<org.datacrow.server.web.api.model.Module> getMainModules() {
        return Modules.getInstance().getMainModules();
    }
}
