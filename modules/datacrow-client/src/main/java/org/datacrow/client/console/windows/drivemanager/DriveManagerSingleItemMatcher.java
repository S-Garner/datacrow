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

package org.datacrow.client.console.windows.drivemanager;

import javax.swing.SwingUtilities;

import org.datacrow.client.console.GUI;
import org.datacrow.client.util.PollerTask;
import org.datacrow.core.drivemanager.DriveManager;
import org.datacrow.core.drivemanager.FileInfo;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;

/**
 * Call from another thread and wait for this task to finish
 */
public class DriveManagerSingleItemMatcher extends Thread {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DriveManagerSingleItemMatcher.class.getName());
    
    private final int precision;
    private final DcObject dco;
    
    private FileInfo result;
    
    public DriveManagerSingleItemMatcher(DcObject dco, int precision) {
        this.dco = dco;
        this.precision = precision;
        
        setName("Drive-Manager-Single-Item-Matcher");
        setPriority(Thread.MIN_PRIORITY);
    }
    
    public FileInfo getResult() {
        return result;
    }

    @Override
    public void run() {
        
        if (!DriveManager.getInstance().drivesWereScanned()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        if (GUI.getInstance().displayQuestion("msgDrivesNotScanned"))
                            DriveManagerDialog.getInstance().setVisible(true);
                    }
                });
            } catch (Exception e) {
                logger.error(e, e);
            }
        } else {
            String filename = (String) dco.getValue(DcObject._SYS_FILENAME);
            String hash = (String) dco.getValue(DcObject._SYS_FILEHASH); 
            Long size  = (Long) dco.getValue(DcObject._SYS_FILESIZE); 
            
            PollerTask poller = new PollerTask(this, DcResources.getText("msgLocatingFile", filename));
            poller.start();
            
            FileInfo fi = new FileInfo(hash, filename, size);
            result = DriveManager.getInstance().find(fi, precision);
            
            poller.finished(true);
        }
    }
}
