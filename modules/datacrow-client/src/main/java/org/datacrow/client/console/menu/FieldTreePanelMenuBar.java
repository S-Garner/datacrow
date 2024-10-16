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

package org.datacrow.client.console.menu;

import java.awt.event.ActionEvent;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.components.DcMenu;
import org.datacrow.client.console.components.DcMenuItem;
import org.datacrow.client.console.components.panels.tree.TreePanel;
import org.datacrow.client.console.windows.GroupByDialog;
import org.datacrow.core.resources.DcResources;

public class FieldTreePanelMenuBar extends TreePanelMenuBar {

    public FieldTreePanelMenuBar(int modIdx, TreePanel treePanel) {
        super(modIdx, treePanel);
        
        DcMenu menuFields = ComponentFactory.getMenu(DcResources.getText("lblGroupBy"));
        
        DcMenuItem miGroupBy = ComponentFactory.getMenuItem(DcResources.getText("msgSelectFields"));
        miGroupBy.setActionCommand("groupBy");
        miGroupBy.addActionListener(this);
        menuFields.add(miGroupBy);
        
        add(menuFields);
    }
    
    private void groupBy(String s) {
        GroupByDialog dlg = new GroupByDialog(modIdx);
        dlg.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("groupBy")) {
            groupBy(ae.getActionCommand());
        } else {
            super.actionPerformed(ae);
        }
    }
}
