package org.datacrow.server.web.api.service;

import org.datacrow.server.web.api.manager.ItemManager;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/items")
public class ItemsService {

    @GET
    @Path("/{moduleIndex}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getItemsForModule(@PathParam("moduleIndex") Long id) {
      	 return Response
 	            .status(200)
 	            .header("Access-Control-Allow-Origin", "*")
 	            .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
 	            .header("Access-Control-Allow-Credentials", "true")
 	            .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
 	            .header("Access-Control-Max-Age", "1209600")
 	            .entity(ItemManager.getInstance().getItems(id.intValue()))
 	            .build();
    }
    
    @GET
    @Path("/{moduleIndex}/{searchTerm}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getItemsForModule(@PathParam("moduleIndex") Long id, @PathParam("searchTerm") String search) {
     	 return Response
  	            .status(200)
  	            .header("Access-Control-Allow-Origin", "*")
  	            .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
  	            .header("Access-Control-Allow-Credentials", "true")
  	            .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
  	            .header("Access-Control-Max-Age", "1209600")
  	            .entity(ItemManager.getInstance().getItems(id.intValue(), search))
  	            .build();        
    }
}