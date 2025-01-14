package org.datacrow.server.web.api.service;

import java.util.List;

import org.datacrow.server.web.api.manager.ItemManager;
import org.datacrow.server.web.api.model.Item;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/items")
public class ItemsService {

    @GET
    @Path("/{moduleIndex}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Item> getItemsForModule(@PathParam("moduleIndex") Long id) {
    	return ItemManager.getInstance().getItems(id.intValue());
    }
    
    @GET
    @Path("/{moduleIndex}/{searchTerm}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Item> getItemsForModule(@PathParam("moduleIndex") Long id, @PathParam("searchTerm") String search) {
     	 return ItemManager.getInstance().getItems(id.intValue(), search);    
    }
}