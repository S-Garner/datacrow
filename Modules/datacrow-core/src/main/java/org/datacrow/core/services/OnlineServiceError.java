package org.datacrow.core.services;

public class OnlineServiceError extends Exception {
    
    public OnlineServiceError(Throwable t) {
        super(t);
    }    
    
    public OnlineServiceError(String msg, Throwable t) {
        super(msg, t);
    }
}
