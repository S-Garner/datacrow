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

package org.datacrow.web.model;

import java.util.ArrayList;
import java.util.List;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.data.DataFilter;
import org.datacrow.core.data.DataFilters;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.server.Connector;
import org.datacrow.core.utilities.definitions.DcFieldDefinition;
import org.datacrow.core.utilities.definitions.DcFieldDefinitions;
import org.datacrow.web.DcBean;

/**
 * Data model for the search page. 
 */
public class Items extends DcBean {

    private List<Item> items = new ArrayList<Item>();
    
    private int moduleIdx;
	private String name;
    private String searchString;
    
    private int[] overviewFields = new int[] {};
    
    public Items() {}
    
    public Items(int module) {
        this();
        
        moduleIdx = module;
    	name = DcModules.get(module).getObjectNamePlural();
    	
    	setOverviewFields();
    }
    
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }
    
    public String getSearchString() {
        return searchString;
    }
    
    public void search() {
        
        setOverviewFields();
        
        Connector conn = DcConfig.getInstance().getConnector();
        int[] indices = getOverviewFields();
        
        DataFilter df = DataFilters.createSearchAllFilter(moduleIdx, searchString);
        df = df == null ? new DataFilter(moduleIdx) : df;
        
        List<DcObject> c = conn.getItems(df, indices);
        setItems(c);
    }
    
    public void setOverviewFields() {
        List<Integer> c = new ArrayList<Integer>();
        
        DcModule m = DcModules.get(moduleIdx);
        
        int[] fields = m.getSettings().getIntArray(DcRepository.ModuleSettings.stWebOverviewFields);
        if (fields != null) {
            for (int field : fields) {
                if (m.getField(field) != null)
                    c.add(Integer.valueOf(field));
            }
        }

        // so - we don't have any web field definitions; let's add the normal field indices instead (descriptive property)
        if (c.size() == 0) {
            DcFieldDefinitions definitions = 
                    (DcFieldDefinitions) m.getSettings().getDefinitions(DcRepository.ModuleSettings.stFieldDefinitions);
            
            for (DcFieldDefinition def : definitions.getDefinitions()) {
                if (def.isDescriptive()) {
                    c.add(Integer.valueOf(def.getIndex()));
                }
            }
        }
        
        if (!c.contains(DcObject._ID))
            c.add(Integer.valueOf(DcObject._ID));
        
        overviewFields = new int[c.size()];
        int idx = 0;
        for (Integer i : c) 
            overviewFields[idx++] = i.intValue();
    }
    
    private int[] getOverviewFields() {
        return overviewFields;
    }    

    public void setItems(List<DcObject> data) {
        
        items.clear();
        
        for (DcObject dco : data)
            items.add(new Item(dco));
    }
    
    public List<Item> getItems() {
    	return items;
    }
    
    public String getName() {
        return name;
    }
}
