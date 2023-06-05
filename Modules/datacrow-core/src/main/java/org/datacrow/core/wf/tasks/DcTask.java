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

package org.datacrow.core.wf.tasks;

import java.util.ArrayList;
import java.util.Collection;

import org.datacrow.core.clients.IClient;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.utilities.CoreUtilities;

public abstract class DcTask implements Runnable {

    public static int _TYPE_DELETE_TASK = 0;
	public static int _TYPE_SAVE_TASK = 1;
    
    private int moduleIdx;

    private boolean executing = false;
    private boolean canceled = false;
    
    protected Collection<IClient> clients = new ArrayList<IClient>();

    protected Collection<DcObject> items = new ArrayList<DcObject>();

    protected boolean success = false;
    
    private String name;
    private String id;
    private boolean silent = false;
    
    public DcTask(String name) {
    	this.name = name;
    	this.id = CoreUtilities.getUniqueID();
    }
    
    public abstract int getType();
    
    public void setSilent(boolean b) {
    	this.silent = b;
    }
    
    public boolean isSilent() {
    	return silent;
    }
    
    public String getName() {
    	return name;
    }
    
    public String getId() {
    	return id;
    }
    
    public int getTaskSize() {
        return items != null ? items.size() : 0;
    }
    
    public void addClient(IClient client) {
    	clients.add(client);
    }
    
    public void removeClient(IClient client) {
    	clients.remove(client);
    }
    
    protected void notifyClientsProcessed() {
        for (IClient client : clients) {
            client.notifyProcessed();
        }
    }
    
    protected void notifyClients(boolean success) {
        for (IClient client : clients) {
            client.notifyTaskCompleted(success, getId());
        }
    }
    
    protected void notifyClients(int type, Throwable t) {
    	for (IClient client : clients) {
	    	if (type == IClient._ERROR)
	    		client.notifyError(t); // always report errors
	    	else if (type == IClient._WARNING)
	    		if (!silent) client.notifyWarning(t.getMessage());
	    	else if (type == IClient._INFO)
	    		if (!silent) client.notify(t.getMessage());
    	}
    }
    
    protected boolean notifyClients(int type, String msg) {
    	boolean result = false;
    	
    	boolean questionAsked = false;
        for (IClient client : clients) {
	    	if (type == IClient._ERROR)
	    	    client.notifyError(new Exception(msg));
	    	else if (type == IClient._WARNING)
	    	    client.notifyWarning(msg);
	    	else if (type == IClient._INFO)
	    	    client.notify(msg);
	    	else if (type == IClient._QUESTION && !questionAsked) {
	    		if (!silent) {
		    		result |= client.askQuestion(msg);
	    		} else {
	    			result = false; // in silent mode we assume a negative response from the user
	    		}
	    		
	    		questionAsked = true;
	    	}
    	}
        
        return result;
    }
    
    public void addItems(Collection<DcObject> items) {
    	this.items = items;
    }
    
    public void setModule(int moduleIdx) {
    	this.moduleIdx = moduleIdx;
    }
    
    public DcModule getModule() {
    	return moduleIdx > 0 ? DcModules.get(moduleIdx) : null;
    }
    
    public void addItem(DcObject item) {
    	this.items.add(item);
    }
    
	public void startTask() {
        executing = true;
        canceled = false;
        
        for (IClient client : clients) {
            client.notifyTaskStarted(0);
        }
    }
    
	public void endTask() {
    	canceled = true;
    	executing = false;
    	
        for (IClient client : clients) {
        	client.notifyTaskCompleted(success, getId());
        }
    }

	public void cancel() {
    	canceled = true;
    }

	public boolean isRunning() {
    	return executing;
    }

	public boolean isCanceled() {
    	return canceled;
    }
}
