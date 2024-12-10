package org.datacrow.server.web.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Reference {
	
	@JsonProperty("name")
	private final String name;
	@JsonProperty("iconUrl")
	private final String iconUrl;
	
	public Reference(String name, String iconUrl) {
		this.name = name;
		this.iconUrl = iconUrl;
	}
	
	public String getIconUrl() {
		return iconUrl;
	}
	
	public String getName() {
		return name;
	}
}