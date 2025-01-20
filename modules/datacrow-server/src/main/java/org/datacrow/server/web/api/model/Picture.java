package org.datacrow.server.web.api.model;

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
	
	public Picture(String objectID, String url, String thumbUrl, String filename) {
		this.objectID = objectID;
		this.url = url;
		this.thumbUrl = thumbUrl;
		this.filename = filename;
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
	
	public String getObjectID() {
		return objectID;
	}
}
