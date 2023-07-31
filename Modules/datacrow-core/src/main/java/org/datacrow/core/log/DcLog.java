/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.org                             *
 *                                                                            *
 *                       This file is part of Data Crow.                      *
 *       Data Crow is free software; you can redistribute it and/or           *
 *        modify it under the terms of the GNU General Public                 *
 *       License as published by the Free Software Foundation; either         *
 *              version 3 of the License, or any later version.               *
 *                                                                            *
 *        Data Crow is distributed in the hope that it will be useful,        *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *           MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.             *
 *           See the GNU General Public License for more details.             *
 *                                                                            *
 *        You should have received a copy of the GNU General Public           *
 *  License along with this program. If not, see http://www.gnu.org/licenses  *
 *                                                                            *
 ******************************************************************************/

package org.datacrow.core.log;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.core.LogEvent;
import org.datacrow.core.utilities.CoreUtilities;

public class DcLog {
    
    private static final DcLog me = new DcLog();
    
    private final Collection<ILogListener> listeners;
    
    private final ArrayList<String> backlog = new ArrayList<String>();
    
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
