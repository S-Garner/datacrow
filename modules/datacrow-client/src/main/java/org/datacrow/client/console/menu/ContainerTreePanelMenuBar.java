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
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.components.DcMenu;
import org.datacrow.client.console.components.DcMenuItem;
import org.datacrow.client.console.components.panels.tree.TreePanel;
import org.datacrow.core.DcRepository;
import org.datacrow.core.console.IMasterView;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.Settings;

public class ContainerTreePanelMenuBar extends TreePanelMenuBar {

    public ContainerTreePanelMenuBar(int modIdx, TreePanel treePanel) {
        super(modIdx, treePanel);
        
        DcMenu menuView = ComponentFactory.getMenu(DcResources.getText("lblView"));

        DcMenuItem menuViewFlat = ComponentFactory.getMenuItem(DcResources.getText("lblFlatView"));
        DcMenuItem menuViewHierarchy = ComponentFactory.getMenuItem(DcResources.getText("lblHierarchicalView"));
        DcMenuItem menuViewContainers = ComponentFactory.getMenuItem(DcResources.getText("lblViewContainers"));
        DcMenuItem menuViewItems = ComponentFactory.getMenuItem(DcResources.getText("lblViewItems"));
        
        menuView.add(menuViewFlat);
        menuView.add(menuViewHierarchy);
        menuView.addSeparator();
        menuView.add(menuViewContainers);
        menuView.add(menuViewItems);
        
        menuViewFlat.setActionCommand("flatView");
        menuViewHierarchy.setActionCommand("hierView");
        menuViewContainers.setActionCommand("viewContainers");
        menuViewItems.setActionCommand("viewItems");
        
        menuViewFlat.addActionListener(this);
        menuViewHierarchy.addActionListener(this);
        menuViewContainers.addActionListener(this);
        menuViewItems.addActionListener(this);
        
        add(menuView);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Settings settings = DcModules.get(DcModules._CONTAINER).getSettings();
        IMasterView mv = GUI.getInstance().getSearchView(DcModules._CONTAINER);
        
        if (ae.getActionCommand().equals("viewContainers")) {
            settings.set(DcRepository.ModuleSettings.stTreePanelShownItems, Long.valueOf(DcModules._CONTAINER));
            mv.applySettings();
        } else if (ae.getActionCommand().equals("viewItems")) {
            settings.set(DcRepository.ModuleSettings.stTreePanelShownItems, Long.valueOf(DcModules._ITEM));
            mv.applySettings();
        } else if (ae.getActionCommand().equals("flatView")) {
            settings.set(DcRepository.ModuleSettings.stContainerTreePanelFlat, Boolean.TRUE);
            mv.applySettings();
            treePanel.reset();
        } else if (ae.getActionCommand().equals("hierView")) {
            settings.set(DcRepository.ModuleSettings.stContainerTreePanelFlat, Boolean.FALSE);
            mv.applySettings();
            treePanel.reset();
        } else {
            super.actionPerformed(ae);
        }
    }
}
