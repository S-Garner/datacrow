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

import org.datacrow.core.DcConfig;
import org.datacrow.core.clients.IClient;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.ValidationException;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;

public class DeleteItemTask extends DcTask {
	
	private boolean cascade = false;

    public DeleteItemTask() {
        super("Delete-Items-Task");
    }
    
    @Override
	public int getType() {
		return _TYPE_DELETE_TASK;
	}
    
	@Override
    public void run() {
        try {

        	startTask();
            
        	Connector connector = DcConfig.getInstance().getConnector();
        	
        	boolean askedForCascade = false;
        	boolean hasReferences = false;
        	
            for (DcObject dco : items) {

            	// only check for references if not asked before if we need to or when we are doing a cascade delete
            	if (!askedForCascade || cascade) {
            		hasReferences = connector.getReferencingItems(dco.getModuleIdx(), dco.getID()).size() > 0;
            	
            		// has references, but cascade not enabled: let's ask the user what to do!
            		if (hasReferences && !cascade) {
            			askedForCascade = true;
            			cascade = notifyClients(IClient._QUESTION, DcResources.getText("msgDeleteCascade"));
            		}
            		
            		// remove references if we are allowed to:
            		if (cascade)
            			connector.removeReferencesTo(dco.getModuleIdx(), dco.getID());
            	}
            	
            	if (isCanceled()) break;
            	
            	try {
            		connector.deleteItem(dco);
            		success = true;
            	} catch (ValidationException ve) {
            		success = false;
            		// skip this as we already asked the user how to handle this.
                	// notifyClients(IClient._WARNING, ve.getMessage());
                }
                
                try {
                    Thread.sleep(10);
                } catch (Exception ignore) {}
            }
        } finally {
            endTask();
        }
    }
}
