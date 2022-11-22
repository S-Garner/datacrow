package org.datacrow.core.services;

public class OnlineSearchUserError extends Exception {

    public OnlineSearchUserError(String msg) {
        super(msg);
    }
    
    public OnlineSearchUserError(String msg, Throwable t) {
        super(msg, t);
    }
}
