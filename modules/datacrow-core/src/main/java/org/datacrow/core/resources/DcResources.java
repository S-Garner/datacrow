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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.CoreUtilities;

/**
 * This class gives access to all labels, messages and tooltips for all languages.
 * The default language is English. Custom languages inherit from the default English language.  
 * 
 * @author Robert Jan van der Waals
 */
public class DcResources { 
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DcResources.class.getName());
    
    private static final Map<String, DcLanguageResource> resources = new HashMap<String, DcLanguageResource>();
    
    private static boolean initialized = false;
    
    /**
     * Creates a new instance and loads all resources.
     */
    public DcResources() {
        initialize();
    }
    
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Loads the default language (English) and the custom languages.
     */
    public void initialize() {
        resources.clear();
        
        String[] propertyFiles = {"DcLabels.properties", 
                                  "DcMessages.properties", 
                                  "DcTooltips.properties",
                                  "DcAudioCodecs.properties",
                                  "DcTips.properties"}; 
        
        DcLanguageResource english = new DcLanguageResource(
                "English",
                new File(DcConfig.getInstance().getResourcesDir(), "English" + "_resources.properties"));
        
        InputStream is1 = null;
        InputStream is2 = null;
        for (String propertyFile : propertyFiles) {
            Properties p = new Properties();
            try {
            	is1 = getClass().getResourceAsStream(propertyFile);
                p.load(is1);
            } catch (Exception ignore) {
                try {
                	is2 = getClass().getResourceAsStream("org/datacrow/core/resources/" + propertyFile);
                    p.load(is2);
                } catch (Exception e) {
                    logger.error("Could not load custom resource files. Falling back to the default resources.", e);
                } finally {
                	try { if (is2 != null) is2.close(); } catch (Exception e) {logger.error("Could not close language input stream");}	
                }
            } finally {
            	try { if (is1 != null) is1.close(); } catch (Exception e) {logger.error("Could not close language input stream");}
            }
            
            for (Object o : p.keySet()) {
                String key = (String) o;
                english.put(key, p.getProperty(key));
            }
        }
        
        resources.put("English", english);
        
        File localFile;   
        File installFolderFile;
        DcLanguageResource localResource;
        DcLanguageResource installResource;
        for (String language : getLanguages()) {
            localFile = new File(DcConfig.getInstance().getResourcesDir(), language + "_resources.properties");
            installFolderFile = new File(new File(DcConfig.getInstance().getInstallationDir(), "resources"), language + "_resources.properties"); 
            
            localResource = new DcLanguageResource(language, localFile);
            
            if (installFolderFile.exists()) {
                installResource = new DcLanguageResource(language, installFolderFile);
                
                // add newly introduced translations
                localResource.merge(installResource);
            }
                
            // add English values for missing translations 
            localResource.merge(english);
                
            addLanguageResource(language, localResource);
        }
        
        initialized = true;
    }
    
    public static void addLanguageResource(String language, DcLanguageResource lr) {
        resources.put(language, lr);
    }
    
    /**
     * Retrieves all the language resources.
     */
    public static Collection<DcLanguageResource> getLanguageResources() {
        return resources.values();
    }
    
    public static DcLanguageResource getLanguageResource(String language) {
        return resources.get(language);
    }
    
    /**
     * Retrieves all available languages. Language reside in the resources folder.
     * A language file has the following name: &lt;language&gt;_resources.properties.
     */
    public static Collection<String> getLanguages() {
    	Collection<String> languages = new ArrayList<String>();
    	
    	if (DcConfig.getInstance().getResourcesDir() != null) {
	        String[] files = new File(DcConfig.getInstance().getResourcesDir()).list();
	        
	        if (files != null) {
	            for (String file : files) {
	                if (file.toLowerCase().endsWith("resources.properties") && file.length() > "resources.properties".length() + 1)
	                    languages.add(file.substring(0, file.toLowerCase().indexOf("resources.properties") - 1));
	            }
	        }
    	}
        
        if (!languages.contains("English"))
            languages.add("English");
        
        return languages;
    }

    /**
     * The currently used language resource.
     */
    public static DcLanguageResource getCurrent() {
        String language = "English";

        try {
            language = DcSettings.getString(DcRepository.Settings.stLanguage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (CoreUtilities.isEmpty(language))
        	language = "English";
        
        return resources.get(language);
    }
    
    public static String getText(String id) {
        if (id == null) {
            logger.debug("Empty message passed to the DcResources class! Returning empty String!");
            return "";
        }
        
    	return getText(id, (String[]) null);
    }

    public static String getText(String id, String param) {
    	return getText(id, new String[] {param});
    }
    
    public static Collection<String> getTextAllLanguages(String id, String[] params) {
        Collection<String> result = new ArrayList<String>();
        for (DcLanguageResource resource : resources.values()) {
        	String value = resource.get(id);	
        	if (params != null && value != null) 
        		value = insertParams(value, params);
        	
        	result.add(value);
        }
        
        return result;
    }
    
    public static String getText(String id, String[] params) {
        String value = getCurrent().get(id);
        return params == null || value == null ? value : insertParams(value, params);
    }

    private static String insertParams(String s, String[] params) {
    	String result = s;
    	for (int i = 1; i - 1 < params.length; i++) {
    	    String searchPat = "%" + i;
    	    int index = 0;
    	    boolean escapedPatFound = false;
    	    do {
        		index = result.indexOf(searchPat, index);
        		if (index < 0) 
        		    logger.debug("Could not insert the parameter for label " + s);
        		else
        		    escapedPatFound = (index > 0 && result.charAt(index - 1) == '\\');
    	    } while (escapedPatFound);

            StringBuffer sb = new StringBuffer(result);
            try {
            	sb.replace(index, index+searchPat.length(), params[i -1]);
            } catch (Exception e) {}
            
    	    result = sb.toString();
    	}
    	return result;
    }
}
