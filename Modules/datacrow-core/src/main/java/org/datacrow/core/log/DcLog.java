package org.datacrow.core.log;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.core.LogEvent;
import org.datacrow.core.utilities.CoreUtilities;

public class DcLog {
    
    private static final DcLog me = new DcLog();
    
    private Collection<ILogListener> listeners;
    
    private ArrayList<String> backlog = new ArrayList<String>();
    
    public static DcLog getInstance() {
        return me;
    }
    
    private DcLog() {
        listeners = new ArrayList<ILogListener>();
    }
    
    public void addListener(ILogListener listener) {
        listeners.add(listener);
        
        for (String backlogMsg : backlog) { 
            listener.add(backlogMsg);
        }
        
        backlog.clear();
    }
    
    public void notify(LogEvent event) {
        String msg = 
                CoreUtilities.getTimestamp() + 
                " - " + event.getLevel().name() + 
                " - " + event.getMessage().getFormattedMessage().trim();
        
        if (listeners.size() == 0) {
            backlog.add(msg);
        }
        
        for (ILogListener listener : listeners) {
        	listener.add(msg);
        }
    }
}
