package org.datacrow.server.web.api.service;

import org.datacrow.server.web.api.manager.ItemManager;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/item")
public class ItemService {

    @GET
    @Path("/{moduleIndex}/{itemID}")
    @Produces(MediaType.APPLICATION_JSON)
    public org.datacrow.server.web.api.model.Item getItem(@PathParam("moduleIndex") Long id, @PathParam("itemID") String ID) {
        return ItemManager.getInstance().getItem(id.intValue(), ID);
    }
}