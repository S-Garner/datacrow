package org.datacrow.server.web.api.service;

import java.util.Collection;
import java.util.LinkedList;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.server.data.AttachmentManager;
import org.datacrow.server.web.api.model.Attachment;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
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
