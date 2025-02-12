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
    		@PathParam("moduleIndex") Long id, 
    		@PathParam("itemID") String ID) {

    	checkAuthorization(token);
    	
    	SecuredUser su = SecurityCenter.getInstance().getUser(token);
    	return ItemManager.getInstance().getItem(su, id.intValue(), ID);
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(@HeaderParam("authorization") String token, Map<Object, Object> data) {
    	
    	checkAuthorization(token);
    	
    	@SuppressWarnings("unchecked")
		Map<Object, Object> payload = (Map<Object, Object>) data.get("payload");
    	
    	String id = (String) payload.get("inputfield-0");
    	
    	boolean isNew = CoreUtilities.isEmpty(id);
    		
    	int moduleIdx = ((Integer) data.get("module")).intValue();
    	
    	DcObject dco = isNew ? 
    			DcModules.get(moduleIdx).getItem() : 
    				DcConfig.getInstance().getConnector().getItem(moduleIdx, id);

    	try {
    		ItemManager.getInstance().saveItem(payload, dco, isNew);
    	} catch (ValidationException ve) {
    		return Response.status(Response.Status.BAD_REQUEST).entity(ve.getMessage()).build();
    	}
    	
        return Response.ok().entity(dco.getID()).build();
    }
}