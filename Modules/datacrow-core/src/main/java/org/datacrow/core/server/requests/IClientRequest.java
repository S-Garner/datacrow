package org.datacrow.core.server.requests;

public interface IClientRequest {

	public String getUsername();
	
	public String getPassword();
	
	public String getClientKey();
	
	public int getType();
	
	public void close();
}
