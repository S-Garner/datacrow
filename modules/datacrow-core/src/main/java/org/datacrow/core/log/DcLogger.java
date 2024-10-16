package org.datacrow.core.log;

public abstract class DcLogger {
	
	public DcLogger(String clazz) {}
	
	public abstract boolean isDebugEnabled();
	
	public abstract void error(String msg);
	
	public abstract void error(String msg, Throwable t);
	
	public abstract void error(String msg, String error);
	
	public abstract void error(Throwable t);
	
	public abstract void error(Throwable t1, Throwable t2);
	
	public abstract void fatal(String msg);
	
	public abstract void fatal(String msg, Throwable t);
	
	public abstract void fatal(Throwable t1, Throwable t2);
	
	public abstract void debug(String msg);
	
	public abstract void debug(String msg, Throwable t);
	
	public abstract void debug(Throwable t1, Throwable t2);
	
	public abstract void warn(String msg);
	
	public abstract void warn(String msg, Throwable t);
	
	public abstract void warn(Throwable t1, Throwable t2);
	
	public abstract void info(String msg);
	
	public abstract void info(Object o);
	
	public abstract void info(String msg, Throwable t);
	
	public abstract void info(Throwable t1, Throwable t2);
}
