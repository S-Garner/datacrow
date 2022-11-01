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

package org.datacrow.core.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.Logger;
import org.datacrow.core.DcConfig;
import org.datacrow.core.Version;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.http.HttpConnectionUtil;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.services.plugin.ServiceClassLoader;
import org.datacrow.core.utilities.CoreUtilities;

/**
 * This class is used to register all the found servers in the services folder.
 * The {@link ServiceClassLoader} is used to located these servers.
 * @author Robert Jan van der Waals
 */
public class Servers {
    
    private static Logger logger = DcLogManager.getLogger(Servers.class.getName());
    private static Servers instance;
    
    private boolean initialized = false;
    
    private final Map<Integer, OnlineServices> registered;
    
    static {
    	 instance = new Servers();
    }

    /**
     * Creates this class and starts the search for the servers.
     */
    private Servers() {
    	registered = new HashMap<Integer, OnlineServices>();
    }
    
    public boolean isInitialized() {
		return initialized;
	}
    
    public boolean hasOnlineService(int moduleIdx) {
    	return registered.containsKey(Integer.valueOf(moduleIdx));
    }
    
	/**
     * Retrieves all the servers for the given module.
     * @param module
     */
    public OnlineServices getOnlineServices(int moduleIdx) {
        return registered.get(Integer.valueOf(moduleIdx));
    }
    
    public Properties getOnlineVersionInformation() {
        
        Properties properties = new Properties();
        
        String file = "https://www.datacrow.org/services.properties";
        URL address;
        
        try {
            address = new URL(file);
        } catch (Exception e) {
            logger.debug(e, e);
            return null;
        }
        
        try {
            HttpConnection conn =  HttpConnectionUtil.getConnection(address);
            InputStream is = conn.getInputStream();
            properties.load(is);

            try {
                is.close();
            } catch (Exception e) {
                logger.debug("Failed to close input stream when checking for online services package version", e);
            }
            
            return properties;
        } catch (Exception e) {
            logger.warn("Failed to check whether a new online services package was released", e);
        }
        
        return null;
    }
    
    private Version getCurrentVersion() throws IOException {
        String[] existingFiles = new File(DcConfig.getInstance().getServicesDir()).list();
        
        String version = "0.0.0";
        
        // delete existing jar files
        for (String existingFile : existingFiles) {
            if (existingFile.endsWith(".jar")) {
                ZipFile zf = new ZipFile(DcConfig.getInstance().getServicesDir() + existingFile);
                Properties p = new Properties();
                try {
                    ZipEntry entry;
                    String name;
                    Enumeration<? extends ZipEntry> entries = zf.entries();
                    while (entries.hasMoreElements()) {
                        entry = entries.nextElement();
                        name = entry.getName();
                        
                        if (name.endsWith("services.properties")) {
                            try {
                                InputStream is = zf.getInputStream(entry);
                                p.load(is);
                                version = p.getProperty("version");
                                is.close();
                            } catch (IOException ie) {  
                                logger.error("Could not read version.properties from online services jar file", ie); 
                            }                        
                            break;
                        }
                    }
                } finally {
                    zf.close();       
                }
            }
        }
        
        return new Version(version);
    }
    
    public boolean upgrade() throws IOException {
        
        boolean updated = false;
        
        Version currentVersion = getCurrentVersion();
        Properties onlineVersionInfo = getOnlineVersionInformation();

        String onlineVersion = onlineVersionInfo != null ? onlineVersionInfo.getProperty("version") : null;
        if (!CoreUtilities.isEmpty(onlineVersion)) {
            if (currentVersion.isOlder(new Version(onlineVersion))) {
                String url = onlineVersionInfo.getProperty("downloadUrl");
                String filename =  url.substring(url.lastIndexOf("/") + 1);
                try {
                    CoreUtilities.downloadFile(url, DcConfig.getInstance().getServicesDir() + "_new" + filename);
                    
                    // download was successful - delete existing version and overwrite with the new one
                    String[] existingFiles = new File(DcConfig.getInstance().getServicesDir()).list();
                    
                    // delete existing jar files
                    for (String existingFile : existingFiles) {
                        if (!existingFile.startsWith("_new"))
                            new File(DcConfig.getInstance().getServicesDir() + existingFile).delete();
                    }
                    
                    // rename the downloaded jar file to the correct name
                    CoreUtilities.rename(
                            new File(DcConfig.getInstance().getServicesDir() + "_new" + filename), 
                            new File(DcConfig.getInstance().getServicesDir() + filename), 
                            true);
                    
                    updated = true;
                } catch (Exception e) {
                    logger.error("Could not download the new services jar file", e);
                }
            }
        }
        
        return updated;
    }
    
    /**
     * Starts the search for the servers using the {@link ServiceClassLoader}. 
     * The services folder is scanned for both jar and class files. Any class implementing
     * the {@link IServer} class is registered.
     */
    public synchronized void initialize() {
    	
    	initialized = true;
    	
    	try {
    	    upgrade();
    	} catch (Exception e) {
    	    logger.error("Could not upgrade the online service jar file", e);
    	}

        ServiceClassLoader scl = new ServiceClassLoader(DcConfig.getInstance().getServicesDir());
        registered.clear();
        
        for (Class<?> clazz : scl.getClasses()) {
            
            IServer server = null;
            try {
                server = (IServer) clazz.getDeclaredConstructors()[0].newInstance();
            } catch (Exception ignore) {}    
            
            if (server != null && server.isEnabled()) {
                try {
                    OnlineServices servers = registered.get(Integer.valueOf(server.getModule()));
                    servers = servers == null ? new OnlineServices(server.getModule()) : servers;
                    servers.addServer(server);
                    
                    registered.put(Integer.valueOf(server.getModule()), servers);
                    
                    String name = server.getClass().getName();
                    name = name.substring(name.lastIndexOf(".") + 1);
                    logger.info("Registered online server " + name);
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
        }
    }

    /**
     * Returns an instance of this class.
     */
    public static Servers getInstance() {
        return instance;
    }
}
