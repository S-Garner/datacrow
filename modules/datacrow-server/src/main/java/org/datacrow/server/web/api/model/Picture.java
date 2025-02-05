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
	private final String order;	
	
	
	public Picture(String objectID, String url, String thumbUrl, String filename) {
		this.objectID = objectID;
		this.url = url;
		this.thumbUrl = thumbUrl;
		this.filename = filename;
		
		String name = new File(filename).getName();
		this.order = name.substring(7, name.lastIndexOf("."));
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
	
	public String getOrder() {
		return order;
	}	
	
	public String getObjectID() {
		return objectID;
	}
}
