package org.datacrow.synch.request;

import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.server.requests.IClientRequest;

public class SynchClientRequest implements IClientRequest {
	
	public static final int _REQUEST_LOGIN = 0;
	public static final int _REQUEST_MODULES = 1;

	private int type;
	
	private String clientKey;
	protected String username;
	protected String password;
	
	public SynchClientRequest(int type, SecuredUser su) {
		this.type = type;
		
		if (su != null) {
			this.clientKey = su.getUser().getID();
			this.username = su.getUsername();
			this.password = su.getPassword();
		}
	}
	
	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getClientKey() {
		return clientKey;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public void close() {}
}
