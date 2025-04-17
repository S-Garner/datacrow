package org.datacrow.server.web.api.service;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/status")
public class StatusService extends DataCrowApiService {
	
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getStatus() {
		return "API status: OK";
    }
}
