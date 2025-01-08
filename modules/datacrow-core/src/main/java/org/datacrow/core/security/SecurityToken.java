package org.datacrow.core.security;

import java.util.Calendar;
import java.util.Date;

import org.datacrow.core.utilities.CoreUtilities;

public class SecurityToken {

	private final String token;
	private final Date createdOn;
	
	public SecurityToken() {
		this.token = CoreUtilities.getUniqueID();
		this.createdOn = Calendar.getInstance().getTime();
	}
	
	public String getToken() {
		return token;
	}
	
	public Date getCreationDate() {
		return createdOn;
	}
	
	public boolean isValid() {
		return true;
	}
	
	public boolean matches(String token) {
		return this.token.equals(token);
	}
}
