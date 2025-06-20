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

package org.datacrow.core.migration.itemexport;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;

public class ItemExporters {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ItemExporters.class.getName());
	
    private static final ItemExporters instance;
    private final Map<String, Class<?>> exporters = new HashMap<String, Class<?>>();
    
    static {
    	instance = new ItemExporters();
    }
    
    private ItemExporters() {
        exporters.put("CSV", CsvExporter.class);
        exporters.put("XML", XmlExporter.class);
    }

    public static ItemExporters getInstance() {
        return instance;
    }

    public Collection<ItemExporter> getExporters(int moduleIdx) {
    	LinkedList<ItemExporter> c = new LinkedList<ItemExporter>();
    	for (String key : exporters.keySet()) {
    		try {
    			c.add(getExporter(key, moduleIdx));
    		} catch (Exception e) {
    			logger.error(e, e);
    		}
    	}
    	
    	Collections.sort(c, new Comparator<ItemExporter>() {
            @Override
            public int compare(ItemExporter o1, ItemExporter o2) {
                return o1.hashCode() - o2.hashCode();
            }
        });
    	
    	return c;
    }
    
    public ItemExporter getExporter(String type, int moduleIdx) throws Exception {
        return getExporter(type, moduleIdx, ItemExporter._MODE_THREADED);
    }

    public ItemExporter getExporter(String type, int moduleIdx, int mode) throws Exception {
        Class<?> clazz = exporters.get(type.toUpperCase());
        if (clazz != null) {
            return (ItemExporter) clazz.getConstructors()[0].newInstance(
                    new Object[] {Integer.valueOf(moduleIdx), Integer.valueOf(mode), Boolean.TRUE});
        }
        
        throw new Exception("No item exporter found for " + type);
    }
}
