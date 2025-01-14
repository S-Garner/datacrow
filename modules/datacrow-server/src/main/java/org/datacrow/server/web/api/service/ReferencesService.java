package org.datacrow.server.web.api.service;

import java.util.ArrayList;
import java.util.List;

import org.datacrow.server.web.api.manager.ModuleManager;
import org.datacrow.server.web.api.manager.ReferenceManager;
import org.datacrow.server.web.api.model.Field;
import org.datacrow.server.web.api.model.Reference;
import org.datacrow.server.web.api.model.References;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/references")
public class ReferencesService {

    @GET
    @Path("/{moduleIdx}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<References> getItemsForModule(@PathParam("moduleIdx") Long moduleIdx) {
        
    	org.datacrow.server.web.api.model.Module webModule = 
    			ModuleManager.getInstance().getModule(moduleIdx.intValue());
    	
    	List<References> allReferences = new ArrayList<References>();
    	List<Reference> references;

    	for (Field field : webModule.getFields()) {
    		if (	field.getType() == Field._DROPDOWN || 
    				field.getType() == Field._MULTIRELATE) {
    			
    			references = ReferenceManager.getInstance().getReferences(field.getReferencedModuleIdx());
    			allReferences.add(new References(field.getReferencedModuleIdx(), references));
    		}
    	}
    	
		return allReferences;    	
    }
}