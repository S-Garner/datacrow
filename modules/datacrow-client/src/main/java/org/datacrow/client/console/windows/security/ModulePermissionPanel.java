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

package org.datacrow.client.console.windows.security;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.lists.SecurityModuleList;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Permission;
import org.datacrow.core.objects.helpers.User;
import org.datacrow.core.resources.DcResources;

public class ModulePermissionPanel extends JPanel {

	private java.util.Map<Integer, FieldPermissionPanel> modulePermissionPanels = 
		new HashMap<Integer, FieldPermissionPanel>();
    
    public ModulePermissionPanel(DcObject user, boolean update) {
        
    	SecurityModuleList sml = new SecurityModuleList(this);
    	setLayout(Layout.getGBL());
    	
    	JLabel label = ComponentFactory.getLabel(DcResources.getText("lblModules"));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setBorder(BorderFactory.createEtchedBorder());
    	
        add(new JScrollPane(sml),  Layout.getGBC(0, 0, 1, 1, 1.0, 1.0, 
            GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 0, 0), 0, 0));
    	
        FieldPermissionPanel modulePanel;
        for (DcModule module : DcModules.getSecuredModules()) {
            modulePanel = new FieldPermissionPanel(module, user, update);
            
            modulePanel.setEnabled(!((User) user).isAdmin());
            modulePermissionPanels.put(module.getIndex(), modulePanel);
            
            add(modulePanel, Layout.getGBC(1, 0, 1, 1, 100.0, 100.0, 
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        }
    }
    
    public Collection<Permission> getPermissions(boolean changedOnly) {
    	Collection<Permission> permissions = new ArrayList<Permission>();
    	for (FieldPermissionPanel panel : modulePermissionPanels.values()) {
    		permissions.addAll(panel.getPermissions(changedOnly));
    	}
    	return permissions;
    }
    
    public boolean isChanged() {
    	for (FieldPermissionPanel panel : modulePermissionPanels.values())
    		if (panel.isChanged()) return true;
    	
    	return false;
    }

    public void clear() {
    	for (FieldPermissionPanel panel : modulePermissionPanels.values()) {
    		panel.clear();
    	}
    	modulePermissionPanels.clear();
    	modulePermissionPanels = null;
    }
    
    public void setSelected(int module) {
    	for (FieldPermissionPanel panel : modulePermissionPanels.values())
    		panel.setVisible(false);
    	
    	modulePermissionPanels.get(module).setVisible(true);
    }
}
