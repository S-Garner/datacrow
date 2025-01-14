package org.datacrow.server.web.api.service;

import java.util.List;

import org.datacrow.server.web.api.manager.ItemManager;
import org.datacrow.server.web.api.model.Item;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/items")
public class ItemsService extends DataCrowApiService {

    @GET
    @Path("/{moduleIndex}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Item> getItemsForModule(
    		@HeaderParam("authorization") String token, 
    		@PathParam("moduleIndex") Long id) {
    	
    	checkAuthorization(token);
    	return ItemManager.getInstance().getItems(id.intValue());
    }
    
    @GET
    @Path("/{moduleIndex}/{searchTerm}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Item> getItemsForModule(
    		@HeaderParam("authorization") String token, 
    		@PathParam("moduleIndex") Long id, 
    		@PathParam("searchTerm") String search) {
    	
    	checkAuthorization(token);
    	return ItemManager.getInstance().getItems(id.intValue(), search);    
    }
}