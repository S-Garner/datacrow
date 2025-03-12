package org.datacrow.server.web.api.service;

import java.util.ArrayList;
import java.util.List;

import org.datacrow.core.security.SecuredUser;
import org.datacrow.server.security.SecurityCenter;
import org.datacrow.server.web.api.manager.ModuleManager;
import org.datacrow.server.web.api.manager.ReferenceManager;
import org.datacrow.server.web.api.model.Field;
import org.datacrow.server.web.api.model.Reference;
import org.datacrow.server.web.api.model.References;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/references")
public class ReferencesService extends DataCrowApiService {

    @GET
    @Path("/{moduleIdx}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<References> getReferences(
    		@HeaderParam("authorization") String token,
    		@PathParam("moduleIdx") int moduleIdx) {
        
    	checkAuthorization(token);
    	
    	SecuredUser su = SecurityCenter.getInstance().getUser(token);

    	org.datacrow.server.web.api.model.Module webModule = 
    			ModuleManager.getInstance().getModule(su, moduleIdx);
    	
    	List<References> allReferences = new ArrayList<References>();
    	List<Reference> references;

    	for (Field field : webModule.getFields()) {
    		if (	field.getType() == Field._TAGFIELD || 
    				field.getType() == Field._DROPDOWN || 
    				field.getType() == Field._MULTIRELATE) {
    			
    			references = ReferenceManager.getInstance().getReferences(field.getReferencedModuleIdx());
    			allReferences.add(new References(field.getReferencedModuleIdx(), references));
    		}
    	}
    	
		return allReferences;    	
    }
    
    @GET
    @Path("/{moduleIdx}/{itemID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Reference getReference(
    		@HeaderParam("authorization") String token,
    		@PathParam("moduleIdx") int moduleIdx,
    		@PathParam("itemID") String itemID) {
        
    	checkAuthorization(token);
    	return ReferenceManager.getInstance().getReference(moduleIdx, itemID);
    }    
}