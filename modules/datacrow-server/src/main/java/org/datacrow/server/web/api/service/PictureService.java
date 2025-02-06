package org.datacrow.server.web.api.service;

import java.io.File;
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

@Path("/pictures")
public class PictureService extends DataCrowApiService {

	private static final int _DIRECTION_UP = 0;
	private static final int _DIRECTION_DOWN = 1;
	
	@GET
    @Path("/{itemID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Picture> getPictures(
    		@HeaderParam("authorization") String token,
    		@PathParam("itemID") String itemID) {

		checkAuthorization(token);
    	return getPictures(itemID);
    }

	@GET
    @Path("/movedown/{itemID}/{number}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Picture> moveDown(
    		@HeaderParam("authorization") String token,
    		@PathParam("itemID") String itemID,
    		@PathParam("number") int number) {

		checkAuthorization(token);
		movePictures(itemID, number, _DIRECTION_DOWN);
    	return getPictures(itemID);

    }
	
	@GET
    @Path("/moveup/{itemID}/{number}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Picture> moveUp(
    		@HeaderParam("authorization") String token,
    		@PathParam("itemID") String itemID,
    		@PathParam("number") int number) {

		checkAuthorization(token);
		movePictures(itemID, number, _DIRECTION_UP);
    	return getPictures(itemID);
    }
	
	private void movePictures(String itemID, int number, int direction) {
		PictureManager pm = PictureManager.getInstance();
		LinkedList<String> filenames = new LinkedList<String>();
				
		Collection<org.datacrow.core.pictures.Picture> 
					currentPictures = pm.getPictures(itemID);
		
		String filename;
		String move = null;
		for (org.datacrow.core.pictures.Picture pic : currentPictures) {
			filename = new File(pic.getFilename()).getName();
			
			if (filename.endsWith(number + ".jpg"))
				move = filename;
			else
				filenames.add(filename);
		}
		
		if (move != null) {
			
			if (direction == _DIRECTION_UP)
				filenames.add(number - 2, move);
			else
				filenames.add(number, move);
			
			pm.savePictureOrder(itemID, filenames);
		}
	}
	
	@Path("/{itemID}/{number}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Picture> delete(
    		@HeaderParam("authorization") String token, 
    		@PathParam("itemID") String itemID, 
    		@PathParam("number") String number) {
		
		checkAuthorization(token);
		
		PictureManager pm = PictureManager.getInstance();
		org.datacrow.core.pictures.Picture pic = getPicture(itemID, number);
		
		if (pic != null)
			pm.deletePicture(pic);
		
		Collection<Picture> pictures = new LinkedList<Picture>();
    	for (org.datacrow.core.pictures.Picture p : PictureManager.getInstance().getPictures(itemID))
    		pictures.add(new Picture(p.getObjectID(), p.getUrl(), p.getThumbnailUrl(), p.getFilename()));

		return getPictures(itemID);
    }
	
	private LinkedList<Picture> getPictures(String itemID) {
		LinkedList<Picture> pictures = new LinkedList<Picture>();
    	for (org.datacrow.core.pictures.Picture p : PictureManager.getInstance().getPictures(itemID))
    		pictures.add(new Picture(p.getObjectID(), p.getUrl(), p.getThumbnailUrl(), p.getFilename()));
		
    	return pictures;
	}
	
	private org.datacrow.core.pictures.Picture getPicture(String itemID, String number) {
		
		PictureManager pm = PictureManager.getInstance();
		
		for (org.datacrow.core.pictures.Picture pic : pm.getPictures(itemID)) {
			if (pic.getFilename().endsWith(number + ".jpg"))
				return pic;
		}
		
		return null;
	}	
}