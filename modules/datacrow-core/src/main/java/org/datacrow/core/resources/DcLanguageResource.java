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

package org.datacrow.core.resources;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;

/**
 * Represents the resources for a language (labels and messages).
 * 
 * @author Robert Jan van der Waals
 */
public class DcLanguageResource {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DcLanguageResource.class.getName());
    
    public static final String suffix = "resources.properties";
    
    private final Map<String, String> resources = new HashMap<String, String>();
    
    private final File file;
    private final String language;
    
    public DcLanguageResource(String language, File file) {
        this.language = language;
        this.file = file;
        load();
    }
    
    public Map<String, String> getResourcesMap() {
        return resources;
    }
    
    public String get(String key) {
        String value = resources.get(key);
        return value == null ? resources.get(key.toLowerCase()) : value;
    }
    
    /**
     * Updates or inserts the value for the given key. 
     * @param key
     * @param value
     */
    public void put(String key, String value) {
        resources.put(key, value);
    }
    
    /**
     * Adds the values and keys from the provided map to the existing resources
     * (if these do not yet exist). 
     * @param m Map to load the information from.
     */
    public void merge(DcLanguageResource resource) {
        for (String key : resource.getResourcesMap().keySet()) {
            if (!resources.containsKey(key))
                resources.put(key, resource.getResourcesMap().get(key));
        }
    }
    
    /**
     * Saves the resources to file.
     */
    public void save() {
        try {
            file.delete();
            
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            int count = 0;
            for (String key : resources.keySet()) {
                String value = key + "=" + resources.get(key) + "\r\n";
                bos.write(value.getBytes("UTF8"));
                count++;
                if (count == 2000) {
                    bos.flush();
                    count = 0;
                }
            }
            
            bos.flush();
            bos.close();
        } catch (Exception e) {
            logger.error("Could not save resources for language " + language, e);
        }
    }
    
    /**
     * Load the resources from file.
     */
    private void load() {
        Scanner scanner = null;
        try {
            
            scanner = new Scanner(file, "UTF-8");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                String[] values = line.split("=");
                if (values != null && values.length >= 2) {
                    String value = values[1];
                    if (values.length > 2) {
                        for (int i = 2;i < values.length; i++)
                            value += " " + values[i];
                    }
                    resources.put(values[0].trim(), value.trim());
                }
            }
        } catch (Exception e) {
            if (!"English".equals(language))
                logger.info("Could not load resources for language " + language);
        } finally {
            if (scanner != null) scanner.close();
        }
    }
}
