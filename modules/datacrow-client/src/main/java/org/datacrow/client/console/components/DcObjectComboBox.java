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

package org.datacrow.client.console.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datacrow.core.DcConfig;
import org.datacrow.core.data.DataFilter;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.server.Connector;

public class DcObjectComboBox extends DcComboBox<Object> {

    private final int module;
    
    public DcObjectComboBox(int module) {
        super();
        this.module = module;
        refresh();
    }
    
    @Override
    public void setValue(Object value) {
        setSelectedItem(value);
        
        if (getSelectedItem() != value) { // 0 == empty value
            if (value != null) {
            	boolean existing = false;
            	
            	Object o;
                for (int i = 0; i < getItemCount(); i++) {
                    o = getItemAt(i);
                    if (o.equals(value)) {
                        setSelectedItem(o);
                        existing = true;
                    }
                }
                
                if (!existing) {
                    Connector conn = DcConfig.getInstance().getConnector();
                    Object dco = (value instanceof DcObject) ? value : conn.getItem(module, (String) value);
                    addItem(dco);
                    setSelectedItem(dco);
                }
            }
        }
    }
    
    public void remove(Collection<? extends DcObject> remove) {
        Collection<DcObject> newValues = new ArrayList<DcObject>();
        Object value;
        for (int i = 0; i < dataModel.getSize(); i++) {
            value = dataModel.getElementAt(i);
            if (value instanceof DcObject && !remove.contains(value))
                newValues.add((DcObject) value);
        }
        
        removeAllItems();
        
        addItem(" ");
        for (DcObject dco : newValues)
            addItem(dco);        
    }

    @Override
    public void refresh() {
        Object o = getSelectedItem();
  
        Collection<DcObject> newValues = new ArrayList<DcObject>();
        Object value;
        for (int i = 0; i < dataModel.getSize(); i++) {
            value = dataModel.getElementAt(i);
            if (value instanceof DcObject && ((DcObject) value).isNew())
                newValues.add((DcObject) value);
        }
        
        removeAllItems();
        addItem(" ");
        
        for (DcObject dco : newValues)
            addItem(dco);

        Connector connector = DcConfig.getInstance().getConnector();
        
        DataFilter df = new DataFilter(module);
        df.setOrder(DcModules.get(module).getDescriptiveFields());
        
        List<DcObject> items = connector.getItems(df, DcModules.get(module).getMinimalFields(null));
        for (DcObject dco : items)
            addItem(dco);

        if (o != null)
            setSelectedItem(o);
        else
            setSelectedIndex(0);
        
        revalidate();
    }
}
