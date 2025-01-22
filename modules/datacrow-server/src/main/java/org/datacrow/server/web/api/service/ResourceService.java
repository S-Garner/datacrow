package org.datacrow.server.web.api.service;

import java.util.Map;
import java.util.stream.Collectors;

import org.datacrow.core.resources.DcLanguageResource;
import org.datacrow.core.resources.DcResources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/resources")
public class ResourceService extends DataCrowApiService {
	
    @GET
    @Path("/{lang}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getResources(@PathParam("lang") String lang) {
		DcLanguageResource resources = DcResources.getLanguageResource(lang);
		
		
		if (resources != null) {
			Map<String, String> currentResources = resources.getResourcesMap();
			
			Map<String, String> filteredResources = currentResources.entrySet()
			            .stream()
			            .filter(entry -> entry.getKey().startsWith("lbl") || entry.getKey().startsWith("msg"))
			            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			
			return filteredResources;
			
		} else {
			return null;
		}
    }
}
