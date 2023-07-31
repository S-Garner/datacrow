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

package org.datacrow.core.migration.itemimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.migration.ItemMigrater;

public class ItemImporters {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ItemImporters.class.getName());
	
    private static final ItemImporters instance;
    
    private final Map<String, Class<?>> importers = new HashMap<String, Class<?>>();
    
    static {
    	instance = new ItemImporters();
    }
    
    private ItemImporters() {
        importers.put("CSV", CsvImporter.class);
        importers.put("XML", XmlImporter.class);
    }

    public static ItemImporters getInstance() {
        return instance;
    }

    public Collection<ItemImporter> getImporters(int moduleIdx) {
    	Collection<ItemImporter> c = new ArrayList<ItemImporter>();
    	for (String key : importers.keySet()) {
    		try {
    			c.add(getImporter(key, moduleIdx));
    		} catch (Exception e) {
    			logger.error(e, e);
    		}
    	}
    	return c;
    }
    
    /**
     * Gets a (threaded) importer which can handle the specified file type.
     * This method only looks at the default (not module specific) importers.
     * @param type
     * @param moduleIdx
     * @throws Exception
     */
    public ItemImporter getImporter(String type, int moduleIdx) throws Exception {
        return getImporter(type, moduleIdx, ItemMigrater._MODE_THREADED);
    }
    
    /**
     * Gets an importer which can handle the specified file type.
     * This method only looks at the default (not module specific) importers.
     * @param type
     * @param moduleIdx
     * @param mode
     * @throws Exception
     */
    public ItemImporter getImporter(String type, int moduleIdx, int mode) throws Exception {
        Class<?> clazz = importers.get(type.toUpperCase());
        if (clazz != null) {
            return (ItemImporter) clazz.getConstructors()[0].newInstance(
                    new Object[] {Integer.valueOf(moduleIdx), Integer.valueOf(mode)});
        }

        throw new Exception("No item importer found for " + type);
    }
}
