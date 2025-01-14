package org.datacrow.server.web.api.service;

import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.server.security.SecurityCenter;

import jakarta.ws.rs.NotAuthorizedException;

public class DataCrowApiService {

	protected void checkAuthorization(String token) {
		if (CoreUtilities.isEmpty(token) || !SecurityCenter.getInstance().isLoggedIn(token)) {
			throw new NotAuthorizedException("Unauthorized request - missing a valid security token.");
		}
	}
}
