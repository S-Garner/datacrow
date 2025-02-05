package org.datacrow.server.web.api.service;

import java.util.Collection;
import java.util.LinkedList;

import org.datacrow.server.data.PictureManager;
import org.datacrow.server.web.api.model.Picture;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/pictures")
public class PictureService extends DataCrowApiService {

	@GET
    @Path("/{itemID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Picture> getPictures(
    		@HeaderParam("authorization") String token,
    		@PathParam("itemID") String ID) {

		Collection<Picture> pictures = new LinkedList<Picture>();
		
    	checkAuthorization(token);
    	for (org.datacrow.core.pictures.Picture p : PictureManager.getInstance().getPictures(ID)) {
    		pictures.add(new Picture(p.getObjectID(), p.getUrl(), p.getThumbnailUrl(), p.getFilename()));
    	}
    	
    	return pictures;
    }
	
	@Path("/{itemID}/{number}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("itemID") String itemID, @PathParam("number") String number) {
		
		PictureManager pm = PictureManager.getInstance();
		
		for (org.datacrow.core.pictures.Picture pic : pm.getPictures(itemID)) {
			if (pic.getFilename().endsWith(number + ".jpg"))
				pm.deletePicture(pic);
		}

		// TODO: translate / use?
		return Response.ok().entity("Picture has been deleted successfully.").build();
    }
}