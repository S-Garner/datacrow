package org.datacrow.server.web.api.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.LinkedList;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.server.data.AttachmentManager;
import org.datacrow.server.web.api.model.Attachment;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

@Path("/attachments")
public class AttachmentService extends DataCrowApiService {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(AttachmentService.class.getName());
	
    @GET
    @Path("/{itemID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Attachment> getAttachments(
    		@HeaderParam("authorization") String token,
    		@PathParam("moduleIndex") Long id, 
    		@PathParam("itemID") String itemID) {

    	checkAuthorization(token);
    	return getAttachments(itemID);
    }
    
	@POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadAttachment(
    		@HeaderParam("itemID") String itemID,
    		@HeaderParam("fileName") String fileName,
    		@HeaderParam("authorization") String token,
    		InputStream fileInputStream) {
        
		checkAuthorization(token);
		
		try {
        	
			File file = new File(CoreUtilities.getTempFolder(), fileName);
			file.deleteOnExit();
			
            try (FileOutputStream out = new FileOutputStream(file)) {
                Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            
			org.datacrow.core.attachments.Attachment
				attachment = new org.datacrow.core.attachments.Attachment(itemID, file);

			attachment.setData(CoreUtilities.readFile(file));
			AttachmentManager.getInstance().saveAttachment(attachment);
            
			file.delete();
			
            return Response.ok().entity(getAttachments(itemID)).build();
            
        } catch (Exception e) {
            logger.error("There was an error in uploading the attachment", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("msgErrorUploadingAttachment").build();
        }
    }    
    
    
	@Path("/{itemID}/{name}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Attachment> delete(
    		@HeaderParam("authorization") String token, 
    		@PathParam("itemID") String itemID, 
    		@PathParam("name") String name) {
		
		checkAuthorization(token);
		
		AttachmentManager am = AttachmentManager.getInstance();
		
		org.datacrow.core.attachments.Attachment 
			attachment = getAttachment(itemID, name);
		
		if (attachment != null)
			am.deleteAttachment(attachment);
		
		return getAttachments(itemID);
    }
	
	@GET
	@Path("/download/{itemID}/{name}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response download(
			@HeaderParam("authorization") String token,
			@PathParam("itemID") String itemID,
			@PathParam("name") String name) {
		
		checkAuthorization(token);
		
		org.datacrow.core.attachments.Attachment attachment = getAttachment(itemID, name);
		
		Response response;
		try {
			attachment.storeLocally();

			ResponseBuilder builder = Response.ok(attachment.getLocalFile());
			builder.header("Content-Disposition", "attachment; filename=" + attachment.getLocalFile());
			response = builder.build();
		} catch (Exception e) {
			logger.error("An error occured while sending the request file (" + name + ")", e);
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}

		return response;
	}
	
	private Collection<Attachment> getAttachments(String itemID) {
    	
		AttachmentManager am = AttachmentManager.getInstance();
    	Collection<Attachment> result = new LinkedList<Attachment>();
    	
    	for (org.datacrow.core.attachments.Attachment attachment : am.getAttachments(itemID))
    		result.add(new Attachment(attachment));
    	
    	return result;
	}
	
	
	private org.datacrow.core.attachments.Attachment getAttachment(String itemID, String name) {
		AttachmentManager am = AttachmentManager.getInstance();
		org.datacrow.core.attachments.Attachment attachment = null;
		for (org.datacrow.core.attachments.Attachment a : am.getAttachments(itemID)) {
			if (a.getName().equals(name))
				attachment = a;
		}
		
		return attachment;
	}
}
