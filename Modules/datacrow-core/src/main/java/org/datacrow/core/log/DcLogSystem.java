package org.datacrow.core.log;

public abstract class DcLogSystem {
	
	private boolean debug = false;
	
	public void isDebug(boolean debug) {
		this.debug = debug;
	}
	
	public boolean isDebug() {
		return debug;
	}
	
	public abstract void initialize(boolean debug);
	
	public abstract DcLogger getLogger(String name);
}