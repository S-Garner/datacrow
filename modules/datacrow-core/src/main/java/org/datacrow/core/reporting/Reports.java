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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.datacrow.core.DcConfig;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.utilities.Directory;
import org.datacrow.core.utilities.Hash;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRSaver;

public class Reports {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(Reports.class.getName());

    private final Map<Integer, String> folders = new HashMap<Integer, String>();
    private final Map<String, String> reportHashes = new HashMap<String, String>();
    private final File reportsFile;
    
    private static Reports instance = new Reports();
    
    public static Reports getInstance() {
        return instance;
    }
    
    private Reports() {
    	
    	reportsFile = Paths.get(DcConfig.getInstance().getReportDir(), "reports.properties").toFile();
    	
    	// reads the reports file which holds hashes of the jrxml files.
    	retrieveCompilationStatus();
    	
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
    
    private void retrieveCompilationStatus() {
    	
    	if (reportsFile.exists()) {
    		try {
	            Properties p = new Properties();
	            FileInputStream fis = new FileInputStream(reportsFile);
                p.load(fis);
                fis.close();	            
            
	            for (Object key : p.keySet()) {
	            	reportHashes.put((String) key, p.getProperty((String) key));
	            }
    		} catch (IOException e) {
    			logger.error("Error while trying to establish compilation status for the reports", e);
    		}
    	}    	
    }
    
    private void saveReportHashes() {
    	String value;
    	
    	Properties p = new Properties();
    	
    	for (String key : reportHashes.keySet()) {
    		value = reportHashes.get(key);
    		p.put(key, value);
    	}
    	
    	try {
    		FileOutputStream fos = new FileOutputStream(reportsFile);
    		p.store(fos, "");
    		fos.close();
    	} catch (Exception e) {
    		logger.error("Failed to save the reports.properties file", e);
    	}
    }

	// TODO: download the JRXML files from the server in server-client mode
    private void updateUserDir() {
    	
    	Directory dir = new Directory(
    			 new File(DcConfig.getInstance().getInstallationDir(), "reports").toString(), 
    			 true,
    			 new String[] {"jrxml"});
    	
    	logger.info("Checking for report file updates from the application folder.");
    	
    	Collection<String> applicationReports = dir.read();
    	
    	Path applicationFile;
    	Path userFile;

    	String moduleName;
    	String reportName;
    	String key;
    	
    	String newHash;
    	String oldHash;
    	
    	for (String applicationReport : applicationReports) {
    		applicationFile = Paths.get(applicationReport);

    		moduleName = applicationFile.getName(applicationFile.getNameCount() - 2).toString();
    		reportName = applicationFile.getFileName().toString();
    		
    		key = moduleName + "_" + reportName;
    		
    		Hash hash = Hash.getInstance();
    		hash.setHashType("sha256");
    		
    		newHash = hash.calculateHash(applicationFile.toString());
    		oldHash = reportHashes.get(key);
    		
			userFile = Paths.get(
					DcConfig.getInstance().getReportDir(), 
					moduleName, 
					reportName.replaceAll("jrxml", "jasper"));
    		
    		if (!newHash.equals(oldHash) || !userFile.toFile().exists()) {
    			InputStream is = null;
    			try {
    				is = new FileInputStream(applicationFile.toFile());
    				JasperReport jasperReport = JasperCompileManager.compileReport(is);
    				JRSaver.saveObject(jasperReport, userFile.toString());
    				
    				logger.info("Successfully recompiled report " + reportName + " for module " + moduleName);
    				
    				reportHashes.put(key, newHash);
    				
    			} catch (Exception e) {
    				logger.error("Failed to compile report " + reportName, e);
    			} finally {
    				if (is != null) try {is.close(); } catch (Exception e) {logger.error("Could not close resource", e);}
    			}
    		}
    	}
    	
    	saveReportHashes();
    	
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
