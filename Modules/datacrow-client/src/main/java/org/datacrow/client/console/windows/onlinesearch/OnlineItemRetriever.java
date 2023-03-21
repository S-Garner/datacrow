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

package org.datacrow.client.console.windows.onlinesearch;

import org.apache.logging.log4j.Logger;

import org.datacrow.client.console.GUI;
import org.datacrow.client.util.PollerTask;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.utilities.CoreUtilities;

/**
 * Call from another thread and wait for this task to finish
 */
public class OnlineItemRetriever extends Thread {

    private static Logger logger = DcLogManager.getLogger(OnlineItemRetriever.class.getName());

    private SearchTask task;
    private DcObject dco;
    
    public OnlineItemRetriever(SearchTask task, DcObject dco) {
        this.task = task;
        this.dco = dco;
        
        setPriority(Thread.MIN_PRIORITY);
    }

    public DcObject getDcObject() {
        return dco;
    }
    
    @Override
    public void run() {
        PollerTask poller = new PollerTask(this, DcResources.getText("msgRetrievingItemDetails", dco.toString()));
        poller.start();
        
        try {
            dco = task.query(dco);
            poller.finished(true);
        } catch (Exception e) {
            GUI.getInstance().displayErrorMessage(CoreUtilities.isEmpty(e.getMessage()) ? e.toString() : e.getMessage());
            logger.error(e, e);
        }
    }
}
