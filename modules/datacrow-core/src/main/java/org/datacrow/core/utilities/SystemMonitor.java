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

package org.datacrow.core.utilities;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;

/**
 * The system monitor checks the available resources and logs information about the 
 * available memory. It is also capable to run a scheduled garbage collection task
 * based on the settings.
 * 
 * @author Robert Jan van der Waals 
 */
public class SystemMonitor extends Thread {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(SystemMonitor.class.getName());
    
    private final Runtime runtime;
    
    public SystemMonitor() {
        runtime = Runtime.getRuntime();
        setName("System-Monitor-Thread");
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                sleep(60000);
                checkMemory();
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }
    
    private void checkMemory() {
        long max = Math.round(Math.round(runtime.maxMemory() / 1024) / 1024) + 1;
        long used = Math.round(Math.round(runtime.totalMemory() / 1024) / 1024) + 1;
        
        long available = max - used;

        logger.debug("Memory usage (max " + max + " MB) (used " + used + " MB) (available " + available + " MB)");
    }
}
