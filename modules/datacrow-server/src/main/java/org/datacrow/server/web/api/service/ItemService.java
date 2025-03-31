package org.datacrow.server.web.api.service;

import java.util.Map;

import org.datacrow.core.DcConfig;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.ValidationException;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.server.security.SecurityCenter;
import org.datacrow.server.web.api.manager.ItemManager;
import org.datacrow.server.web.api.model.Item;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/item")
public class ItemService extends DataCrowApiService {

    @GET
    @Path("/{moduleIndex}/{itemID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Item getItem(
    		@HeaderParam("authorization") String token,
    		@HeaderParam("viewMode") boolean viewMode,
    		@PathParam("moduleIndex") int moduleIndex, 
    		@PathParam("itemID") String ID) {

    	checkAuthorization(token);
    	
    	SecuredUser su = SecurityCenter.getInstance().getUser(token);
    	return ItemManager.getInstance().getItem(su, moduleIndex, ID, viewMode);
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(
    		@HeaderParam("authorization") String token, 
    		@HeaderParam("moduleIndex") int moduleIndex, 
    		@HeaderParam("itemID") String itemID,
    		@HeaderParam("parentID") String parentID,
    		Map<Object, Object> data) {
    	
    	checkAuthorization(token);
    	
    	boolean isNew = CoreUtilities.isEmpty(itemID);
    	
    	DcObject dco = isNew ? 
    			DcModules.get(moduleIndex).getItem() : 
    				DcConfig.getInstance().getConnector().getItem(moduleIndex, itemID);

    	if (!CoreUtilities.isEmpty(parentID))
    		dco.setValue(dco.getParentReferenceFieldIndex(), parentID);
    	
    	try {
    		ItemManager.getInstance().saveItem(data, dco, isNew);
    	} catch (ValidationException ve) {
    		return Response.status(Response.Status.BAD_REQUEST).entity(ve.getMessage()).build();
    	}
    	
        return Response.ok().entity(dco.getID()).build();
    }
    
    @Path("/{moduleIdx}/{itemID}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
    		@HeaderParam("authorization") String token,
    		@PathParam("moduleIdx") int moduleIdx, 
    		@PathParam("itemID") String itemID) {
    	
    	checkAuthorization(token);
    	
    	SecuredUser su = SecurityCenter.getInstance().getUser(token);
    
    	try {
			ItemManager.getInstance().delete(su, moduleIdx, itemID);
		} catch (ValidationException ve) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ve.getMessage()).build();
		}
	
    	return Response.ok().build();
    }    
}