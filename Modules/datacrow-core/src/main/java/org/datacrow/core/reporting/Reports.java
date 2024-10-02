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

package org.datacrow.core.reporting;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.datacrow.core.DcConfig;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.Directory;

public class Reports {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(Reports.class.getName());

    private final Map<Integer, String> folders = new HashMap<Integer, String>();
    
    private static Reports instance = new Reports();
    
    public static Reports getInstance() {
        return instance;
    }
    
    private Reports() {
    	
    	updateUserDir();
    	
        for (DcModule module : DcModules.getModules()) {
            if (module.isSelectableInUI()) {
                String path = DcConfig.getInstance().getReportDir() + module.getName().toLowerCase().replaceAll("[/\\*%., ]", "");
                File file = new File(path);
                if (!file.exists() && 
                	!file.getParentFile().equals(new File(DcConfig.getInstance().getInstallationDir())))
                    file.mkdirs();
                
                folders.put(module.getIndex(), path);
            }
        }
    }
    
    private void updateUserDir() {
    	
    	Directory dir = new Directory(
    			 new File(DcConfig.getInstance().getInstallationDir(), "reports").toString(), 
    			 true,
    			 new String[] {"jasper"});
    	
    	logger.info("Checking for report file updates from the application folder.");
    	
    	Collection<String> applicationReports = dir.read();
    	
    	Path applicationFile;
    	Path userFile;
    	String moduleName;
    	
    	BasicFileAttributes bfaApp;
    	BasicFileAttributes bfaUsr;
    	
    	for (String applicationReport : applicationReports) {
    		applicationFile = Paths.get(applicationReport);

    		moduleName = applicationFile.getName(applicationFile.getNameCount() - 2).toString();
    		userFile = Paths.get(DcConfig.getInstance().getReportDir(), moduleName, applicationFile.getFileName().toString());

    		if (userFile.toFile().exists()) {
    		
    			try {
    				bfaApp = Files.readAttributes(applicationFile, BasicFileAttributes.class);
    				bfaUsr = Files.readAttributes(userFile, BasicFileAttributes.class);
    				
    				if (bfaApp.lastModifiedTime().compareTo(bfaUsr.lastModifiedTime()) > 0) {
    					CoreUtilities.copy(applicationFile.toFile(), userFile.toFile(), true);
    					
    					logger.info("Updated report " + applicationFile.getFileName() + " in user folder as the application report file is newer.");
    				}
    				
    			} catch (Exception e) {
    				logger.error(
    						"An error occured whilst checking whether the report file [" +
    						applicationFile + "] is newer than its counterpart in the user folder. SKipping the check on this file.", e);
    			}
    		} else {
    			try {
    				CoreUtilities.copy(applicationFile.toFile(), userFile.toFile(), true);

    				logger.info("Copied report " + applicationFile.getFileName() + " to the user folder as it didn't exist.");
				} catch (Exception e) {
					logger.error(
							"An error occured whilst checking whether the report file [" +
							applicationFile + "] is newer than its counterpart in the user folder. SKipping the check on this file.", e);
				}    			
    		}
    	}
    	
    	logger.info("Finished checking for report file updates.");
    }
    
    public Collection<String> getFolders() {
        return folders.values();
    }
    
    public boolean hasReports(int module) {
        String folder = folders.get(module);
        if (folder != null) {
            String[] extensions = {"jasper"};
            Directory dir = new Directory(folder, true, extensions);
            Collection<String> files = dir.read();
            if (files.size() > 0) return true;
        }
        return false;
    }
    
    public Collection<Report> getReports(int module) {
        String folder = folders.get(module);
        
        Collection<Report> reports = new ArrayList<Report>();
        if (folder != null) {
            String[] extensions = {"jasper"};
            Directory directory = new Directory(folder, true, extensions);
            Collection<String> files = directory.read();
            for (String filename : files) {
                Report rf = new Report(filename);
                reports.add(rf);
            }
        }
        
        return reports;
    }
}
