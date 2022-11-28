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
import org.datacrow.core.DcRepository;
import org.datacrow.core.Version;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.http.HttpConnectionUtil;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.services.plugin.ServiceClassLoader;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.CoreUtilities;

/**
 * This class is used to register all the found servers in the services folder.
 * The {@link ServiceClassLoader} is used to located these servers.
 * @author Robert Jan van der Waals
 */
public class Servers {
    
    private static final String baseUrl = "https://www.datacrow.org/online-services/";
    
    private static Logger logger = DcLogManager.getLogger(Servers.class.getName());
    private static Servers instance;
    
    private boolean initialized = false;
    
    private final Map<Integer, OnlineServices> registered;
    
    private boolean upgraded = false;
    private String upgradeInformation = "";
    private Version version;
    
    private Properties apiKeys = new Properties();
    
    static {
    	 instance = new Servers();
    }

    /**
     * Creates this class and starts the search for the servers.
     */
    private Servers() {
    	registered = new HashMap<Integer, OnlineServices>();
    }
    
    /**
     * Starts the search for the servers using the {@link ServiceClassLoader}. 
     * The services folder is scanned for both jar and class files. Any class implementing
     * the {@link IServer} class is registered.
     */
    public synchronized void initialize() {
        
        initialized = true;

        // initialize the services directory in the user folder
        initializeServicesDir();
        
        // download API keys for online services
        downloadApiKeys();
        
        
        if (DcSettings.getBoolean(DcRepository.Settings.stAutoUpdateOnlineServices)) {
            // check for a new version online
            try {
                upgrade();
            } catch (Exception e) {
                logger.error("Could not upgrade the online service jar file", e);
            }
        }

        // load the service pack
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
    
    public String getApiKey(String key) {
        return apiKeys.getProperty(key);
    }
    
    private void downloadApiKeys() {
        
        
        String file = baseUrl + "services-keys.properties";
        URL address = null;
        
        try {
            address = new URL(file);
        } catch (Exception e) {
            logger.error("Could not download the required information for services. Some services might not function.", e);
        }
        
        if (address == null) return;
        
        try {
            HttpConnection conn =  HttpConnectionUtil.getConnection(address);
            InputStream is = conn.getInputStream();
            apiKeys.load(is);
            
            try {
                is.close();
            } catch (Exception e) {
                logger.debug("Failed to close input stream when checking for online services required information", e);
            }
            
        } catch (Exception e) {
            logger.error("Could not download the required information for services. Some services might not function.", e);
        }
    }
    
    public Version getVersionInformation() {
        return version;
    }    
    
    public boolean isUpgraded() {
        return upgraded;
    }
    
    public String getUpgradeInformation() {
        return upgradeInformation;
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
        
        String file = baseUrl + "services.properties";
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
        
        String s = "0.0.0";
        
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
                                s = p.getProperty("version");
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
        
        version = new Version(s);
        
        return version;
    }
    
    public boolean upgrade() throws IOException {
        
        Version currentVersion = getCurrentVersion();
        Properties onlineVersionInfo = getOnlineVersionInformation();

        String onlineVersion = onlineVersionInfo != null ? onlineVersionInfo.getProperty("version") : null;
        if (!CoreUtilities.isEmpty(onlineVersion)) {
            if (currentVersion.isOlder(new Version(onlineVersion))) {
                
                logger.info("New version found for the online services. Current version = " + currentVersion + ". New version = " + onlineVersion + ".");
                
                String url = onlineVersionInfo.getProperty("downloadUrl");
                String filename =  url.substring(url.lastIndexOf("/") + 1);
                try {
                    
                    File targetFile = new File(DcConfig.getInstance().getServicesDir() + "_new" + filename);
                    
                    // check if the file does not yet exist; else, delete
                    if (targetFile.exists())
                        targetFile.delete();
                    
                    CoreUtilities.downloadFile(url, targetFile.toString());
                    
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
                    
                    logger.info("Updated the online services to version " + onlineVersion);
                    
                    upgradeInformation = onlineVersionInfo.getProperty("information");
                    upgraded = true;
                } catch (Exception e) {
                    logger.error("Could not download the new services jar file", e);
                }
            }
        }
        
        return upgraded;
    }
    
    private void initializeServicesDir() {
        File targetDir = new File(DcConfig.getInstance().getServicesDir());
        File sourceDir = new File(DcConfig.getInstance().getInstallationDir(), "services/");
        
        if (!targetDir.exists())
            targetDir.mkdirs();
        
        if (targetDir.list().length == 0) {
            String[] sourceFiles = sourceDir.list();
            for (String sourceFile : sourceFiles) {
                try {
                    CoreUtilities.copy(new File(sourceDir, sourceFile), new File(targetDir, sourceFile), false);
                } catch (Exception e) {
                    logger.error("Could not copy the online service pack to the user folder", e);
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
