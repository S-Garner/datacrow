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

package org.datacrow.server.backup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.datacrow.core.DcConfig;
import org.datacrow.core.Version;
import org.datacrow.core.clients.IBackupRestoreClient;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.server.db.DatabaseManager;

/**
 * The restore class is capable of restoring a back up.
 * Based on the settings either the data, the modules, the modules or all
 * information is restored.
 * 
 * @author Robert Jan van der Waals
 */
public class Restore extends Thread {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(Restore.class.getName());
    
    private Version version;
    private IBackupRestoreClient client;
    private ZipFile zipFile;
    
    /**
     * Creates a new instance.
     * @param listener The listener will be updated on events and errors.
     * @param source The backup file.
     */
    public Restore(IBackupRestoreClient listener, File source) throws IOException {
        this.client = listener;
        this.zipFile = new ZipFile(source);
    }
    
    private void restartApplication() {
        client.notify(DcResources.getText("msgRestoreFinishedRestarting"));
        System.exit(0);
    }    
    
    private boolean isSupportedVersion() {
        boolean supported = false;
        

        InputStream is = null;
        try {
            ZipEntry versionEntry = zipFile.getEntry("version.txt");
            
            if (versionEntry == null) {
                client.notifyWarning(DcResources.getText("msgCouldNotDetermineVersion"));
                return false;
            }
                
            is = zipFile.getInputStream(versionEntry);
            String s = CoreUtilities.readInputStream(is);
            s = s.indexOf("\n") > -1 ? s.substring(0, s.indexOf("\n")) : s;
            version = new Version(s);
        } catch (IOException e) {
        	logger.error(e, e);
        	client.notifyError(e);
        } finally {
            if (is != null) {
                try { 
                    is.close(); 
                } catch (IOException e) {
                    logger.debug("An error occured while closing resources", e);
                }
            }
        }
        
        if (version == null || version.isUndetermined()) {
        	client.notifyWarning(DcResources.getText("msgCouldNotDetermineVersion"));
        } else if (version != null && version.isOlder(new Version(3, 4, 13, 0))) {
        	client.notifyWarning(DcResources.getText("msgOldVersion3.4.12"));
        } else if (version != null && version.isOlder(new Version(3, 8, 16, 0))) {
        	client.notifyWarning(DcResources.getText("msgOldVersion3.8.16"));
        } else if (version != null && version.isOlder(new Version(3, 12, 5, 0))) {
        	client.notifyWarning(DcResources.getText("msgOldVersion3.12.5"));
        } else {
            supported = true;
        }
        
        return supported;
    }
    
    private void finish() {
        client.notifyTaskCompleted(true, null);
        version = null;
        client = null;
        
        try {
            zipFile.close();
        } catch (IOException e) {
            logger.debug("An error occured while closing resources", e);
        }       
    }
    
    /**
     * Returns the target file for the provided backup file entry.
     */
    private String getTargetFile(String filename) {
        boolean restore = true;
        if (!client.isRestoreDatabases() && 
            (filename.toLowerCase().startsWith("database/") ||
             filename.toLowerCase().startsWith("database\\")  ||
             filename.toLowerCase().indexOf("/mediaimages/") > -1 ||
             filename.toLowerCase().indexOf("\\mediaimages\\") > -1)) {
            restore = false;
        } else if (
            !client.isRestoreModules() && 
            (filename.toLowerCase().startsWith("modules/") ||
            filename.toLowerCase().startsWith("modules\\"))) {    
            restore = false;
        } else if (
            !client.isRestoreReports() && 
            (filename.toLowerCase().startsWith("reports/") ||
             filename.toLowerCase().startsWith("reports\\") ))   {
            restore = false;
        } else if (
            filename.toLowerCase().endsWith(".log") || 
            filename.toLowerCase().endsWith("version.properties") ||
            filename.toLowerCase().endsWith("log4j.properties") ||
            filename.toLowerCase().contains("datacrow.log")) {
            restore = false;
        }
        
        if (filename.toLowerCase().contains("wwwroot") && !filename.toLowerCase().contains("mediaimages")) {
            restore = false;
        }
   
        return restore ? new File(DcConfig.getInstance().getDataDir(), filename).toString() : null;
    }
    
    private boolean restore() throws Exception {
        
        boolean success = true;
        
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        
        client.notifyTaskStarted(zipFile.size());
        client.notify(DcResources.getText("msgStartRestore"));
        client.notify(DcResources.getText("msgClosingDb"));
        
        DatabaseManager.getInstance().closeDatabases(false);

        String filename;
        File destFile;
        ZipEntry zipEntry;
        String srcFilename;

        try {            
            while (entries.hasMoreElements()) {
                zipEntry = entries.nextElement();
                client.notifyProcessed();
                
                // the filename will contain the full zip file name and thus needs to be stripped
                srcFilename = zipEntry.getName();
                
                client.notify(DcResources.getText("msgRestoringFile", srcFilename));
                
                filename = getTargetFile(srcFilename);
                
                if (filename == null) continue;
                
                destFile = new File(filename);
                
                if (destFile.exists()) 
                    destFile.delete();
                
                if (destFile.exists()) 
                    client.notify(DcResources.getText("msgRestoreFileOverwriteIssue", srcFilename));
                
                try {
                    destFile.getParentFile().mkdirs();
                } catch (Exception e) {
                    String msg = DcResources.getText("msgUnableToCreateDir", filename);
                    logger.warn(msg, e);
                    throw new Exception(msg, e);
                }
                
                if (    !zipEntry.getName().endsWith("/") && 
                        !zipEntry.getName().endsWith("\\") && 
                        !zipEntry.getName().equals("version.txt")) {
                    
                	InputStream is = zipFile.getInputStream(zipEntry);
                    Files.copy(is, Paths.get(filename));
                    
                    try {
                    	is.close();
                    } catch (Exception e) {
                    	logger.error("Could not close input stream", e);
                    }
                }

                sleep(10);
            }
        } catch (Exception e) {
            success = false;
            logger.error(e, e);
            client.notify(DcResources.getText("msgRestoreFileError", new String[] {zipFile.getName(), e.getMessage()}));
        }
        
        return success;
    }
    
    /**
     * Performs the actual restore. The listener is updated on errors and events.
     */
    @Override
    public void run() {
        boolean success = false;

        try {
            if (!isSupportedVersion()) {
                finish();
                return;
            }
            
            success = restore();
            
        } catch (Exception e) {
            client.notifyError(e);
        }

        if (success) {
            restartApplication();
            client.notify(DcResources.getText("msgRestoreFinished"));
        } else {
            client.notifyError(new Exception(DcResources.getText("msgIncompleteRestore")));
        }
        
        finish();
    }
}
