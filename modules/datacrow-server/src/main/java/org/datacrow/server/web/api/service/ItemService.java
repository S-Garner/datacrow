package org.datacrow.server.web.api.service;

import org.datacrow.server.web.api.manager.ItemManager;
import org.datacrow.server.web.api.model.Item;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
    	return ItemManager.getInstance().getItem(id.intValue(), ID);
    }
}