package org.datacrow.core.security;

import org.datacrow.core.utilities.CoreUtilities;

public class SecurityToken {

	private final String token;
	
	public SecurityToken() {
		this.token = CoreUtilities.getUniqueID();
	}
	
	public String getToken() {
		return token;
	}
	
	public boolean isValid() {
		return true;
	}
	
	public boolean matches(String token) {
		return this.token.equals(token);
	}
}
