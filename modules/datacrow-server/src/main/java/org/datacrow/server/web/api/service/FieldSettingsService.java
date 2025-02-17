package org.datacrow.server.web.api.service;

import java.util.LinkedList;
import java.util.List;

import org.datacrow.core.DcRepository;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.utilities.definitions.WebFieldDefinition;
import org.datacrow.core.utilities.definitions.WebFieldDefinitions;
import org.datacrow.server.web.api.model.FieldSetting;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/fieldsettings")
public class FieldSettingsService {
	
    @GET
    @Path("/{moduleIndex}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FieldSetting> getFieldSettings(@PathParam("moduleIndex") int moduleIndex) {
    	List<FieldSetting> settings = new LinkedList<FieldSetting>();
    
    	WebFieldDefinitions definitions = (WebFieldDefinitions)
    			DcModules.get(moduleIndex).getSetting(
    					DcRepository.ModuleSettings.stWebFieldDefinitions);

    	int order = 0;
    	for (WebFieldDefinition definition : definitions.getDefinitions())
    		settings.add(new FieldSetting(definition, order++));
    	
    	return settings;
    }
}
