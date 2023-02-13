package org.datacrow.synch.response;

import org.datacrow.core.security.SecuredUser;

public class SynchServerLoginResponse extends SynchServerResponse {
	
	private static final long serialVersionUID = 1L;

	private SecuredUser su;
	
	public SynchServerLoginResponse(SecuredUser su) {
	    super(_RESPONSE_LOGIN);
	    
		this.su = su;
	}
	
	public SecuredUser getUser() {
		return su;
	}
}
