package org.datacrow.server.web.api.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Attachment {

	@JsonProperty("objectID")
	private final String objectID;
	@JsonProperty("name")
	private final String name;
	@JsonProperty("size")
	private final long size;
	@JsonProperty("created")
	private final Date created;
	@JsonProperty("displayName")
	private final String displayName;
	
	public Attachment(org.datacrow.core.attachments.Attachment attachment) {
		
		this.objectID = attachment.getObjectID();
		this.name = attachment.getName();
		this.size = attachment.getSize();
		this.created = attachment.getCreated();
		this.displayName = attachment.toString();
	}
	
	public String getObjectID() {
		return objectID;
	}
	
	public String getName() {
		return name;
	}

	public long getSize() {
		return size;
	}
	
	public Date getCreated() {
		return created;
	}
	
	public String getDisplayName() {
		return displayName;
	}
}
