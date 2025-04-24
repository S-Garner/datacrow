package org.datacrow.server.web.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebUser {

	@JsonProperty("username")
	private final String username;
	@JsonProperty("token")
	private final String token;
	@JsonProperty("settings")
	private final Settings settings = new Settings();
	@JsonProperty("admin")
	private final boolean admin;
	@JsonProperty("canEditAttachments")
	private final boolean canEditAttachments;
	@JsonProperty("canEditPictures")
	private final boolean canEditPictures;
	
	public WebUser(String username, String token, boolean admin, boolean canEditAttachments, boolean canEditPictures) {
		this.username = username;
		this.token = token;
		this.admin = admin;
		
		this.canEditAttachments = canEditAttachments || admin;
		this.canEditPictures = canEditPictures || admin;;
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
