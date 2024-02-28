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

package org.datacrow.client.console.windows.itemformsettings;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.fileselection.FieldSelectionPanel;
import org.datacrow.client.console.components.fileselection.IFieldSelectionListener;
import org.datacrow.client.tabs.Tab;
import org.datacrow.core.DcRepository.ValueTypes;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.definitions.DcFieldDefinition;
import org.datacrow.core.utilities.definitions.DcFieldDefinitions;

public class TabDesignPanel extends JPanel implements IFieldSelectionListener {

    private final String tabName;
    private final DcModule module;
	
    private FieldSelectionPanel pnlFields;
    private final List<Tab> tabs;
    
    public TabDesignPanel(DcModule module, Tab tab, List<Tab> tabs) {
        this.module = module;
        this.tabName = tab.getName();
        this.tabs = tabs;
        
        build();
        refresh();
    }

    @Override
    public void fieldSelected(DcField field) {
        field.getDefinition().setTab(tabName);
    }
    
    @Override
    public void fieldDeselected(DcField field) {
        field.getDefinition().setTab(null);
    }
    
    private String getTabName(String tab) {
        return tab != null && tab.startsWith("lbl") ? DcResources.getText(tab) : tab;
    }
    
    private boolean isAllowed(int fieldIdx) {
        DcField field = module.getField(fieldIdx);

        return  field.isEnabled() && 
               !field.isSystemField() &&
               !(field.isLoanField() && field.getIndex() != DcObject._SYS_LOANALLOWED) &&
                field.getIndex() != DcObject._SYS_DISPLAYVALUE &&
                field.getValueType() != ValueTypes._PICTURE &&
                field.getIndex() != DcObject._SYS_MODULE;
    }

    protected void save(DcFieldDefinitions definitions) {
        for (DcField field : pnlFields.getSelectedFields()) {
            definitions.add(field.getDefinition());
        }
    }
    
    private List<DcField> getSelectedFields() {
        List<DcField> fields = new ArrayList<DcField>();
        String tab;
        for (DcFieldDefinition def : module.getFieldDefinitions().getDefinitions()) {
            tab = def.getTab();
            
            if (CoreUtilities.isEmpty(tab)) continue;
                        
            if (isAllowed(def.getIndex()) && tabName.equalsIgnoreCase(getTabName(def.getTab())))
                fields.add(module.getField(def.getIndex()));
        }
        return fields;
    }
    
    private boolean isUnassigned(DcFieldDefinition definition) {
    	boolean unassigned = CoreUtilities.isEmpty(definition.getTab());
    	
    	// next, check if we have, for whatever reason, tabs assigned which do not exist
    	if (!unassigned) {
    		
    		unassigned = true;
    		
    		for (Tab tab : tabs) {
    			if (	tab.getName().equals(definition.getTab()) ||
    					tab.getName().equals(DcResources.getText(definition.getTab()))) {
    				unassigned = false;
    				break;
    			}
    		}
    	}
    	
    	return unassigned;
    }
    
    private List<DcField> getAvailableFields() {
        List<DcField> fields = new ArrayList<DcField>();
        for (DcFieldDefinition def : module.getFieldDefinitions().getDefinitions()) {
            if (isAllowed(def.getIndex()) && isUnassigned(def))
                fields.add(module.getField(def.getIndex()));
        }
        return fields;
    }
    
    protected void refresh() {
        List<DcField> fields = new ArrayList<DcField>();
        List<DcField> selected = getSelectedFields();
        fields.addAll(selected);
        fields.addAll(getAvailableFields());
        
        pnlFields.setFields(fields);
        pnlFields.setSelectedFields(selected);
    }
    
    private void build() {        
        setLayout(Layout.getGBL());
        pnlFields = new FieldSelectionPanel(module, false, true, true);
        pnlFields.setFieldSelectionListener(this);
        
        add(pnlFields,  Layout.getGBC( 0, 0, 1, 1, 40.0, 40.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 5, 5, 5), 0, 0));
    }
}
