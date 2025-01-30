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

@Path("/children")
public class ChildrenService extends DataCrowApiService {

    @GET
    @Path("/{moduleIndex}/{itemID}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Item> getItem(
    		@HeaderParam("authorization") String token,
    		@PathParam("moduleIndex") Long id, 
    		@PathParam("itemID") String itemID) {

    	checkAuthorization(token);
    	return ItemManager.getInstance().getChildren(id.intValue(), itemID);
    }
}