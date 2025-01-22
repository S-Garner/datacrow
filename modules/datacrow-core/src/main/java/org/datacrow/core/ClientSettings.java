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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.UUID;

import org.datacrow.core.utilities.CoreUtilities;

/**
 * The client settings contains the selected user directory for this client.
 * The settings are stored to a properties file (datacrow_properties) in the user (user.home)
 * folder. 
 * 
 * @author Robert Jan van der Waals
 */
public class ClientSettings {
    
	private static final String _USERDIR = "user.home";
	private static final String _CLIENTID = "user.clientid";
    private static final String _SERVER_ADDRESS = "server.address";
    private static final String _SERVER_PORT = "server.port";
    private static final String _IMAGE_SERVER_ADDRESS = "imageserver.address";
    private static final String _IMAGE_SERVER_PORT = "imageserver.port";
    
    public static final String _CLIENT_UI_SCALING = "ui_scaling";

    private final File file = new File(System.getProperty("user.home"), "datacrow.properties");
    private final Properties properties = new Properties();
    
    /**
     * Creates a new instance and loads the data_crow.properties file from the user folder
     * (user_home) if it exists. The file is then saved with the client ID to the user folder.
     */
    protected ClientSettings() {
        
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                properties.load(fis);
                fis.close();
            } catch (Exception e) {
                e.printStackTrace(); // logger not yet available at this stage
            }
        }
        
        initClientID();
        save();
    }
    
    /**
     * Returns the file to which the properties are saved.
     * @return  the properties file
     */
    public File getFile() {
        return file;
    }
    
    /**
     * Stores the settings to the data_crow.properties files.
     */
    public void save() {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            properties.store(fos, "Data Crow system settings file. Better to leave it right here.");
            fos.close();
        } catch (Exception e) {
            e.printStackTrace(); // logger not yet available at this stage
        }  
    }
    
    public long getUIScaling() {
    	Object o = properties.get(_CLIENT_UI_SCALING);
    	return CoreUtilities.isEmpty(o) ? 100 : Long.valueOf(o.toString());
    }
    
    public int getServerPort() {
    	Object o = properties.get(_SERVER_PORT);
    	return CoreUtilities.isEmpty(o) ? 9000 : Integer.valueOf(o.toString());
    }
    
    public String getServerAddress() {
    	Object o = properties.get(_SERVER_ADDRESS);
    	return CoreUtilities.isEmpty(o) ? "localhost" : o.toString();    	
    }
    
    public int getImageServerPort() {
    	Object o = properties.get(_IMAGE_SERVER_PORT);
    	return CoreUtilities.isEmpty(o) ? 9001 : Integer.valueOf(o.toString());
    }
    
    public String getImageServerAddress() {
    	Object o = properties.get(_IMAGE_SERVER_ADDRESS);
    	return CoreUtilities.isEmpty(o) ? "localhost" : o.toString();    	
    }
    
    public void setServerDetails(
    		String serverAddress, 
    		int serverPort,
    		String imageServerAddress,
    		int imageServerPort) {
        
    	properties.setProperty(_SERVER_ADDRESS, serverAddress);
    	properties.setProperty(_SERVER_PORT, String.valueOf(serverPort));
    	properties.setProperty(_IMAGE_SERVER_ADDRESS, imageServerAddress);
    	properties.setProperty(_IMAGE_SERVER_PORT, String.valueOf(imageServerPort));
    }    
    
    public void setUiScaling(Long value) {
    	properties.setProperty(_CLIENT_UI_SCALING, value.toString());
    }
    
    /**
     * Checks whether the data_crow.properties file currently exists.
     * @return  exist y/n
     */
    public boolean exists() {
        return file.exists();
    }
    
    private void initClientID() {
        String clientID = properties.getProperty(_CLIENTID);
        if (clientID == null || clientID.trim().equals(""))
            properties.setProperty(_CLIENTID, UUID.randomUUID().toString());
    }
    
    /**
     * Retrieves the Data Crow user folder as stored in the properties filed.
     * @return  Data Crow user folder
     */
    public File getUserDir() {
        String userDir = (String) properties.get(_USERDIR);
        return CoreUtilities.isEmpty(userDir) ? null : new File(userDir);
    }
    
    /**
     * Sets the user folder, to be stored to the data_crow.properties file.
     * @param userDir   fully qualified path
     */
    public void setUserDir(String userDir) {
        properties.setProperty(_USERDIR, userDir);
        save();
    }
    
    /**
     * Get the Client ID for this installation. Each Data Crow installation holds
     * its own unique ID.
     * @return  Client ID (UUID)
     */
    public String getClientID() {
        return properties.getProperty(_CLIENTID);
    }
}
