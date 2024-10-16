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

package org.datacrow.core.settings;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.objects.DcColor;
import org.datacrow.core.settings.objects.DcDimension;
import org.datacrow.core.settings.objects.DcFont;
import org.datacrow.core.settings.objects.DcLookAndFeel;
import org.datacrow.core.utilities.definitions.IDefinitions;

public class Settings implements Serializable {
    
	private static final long serialVersionUID = 1L;

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(Settings.class.getName());

    private File settingsFile;
    private LinkedHashMap<String, SettingsGroup> groups = new LinkedHashMap<String, SettingsGroup>();
	
    public Settings() {
        createGroups();
    }
    
    protected void load() {
        try {
            SettingsFile.load(this);
        } catch (Exception e) {
            logger.error(DcResources.getText("msgFailedToLoadUserSettings"), e);
        }
    }
    
    /**
     * Retrieves all the top level groups
     */    
    public LinkedHashMap<String, SettingsGroup> getSettingsGroups() {
        return groups;   
    }
    
    /**
     * Adds a group to the hash table. A group may contain sub-groups.
     * Only the parent, to which a sub-group belongs, should be added.
     * 
     * @param key unique identifier for this group
     * @param group a top-level group (parent)
     */ 
    public void addGroup(String key, SettingsGroup group) {
        groups.put(key, group);
    }

    /**
     * Specifies the location and name of the settings file
     * 
     * @param file the settings file
     */
    public void setSettingsFile(File file) {
        settingsFile = file;
    }
    
    public File getSettingsFile() {
        return settingsFile;   
    }

    /**
     * Retrieves a settings group with the key
     */
    private SettingsGroup getSettingGroup(String key) {
        Collection<SettingsGroup> groups = getGroups();
        for (SettingsGroup group : groups) {
            if (group.getKey().equals(key))
                return group; 
        }
        
        logger.debug("Settings group with key [" + key + "] does not exist");
        return null;
    }

    /**
     * Adds a setting to a specified group. A group can be either
     * a parent or a child.
     * 
     * @param key the key of the group
     * @param setting the setting to be added to the specified group
     */
    public void addSetting(String key, Setting setting) {
        SettingsGroup stGroup = getSettingGroup(key);
        stGroup.add(setting);
    }

    public boolean isSettingKeyValid(String key) {
        boolean bValid = getSetting(key) == null ? false : true;
        return bValid;
    }

    /**
     * Retrieves a setting
     */
    public Setting getSetting(String key) {
        for (SettingsGroup group : groups.values()) {
            Setting setting = group.getSetting(key);
            if (setting != null) return setting;
        }
        return null;
    }

    /**
     * Retrieves a value of a settings
     */
    public Object getValue(String key) {
        Setting setting = getSetting(key);
        return setting != null ? setting.getValue() : null;
    }

    /**
     * Returns the value of the setting as a String array
     */
    public String[] getStringArray(String key) {
        return (String[]) getValue(key);
    }
    
    public int[] getIntArray(String key) {
        return (int[]) getValue(key);
    }

    public IDefinitions getDefinitions(String key) {
        return (IDefinitions) getValue(key);
    }      
    
    public DcFont getFont(String key) {
        return (DcFont) getValue(key);
    }    

    public DcLookAndFeel getLookAndFeel(String key) {
        return (DcLookAndFeel) getValue(key);
    } 
    
    /**
     * Returns the value of the setting as a boolean
     */
    public boolean getBoolean(String key) {
        Object o = getValue(key);
        return o == null ? false : ((Boolean) o).booleanValue();
    }

    /**
     * Returns the value of the setting as a color object
     */
    public DcColor getColor(String key) {
        return (DcColor) getValue(key);
    }
    
    public DcDimension getDimension(String key) {
        return (DcDimension) getValue(key);
    }

    public String getString(String key) {
        Object o = getValue(key);
        return o != null ? o.toString() : null;
    }

    /**
     * Sets the value of a setting
     */
    public void setValue(String key, Object value) {
        Setting setting = getSetting(key);
        if (setting != null) {
        	ISettingsValueConverter converter = DcSettings.getConverter();
        	Object o = converter != null ? converter.convert(value) : value;
        	setting.setValue(o);
        }
    }

    /**
     * Sets a string as a value for the setting (by parsing the string)
     */
    public void setString(String key, String s) {
        Setting setting = getSetting(key);
        if (setting != null)
            setting.setStringAsValue(s);
    }

    protected void createGroups() {}

    /**
     * Retrieves all settings groups without an hierarchy
     */
    public Collection<SettingsGroup> getGroups() {
        Collection<SettingsGroup> c = new ArrayList<SettingsGroup>();
        c.addAll(groups.values());

        for (SettingsGroup group : groups.values())
            c.addAll(group.getChildren().values());

        return c;
    }
    
    /**
     * Retrieves all the settings
     */
    public Collection<Setting> getSettings() {
        Collection<Setting> settings = new ArrayList<Setting>();
        for (SettingsGroup group : getGroups())
            settings.addAll(group.getSettings().values());
        
        return settings;
    }
    
    public boolean contains(String key) {
        return getSetting(key) != null;
    }
    
    public void save() {
        SettingsFile.save(this);
    }    
    
    public Object get(String key) {
        return getValue(key);
    }

    public void set(String key, Object value) {
        setValue(key, value);
    }

    public long getLong(String key) {
        Object o = get(key);
        return o instanceof Long ? ((Long) o).longValue() : 0l;
    }    
    
    public int getInt(String key) {
        Object o = get(key);
        o = o == null ? -1 : o;
        return o instanceof Integer ? ((Integer) o).intValue() :
               o instanceof Long ? ((Long) o).intValue() : 0;
    }

    public void addIntsToIntArray(String key, Collection<Integer> add) {
    	
    	if (add == null || add.size() == 0) return;
    	
    	List<Integer> current = new ArrayList<>();

    	for (int fieldIdx : getIntArray(key))
    		current.add(Integer.valueOf(fieldIdx));

    	for (Integer fieldIdx : add) {
    		if (!current.contains(fieldIdx))
    			current.add(fieldIdx);
    	}
	
    	int[] value = new int[current.size()];
    	for (int i = 0; i < value.length; i++)
    		value[i] = current.get(i).intValue();
    	
    	setValue(key, value);
    }
    
    public void removeIntsFromIntArray(String key, Collection<Integer> remove) {
    	
    	if (remove == null || remove.size() == 0) return;
    	
    	List<Integer> cleaned = new ArrayList<>();

    	for (int fieldIdx : getIntArray(key)) {
    		if (!remove.contains(Integer.valueOf(fieldIdx)))
    			cleaned.add(Integer.valueOf(fieldIdx));
    	}
	
    	int[] value = new int[cleaned.size()];
    	for (int i = 0; i < value.length; i++)
    		value[i] = cleaned.get(i).intValue();
    	
    	setValue(key, value);
    }
}
