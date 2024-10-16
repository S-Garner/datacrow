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

package org.datacrow.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.services.Servers;
import org.datacrow.core.utilities.CoreUtilities;

/**
 * The DcStarter class holds the generic startup routine for Data Crow and is used by 
 * the application server and the client version of Data Crow.
 * 
 * @author Robert Jan van der Waals
 */
public class DcStarter {
    
	private final IStarterClient client;
	
    private static DcLogger logger;
    
    /**
     * Initiates this class.
     * @param client    the client will be notified on errors and warnings. 
     */
    public DcStarter(IStarterClient client) {
        this.client = client;
    }
    
    /**
     * Executes the startup routine.
     * @return  success indicator, will return true when the starter was initialized successfully.
     */
	public boolean initialize() {
	    
	    Locale.setDefault(new Locale("en"));
	    
        // parameterized settings will have been set on the config instance.
        DcConfig dcc = DcConfig.getInstance();
        
        boolean ok = intializeInstallDir(dcc.getInstallationDir());
        if (ok) 
        	ok = initializeDataDir(dcc.getDataDir());
        
        if (!ok && DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_SERVER) {
            System.out.println("No valid data folder. Abort.");
        }
        
        if (ok) {
            checkCurrentDir();
            
            if (dcc.getOperatingMode() == DcConfig._OPERATING_MODE_SERVER) {
                createServerDirectories();
            } else if (dcc.getOperatingMode() == DcConfig._OPERATING_MODE_CLIENT) {
                createClientDirectories();
            } else if (dcc.getOperatingMode() == DcConfig._OPERATING_MODE_STANDALONE) {
                createServerDirectories();
                createClientDirectories();
            }
            
            client.intializeLogger(dcc.isDebug());
            logger = DcLogManager.getInstance().getLogger(DcStarter.class.getName());
            logger.info("Logger enabled");
            client.notifyLoggerConfigured();             
            
            printConfiguration();
            
            new DcResources().initialize();
            org.datacrow.core.settings.DcSettings.initialize();

            if (DcConfig.getInstance().getOperatingMode() != DcConfig._OPERATING_MODE_SERVER) {
                Servers servers = Servers.getInstance();
                servers.initialize();
            }
            
            checkPlatform();
            
            DcConfig.getInstance().setDataCrowStarted(true);
        }
        
        return ok;
    }
    
	/**
	 * Initializes the installation folder. It will checked whether the installation folder
	 * can be determined;
	 * - The DATACROW_HOME environment variable is consulted.
	 * - If not found, the application.home environment variable is consulted.
	 * - If not found the installation folder will be determined through the class loader.
	 * @param s    the installation folder as supplied as a startup parameter
	 * @return installation folder found and usable y/n
	 */
    private boolean intializeInstallDir(String s) {
        boolean intialized = true; 
        
        String installationDir = s;
        if (CoreUtilities.isEmpty(installationDir)) {
            installationDir = System.getenv("DATACROW_HOME");
            if (installationDir == null || installationDir.length() == 0)
                installationDir = System.getProperty("application.home");
            if (installationDir == null || installationDir.length() == 0)
                installationDir = getInstallationDirDeprecated();
        }
        
        if (CoreUtilities.isEmpty(installationDir)) {
            client.notifyWarning(
                    "The installation directory could not be determined. " +
                    "Please set the DATACROW_HOME environment variable or supply the -dir:<installation directory> parameter. " +
                    "The DATACROW_HOME variable value should point to the Data Crow installation directory.");
            intialized = false;
        } else {
            installationDir = installationDir.replaceAll("\\\\", "/");
            installationDir += !installationDir.endsWith("\\") && !installationDir.endsWith("/") ? "/" : "";

            DcConfig.getInstance().setInstallationDir(installationDir);
        }
        
        return intialized;
    }
    
    /**
     * Initializes the Data directory (user folder). This folder is very important as it holds the 
     * settings and data for the Data Crow application. The user folder is checked on;
     * - Can it be found?
     * - Has the user folder been set up correctly? It should at least contain the modules folder.
     * - Is the location suitable; does Data Crow have write permissions for this folder
     * @param the user directory as supplied on startup
     * @return  data directory successfully initialized y/n
     */
    private boolean initializeDataDir(String s) {
        boolean initialized = false;
        
        ClientSettings clientSettings = new ClientSettings();
        DcConfig dcc = DcConfig.getInstance();
        dcc.setClientSettings(clientSettings);

        String userDir = s != null ? s : "";
        
        if (!CoreUtilities.isEmpty(userDir) && (userDir.startsWith("./") || userDir.startsWith(".\\"))) {
            userDir = dcc.getInstallationDir() + userDir.substring(2);
            userDir = userDir.replaceAll("\\\\", "/");
        }
        
        try {
            boolean userFolderExists = !CoreUtilities.isEmpty(userDir);
            if (!userFolderExists && clientSettings.exists()) {
                File userDirSetting =  clientSettings.getUserDir();
                if (userDirSetting != null && userDirSetting.exists()) {
                    userDir = userDirSetting.toString();
                    userDir += userDir.endsWith("/") || userDir.endsWith("\\") ? "" : "/";
                    userFolderExists = new File(userDir).exists();
                }
            } 
            
            if (userFolderExists) {
                userFolderExists = new File(userDir, "modules").exists();
            }
            
            if (!userFolderExists || (userFolderExists && !new File(userDir).exists())) {
                String selectedUserDir = userDir;
                
                String installDir = dcc.getInstallationDir();
                dcc.setModuleDir(installDir + "modules/");
                dcc.setPluginsDir(installDir + "plugins/");
                dcc.setServicesDir(installDir + "services/");
                dcc.setResourcesDir(installDir + "resources/");
                dcc.setReportDir(installDir + "reports/");
                dcc.setDatabaseDir(installDir + "data/");
                dcc.setUpgradeDir(installDir + "upgrade/");
                dcc.setApplicationSettingsDir(installDir + "data/");
                dcc.setImageDir(installDir + "images/");
                dcc.setIconsDir(installDir + "images/icons/");
                dcc.setAttachmentDir(installDir + "attachments/");
                
                client.requestDataDirSetup(selectedUserDir);
                dcc.setInstallationDir(installDir);
                
                initialized = true;
            } else {
                File installDir = new File(dcc.getInstallationDir());
                if (installDir.equals(new File(userDir))) {
                    client.notifyError(
                            "The installation directory can't selected as the user folder. " +
                            "You CAN select a sub folder within the installation folder though.");
                    initialized = false;
                } else {
                    userDir += userDir.endsWith("/") || userDir.endsWith("\\") ? "" : "/";
                    dcc.setDataDir(userDir);
                    initialized = true;
                }
            }
        } catch (Exception e) {
        	if (logger != null)
        		logger.error(e, e);
        	else
        		e.printStackTrace();
        }     
        
        return initialized;
    }
    
    /**
     * Checks whether the specified installation directory is valid.
     */
    private void checkCurrentDir() {
        if (!new File(DcConfig.getInstance().getInstallationDir(), "lib").exists()) {
            String msg = "The installation directory could not be determined. " +
                "Please set the DATACROW_HOME environment variable or supply the -dir:<installation directory> parameter. " +
                "The DATACROW_HOME variable value should point to the Data Crow intallation directory.";
            System.out.println("Installation directory: " + DcConfig.getInstance().getInstallationDir());
            client.notifyFatalError(msg);
        }
    }
    
    /** 
     * Determine the installation folder with the help of the class loaded.
     * @return  the fully qualified installation folder.
     */
    private String getInstallationDirDeprecated() {
        String classLocation = DcStarter.class.getName().replace('.', '/') + ".class";
        ClassLoader loader = DcStarter.class.getClassLoader();
        
        URL location;
        if (loader == null)
            location = ClassLoader.getSystemResource(classLocation);
        else
            location = loader.getResource(classLocation);

        String dir = location.getFile();

        dir = dir.substring(0, dir.indexOf("/net/"));
        dir = dir.endsWith("_build") ? dir.substring(0, dir.indexOf("_build")) : dir;
        dir = dir.endsWith("_classes") ? dir.substring(0, dir.indexOf("_classes")) : dir;
        dir = dir.endsWith("classes") ? dir.substring(0, dir.indexOf("classes")) : dir;
        dir = dir.indexOf("webapp") > 0 ? dir.substring(0, dir.indexOf("webapp")) : dir;
        dir = dir.indexOf("datacrow.jar") > 0 ? dir.substring(0, dir.indexOf("datacrow.jar")) : dir;
        dir = dir.indexOf("datacrow-core.jar") > 0 ? dir.substring(0, dir.indexOf("datacrow-core.jar")) : dir;
        dir = dir.indexOf("datacrow-server.jar") > 0 ? dir.substring(0, dir.indexOf("datacrow-server.jar")) : dir;
        dir = dir.endsWith("/lib/") ? dir.substring(0, dir.indexOf("/lib/")) : dir;
        dir = dir.endsWith("/lib") ? dir.substring(0, dir.indexOf("/lib")) : dir;
        dir = dir.endsWith("\\lib\\") ? dir.substring(0, dir.indexOf("\\lib\\")) : dir;
        dir = dir.endsWith("\\lib") ? dir.substring(0, dir.indexOf("\\lib")) : dir;
        dir = dir.replaceAll("%20", " ");
        dir = dir.startsWith("file:") ? dir.substring(5) : dir;
        
        return dir;
    }
    
    /** 
     * Creates the directories for the server where necessary.
     */
    private void createServerDirectories() {
        DcConfig dcc = DcConfig.getInstance();
        String userDir = dcc.getDataDir();
        String installDir = dcc.getInstallationDir();
        
        if (DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_SERVER)
        	dcc.setWebDir(installDir + "webapp/");
        
        dcc.setImageDir(userDir + "images/");
        dcc.setIconsDir(userDir + "images/icons/");
        dcc.setReportDir(userDir + "reports/");
        dcc.setModuleDir(userDir + "modules/");
        dcc.setResourcesDir(userDir + "resources/");
        dcc.setDatabaseDir(userDir + "database/");
        dcc.setModuleSettingsDir(userDir + "settings/modules/");
        dcc.setApplicationSettingsDir(userDir + "settings/application/");
        dcc.setUpgradeDir(userDir + "upgrade/");
        dcc.setServicesDir(userDir + "services/");
        dcc.setAttachmentDir(userDir + "attachments/");
        
        createDirectory(new File(dcc.getAttachmentDir()), "attachments");
        createDirectory(new File(dcc.getModuleDir()), "modules");
        createDirectory(new File(dcc.getDatabaseDir()), "database");
        createDirectory(new File(dcc.getImageDir()), "images");
        createDirectory(new File(dcc.getReportDir()), "reports");
        createDirectory(new File(dcc.getModuleSettingsDir()), "moduleSettingsDir");
        createDirectory(new File(dcc.getApplicationSettingsDir()), "applicationSettingsDir");
        createDirectory(new File(dcc.getResourcesDir()), "resourcesDir");
        createDirectory(new File(dcc.getUpgradeDir()), "upgradeDir");
        
        if (DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_SERVER)
        	createDirectory(new File(dcc.getWebDir()), "datacrow/static/");
    }

    /** 
     * Creates the directories for the client where necessary.
     */
    private void createClientDirectories() {
        DcConfig dcc = DcConfig.getInstance();
        String userDir = dcc.getDataDir();
        String installDir = dcc.getInstallationDir();
        
        dcc.setReportDir(userDir + "reports/");
        dcc.setModuleDir(userDir + "modules/");
        dcc.setResourcesDir(userDir + "resources/");
        dcc.setModuleSettingsDir(userDir + "settings/modules/");
        dcc.setApplicationSettingsDir(userDir + "settings/application/");
        dcc.setPluginsDir(installDir + "plugins/");
        dcc.setServicesDir(userDir + "services/");
        dcc.setIconsDir(userDir + "images/icons/");
        dcc.setAttachmentDir(userDir + "attachments/");
        
        createDirectory(new File(dcc.getAttachmentDir()), "attachments");
        createDirectory(new File(dcc.getIconsDir()), "icons");
        createDirectory(new File(dcc.getReportDir()), "reports");
        createDirectory(new File(dcc.getModuleDir()), "modules");
        createDirectory(new File(dcc.getResourcesDir()), "resourcesDir");
        createDirectory(new File(dcc.getModuleSettingsDir()), "moduleSettingsDir");
        createDirectory(new File(dcc.getApplicationSettingsDir()), "applicationSettingsDir");
        createDirectory(new File(dcc.getDataDir(), "data"), "data");
    }

    /**
     * Create the specified directory.
     * @param dir   the directory to be created.
     * @param name  the name of the folder, for logging purposes.
     */
    private void createDirectory(File dir, String name) {
        dir.mkdirs();
        
        if (!dir.exists()) {
            String message = "Data Crow was unable to create the " + name + " directory (" + dir + "). " +
                "This indicates that the user running Data Crow has insufficient permissions. " +
                "The user running Data Crow must have full control over the Data Crow folder " +
                "and all if its sub directories.";

            client.notifyFatalError(message);
        }
        
        File file = new File(dir, "temp.txt");
        try {
        
            if (!file.exists())
                file.createNewFile();
            
            if (!file.exists() || !file.canWrite()) 
                throw new IOException("File cannot be created in directory " + dir);

        } catch (Throwable e) {
             String message = "Data Crow does not have permissions to modify files in the " + name + " directory. " +
                "This indicates that the user running Data Crow has insufficient permissions. " +
                "The user running Data Crow must have full control over the Data Crow folder and all of its sub folders. " +
                "Please correct this before starting Data Crow again (see the documentation of your operating system).";

            client.notifyFatalError(message);
        } finally {
            file.delete();
        }
    }
    
    /**
     * Log the Data Crow session information.
     */
    private void printConfiguration() {
        DcConfig dcc = DcConfig.getInstance();
        logger.info("Using installation directory: " + dcc.getInstallationDir());
        logger.info("Using user directory: " + dcc.getDataDir());
        logger.info("Using images directory: " + dcc.getImageDir());
        logger.info("Using database directory: " + dcc.getDatabaseDir());
    }
    
    /**
     * Check the platform for suitability for running Data Crow.
     */
    private void checkPlatform() {
        logger.info(DcConfig.getInstance().getVersion().getFullString());
        logger.info("Java version: " + System.getProperty("java.version"));
        logger.info("Java vendor: " + System.getProperty("java.vendor"));
        logger.info("Operating System: " + System.getProperty("os.name"));
    }
}
