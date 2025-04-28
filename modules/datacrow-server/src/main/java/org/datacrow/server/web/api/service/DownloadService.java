package org.datacrow.server.web.api.service;

import java.io.File;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

@Path("/download")
public class DownloadService extends DataCrowApiService {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DownloadService.class.getName());
	
	@GET
	@Path("/{filename}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response download(
			@HeaderParam("authorization") String token,
			@PathParam("filename") String filename) {
		
		checkAuthorization(token);
		
		Response response;
		File file = new File(filename);
		
		if (file.exists() && file.isFile()) {
			try {
				ResponseBuilder builder = Response.ok(new File(filename));
				builder.header("Content-Disposition", "attachment; filename=" + file.getName());
				response = builder.build();
			} catch (Exception e) {
				logger.error("An error occured while sending the request file (" + filename + ")", e);
				return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
			}
		} else {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					"The file does not exist, is not a file or the server does not have access to it.").build();
		}

		return response;
	}
}
