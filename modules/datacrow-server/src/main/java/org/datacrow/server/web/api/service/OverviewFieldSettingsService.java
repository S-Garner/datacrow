package org.datacrow.server.web.api.service;

import java.util.LinkedList;
import java.util.List;

import org.datacrow.core.DcRepository;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.utilities.definitions.WebOverviewFieldDefinition;
import org.datacrow.core.utilities.definitions.WebOverviewFieldDefinitions;
import org.datacrow.server.web.api.model.OverviewFieldSetting;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/overviewfieldsettings")
public class OverviewFieldSettingsService extends DataCrowApiService {
	
    @GET
    @Path("/{moduleIndex}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OverviewFieldSetting> getFieldSettings(
    		@HeaderParam("authorization") String token,
    		@PathParam("moduleIndex") int moduleIndex) {
    	
    	checkAuthorization(token);
    	return getOverviewFieldSettings(moduleIndex);
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(
    		@HeaderParam("authorization") String token,
    		@HeaderParam("moduleIndex") int moduleIndex,
    		List<OverviewFieldSetting> fieldSettings) {
    	
    	checkAuthorization(token);
    	
    	WebOverviewFieldDefinitions definitions = new WebOverviewFieldDefinitions(moduleIndex);
    	
    	for (OverviewFieldSetting fs : fieldSettings)
    		definitions.add(
    				new WebOverviewFieldDefinition(moduleIndex, fs.getFieldIdx(), fs.isEnabled()));
    	
    	DcModules.get(moduleIndex).getSettings().set(
    			DcRepository.ModuleSettings.stWebOverviewFieldDefinitions, definitions);
    	
    	// save changes to disk
    	DcModules.get(moduleIndex).getSettings().save();
    	
        return Response.ok().build();
    }
    
    private List<OverviewFieldSetting> getOverviewFieldSettings(int moduleIndex) {
    	
    	List<OverviewFieldSetting> settings = new LinkedList<OverviewFieldSetting>();
    
    	DcModule module = DcModules.get(moduleIndex);
    	
    	WebOverviewFieldDefinitions definitions = (WebOverviewFieldDefinitions)
    			module.getSetting(
    					DcRepository.ModuleSettings.stWebOverviewFieldDefinitions);

    	int order = 0;
    	for (WebOverviewFieldDefinition definition : definitions.getDefinitions()) {
    		if (module.getField(definition.getFieldIdx()).isEnabled())
    			settings.add(new OverviewFieldSetting(definition, order++));
    	}
    	
    	return settings;
    }
}
