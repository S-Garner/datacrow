package org.datacrow.synch.service;

public interface ISynchServiceListener {
	
	public void addError(String error, Throwable t);
	
	public void addMessage(String msg);
}
