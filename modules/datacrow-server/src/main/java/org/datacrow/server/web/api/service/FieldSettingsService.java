package org.datacrow.server.web.api.service;

import java.util.LinkedList;
import java.util.List;

import org.datacrow.core.DcRepository;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.utilities.definitions.WebFieldDefinition;
import org.datacrow.core.utilities.definitions.WebFieldDefinitions;
import org.datacrow.server.web.api.model.FieldSetting;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/fieldsettings")
public class FieldSettingsService extends DataCrowApiService {
	
    @GET
    @Path("/{moduleIndex}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FieldSetting> getFieldSettings(
    		@HeaderParam("authorization") String token,
    		@PathParam("moduleIndex") int moduleIndex) {
    	
    	checkAuthorization(token);
    	return getFieldSettings(moduleIndex);
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(
    		@HeaderParam("authorization") String token,
    		@HeaderParam("moduleIndex") int moduleIndex,
    		List<FieldSetting> fieldSettings) {
    	
    	checkAuthorization(token);
    	
    	WebFieldDefinitions definitions = new WebFieldDefinitions(moduleIndex);
    	
    	for (FieldSetting fs : fieldSettings)
    		definitions.add(new WebFieldDefinition(moduleIndex, fs.getFieldIdx()));
    	
    	DcModules.get(moduleIndex).getSettings().set(
    			DcRepository.ModuleSettings.stWebFieldDefinitions, definitions);
    	
    	// save changes to disk
    	DcModules.get(moduleIndex).getSettings().save();
    	
        return Response.ok().build();
    }
    
    private List<FieldSetting> getFieldSettings(int moduleIndex) {
    	
    	List<FieldSetting> settings = new LinkedList<FieldSetting>();
    
    	DcModule module = DcModules.get(moduleIndex);
    	
    	WebFieldDefinitions definitions = (WebFieldDefinitions)
    			module.getSetting(
    					DcRepository.ModuleSettings.stWebFieldDefinitions);

    	int order = 0;
    	for (WebFieldDefinition definition : definitions.getDefinitions()) {
    		if (module.getField(definition.getFieldIdx()).isEnabled())
    			settings.add(new FieldSetting(definition, order++));
    	}
    	
    	return settings;
    }
}
