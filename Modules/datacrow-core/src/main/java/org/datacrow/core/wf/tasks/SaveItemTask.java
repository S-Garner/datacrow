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
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.ValidationException;
import org.datacrow.core.server.Connector;

public class SaveItemTask extends DcTask {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(SaveItemTask.class.getName());
	
    public SaveItemTask() {
        super("Save-Items-Task");
    }
    
    @Override
	public int getType() {
		return _TYPE_SAVE_TASK;
	}

    @Override
    public void run() {
        try {
            startTask();
            
            if (!isCanceled()) {
            	
            	Connector connector = DcConfig.getInstance().getConnector();
            	
                for (DcObject dco : items) {
                	
                	notifyClientsProcessed();
                	
                	if (isCanceled()) break;
                	
                	try {
                		connector.saveItem(dco);
                		success = true;
                	} catch (ValidationException ve) {
                		success = false;
                		notifyClients(IClient._WARNING, ve);
                	}
                    
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    	logger.debug(e, e);
                    }
                }
            }
        } finally {
            endTask();
        }
    }
}
