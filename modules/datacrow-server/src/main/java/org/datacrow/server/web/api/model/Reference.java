package org.datacrow.server.web.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Reference {
	
	@JsonProperty("id")
	private final String id;
	@JsonProperty("name")
	private final String name;
	@JsonProperty("iconUrl")
	private final String iconUrl;
	
	public Reference(String id, String name, String iconUrl) {
		this.name = name;
		this.iconUrl = iconUrl;
		this.id = id;
	}
	
	public String getIconUrl() {
		return iconUrl;
	}
	
	public String getName() {
		return name;
	}
	
	public String getId() {
		return id;
	}
}