package org.datacrow.server.web.api.model;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Picture {

	@JsonProperty("url")
	private final String url;
	@JsonProperty("thumbUrl")
	private final String thumbUrl;	
	@JsonProperty("objectID")
	private final String objectID;
	@JsonProperty("filename")
	private final String filename;	
	@JsonProperty("order")
	private final int order;	
	
	public Picture(org.datacrow.core.pictures.Picture p) {
		this.objectID = p.getObjectID();
		this.url = p.getUrl();
		this.thumbUrl = p.getThumbnailUrl();
		this.filename = p.getFilename();
		
		String name = new File(filename).getName();
		this.order = Integer.parseInt(name.substring(7, name.lastIndexOf(".")));
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getThumbUrl() {
		return thumbUrl;
	}	
	
	public int getOrder() {
		return order;
	}	
	
	public String getObjectID() {
		return objectID;
	}
}
