package org.datacrow.server.web.api.service;

import java.util.Map;
import java.util.stream.Collectors;

import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.resources.DcLanguageResource;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.utilities.CoreUtilities;

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
			            .filter(entry -> entry.getKey().startsWith("lbl") || entry.getKey().startsWith("msg") || entry.getKey().startsWith("sys"))
			            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			
			// add system resources where needed
			for (DcModule module : DcModules.getAllModules()) {
				
				if (module.isTopModule() || 
                    module.isChildModule() || 
                    module.getType() == DcModule._TYPE_PROPERTY_MODULE || 
                    module.isAbstract()) {
                    
					if (!CoreUtilities.isEmpty(module.getLabel()) && CoreUtilities.isEmpty(resources.get( module.getModuleResourceKey())))
                    	filteredResources.put(module.getModuleResourceKey(), module.getSystemLabel());

                    if (!CoreUtilities.isEmpty(module.getObjectName()) && CoreUtilities.isEmpty(resources.get(module.getItemResourceKey())))
                    	filteredResources.put(module.getItemResourceKey(), module.getSystemObjectName());

                    if (!CoreUtilities.isEmpty(module.getObjectNamePlural()) && CoreUtilities.isEmpty(resources.get(module.getItemPluralResourceKey())))
                    	filteredResources.put(module.getItemPluralResourceKey(), module.getSystemObjectNamePlural());

                    for (DcField field : module.getFields()) {

                    	String label = "";
                    	
                    	if (!CoreUtilities.isEmpty(field.getDefinition().getLabel())) {
                    		// protect user overwritten values from the global configuration - they are not language bound
                    		label = field.getDefinition().getLabel();
                    	} else if (!CoreUtilities.isEmpty(field.getLabel()) && CoreUtilities.isEmpty(resources.get(field.getResourceKey()))) {
                    		label = field.getOriginalLabel();
                        } else if (!CoreUtilities.isEmpty(resources.get(field.getResourceKey()))) {
                        	label = resources.get(field.getResourceKey());
                        }
                    	
                    	// catch all - just in case
                    	if (label.startsWith("sys") || CoreUtilities.isEmpty(label))
                    		label = field.getLabel();
                    	
                    	filteredResources.put(field.getResourceKey(), label);
                    }
				}
			}
		
			return filteredResources;
			
		} else {
			
			return null;
			
		}
    }
}
