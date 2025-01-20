package org.datacrow.server.web.api.service;

import java.util.Collection;
import java.util.LinkedList;

import org.datacrow.server.data.PictureManager;
import org.datacrow.server.web.api.model.Picture;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/pictures")
public class PictureService extends DataCrowApiService {

	@GET
    @Path("/{itemID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Picture> getItem(
    		@HeaderParam("authorization") String token,
    		@PathParam("itemID") String ID) {

		Collection<Picture> pictures = new LinkedList<Picture>();
		
    	checkAuthorization(token);
    	for (org.datacrow.core.pictures.Picture p : PictureManager.getInstance().getPictures(ID)) {
    		pictures.add(new Picture(p.getObjectID(), p.getUrl(), p.getThumbnailUrl(), p.getFilename()));
    	}
    	
    	return pictures;
    }
	
}