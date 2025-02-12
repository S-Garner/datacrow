package org.datacrow.server.web.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebUser {

	@JsonProperty("username")
	private final String username;
	@JsonProperty("token")
	private final String token;
	@JsonProperty("settings")
	private final Settings settings = new Settings();
	
	public WebUser(String username, String token) {
		this.username = username;
		this.token = token;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String token() {
		return token;
	}
	
	public Settings getSettings() {
		return settings;
	}
}
