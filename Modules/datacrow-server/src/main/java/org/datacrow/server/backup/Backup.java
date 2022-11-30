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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.clients.IBackupRestoreClient;
import org.datacrow.core.data.DataFilters;
import org.datacrow.core.filerenamer.FilePatterns;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.Directory;
import org.datacrow.server.db.DatabaseManager;


/**
 * Performs a backup of the Data Crow data, settings, modules and reports.
 * 
 * @author Robert Jan van der Waals
 */
public class Backup extends Thread {
    
    private static Logger logger = DcLogManager.getLogger(Backup.class.getName());
    
    private File directory;
    private IBackupRestoreClient client;
    private String comment;
 
    /**
     * Creates a new instance.
     * @param client The listener which will be informed of events and errors.
     * @param directory The directory where the backup will be created.
     */
    public Backup(IBackupRestoreClient client, File directory, String comment) {
        this.directory = directory;
        this.comment = comment;
        this.client = client;
    }
    
    /**
     * Retrieves all the files to be backed up.
     * @return A collection of fully classified filenames.
     */
    private File[] getFiles() {
        Collection<File> files = new ArrayList<File>();
        String paths[] = {
                DcConfig.getInstance().getApplicationSettingsDir(),
                DcConfig.getInstance().getModuleSettingsDir(),
                DcConfig.getInstance().getDatabaseDir(),
                DcConfig.getInstance().getModuleDir(),
                DcConfig.getInstance().getReportDir(),
                DcConfig.getInstance().getResourcesDir(),
                DcConfig.getInstance().getUpgradeDir(),
                DcConfig.getInstance().getImageDir()};
        
        Directory dir;
        for (String path : paths) {
            dir = new Directory(path, true, null);
            for (String filename : dir.read()) {
                files.add(new File(filename));
            }
        }
        return (File[]) files.toArray(new File[0]);
    }

    private String getZipFile(String target) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmm");
        String date = format.format(cal.getTime());

        String filename = "datacrow_backup_" + date + ".zip";
        String zipFile = target.endsWith(File.separator) ? target + filename :
                                                           target + File.separator + filename;
        return zipFile;
    }    
    
    private void saveChanges() {
        DataFilters.save();
        FilePatterns.save();
        DcSettings.save();
        DcModules.save();
    }    
    
    /**
     * Performs the actual back up and informs the clients on the progress.
     */
    @Override
    public void run() {
    	
        if (!directory.exists())
            directory.mkdirs();
        
        client.notify(DcResources.getText("msgStartBackup"));
        client.notify(DcResources.getText("msgClosingDb"));
        
        saveChanges();
        DatabaseManager.getInstance().closeDatabases(true);

        File[] files = getFiles();
        client.notifyTaskStarted(files.length);
        
        FileOutputStream fos = null;
        ZipOutputStream zipOut = null;
        
        try {
            String zipFileName = getZipFile(directory.toString());
            
            fos = new FileOutputStream(zipFileName);
            zipOut = new ZipOutputStream(fos);
            zipOut.setLevel(Deflater.BEST_COMPRESSION);
            
            // add the version and add the comment entered by the customer
            zipOut.putNextEntry(new ZipEntry("version.txt"));
            zipOut.write(DcConfig.getInstance().getVersion().toString().getBytes());
            
            if (comment.length() > 0)
                zipOut.write(("\n" + comment).getBytes());
            
            zipOut.closeEntry();
            
            // add all the file
            for (File file : files) {

                zipDirectory(zipOut, file, DcConfig.getInstance().getDataDir());
                client.notifyProcessed();
                
                try {
                    sleep(10);
                } catch (Exception e) {
                    logger.warn(e, e);
                }                
            }
                
            client.notify(DcResources.getText("msgWritingBackupFile"));
            client.notify(DcResources.getText("msgBackupFinished"));
        } catch (Exception e) {
            client.notify(DcResources.getText("msgBackupError", e.getMessage()));
            client.notifyError(e);
            client.notifyWarning(DcResources.getText("msgBackupFinishedUnsuccessful"));
        } finally {
            try {
                zipOut.close();
                fos.close();
            } catch (Exception ignore) {}
        }
        
        DcSettings.set(DcRepository.Settings.stBackupLocation, directory.toString());
        
        client.notify(DcResources.getText("msgRestartingDb"));
        
        DatabaseManager.getInstance().initialize();
        client.notifyTaskCompleted(true, null);
        
        client = null;
        directory = null;
    }
    
    private void zipDirectory(ZipOutputStream zipOut, File fileToZip, String parentDirectoryName) throws Exception
    {
        if (fileToZip == null || !fileToZip.exists()) 
            return;

        String zipEntryName = fileToZip.getName();

        String s = fileToZip.getAbsolutePath();
        zipEntryName =  s.substring(DcConfig.getInstance().getDataDir().length() + 1 - 
                (DcConfig.getInstance().getDataDir().startsWith("/") && !s.startsWith("/") ? 2 : 1));

        client.notify(DcResources.getText("msgCreatingBackupOfFile", zipEntryName));
        
        if (fileToZip.isDirectory()) {
            if (parentDirectoryName == null)
                zipEntryName = "";

            for (File file : fileToZip.listFiles()) {
                zipDirectory(zipOut, file, fileToZip.getAbsolutePath());
            }
        } else {
            byte[] buffer = new byte[1024];
            
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(fileToZip);
                zipOut.putNextEntry(new ZipEntry(zipEntryName));
                
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, length);
                }
            } finally {
                zipOut.closeEntry();
                if (fis != null) fis.close();
            }
        }
    }    
}
