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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.components.DcMenu;
import org.datacrow.client.console.components.DcMenuBar;
import org.datacrow.client.console.components.DcMenuItem;
import org.datacrow.client.console.components.panels.tree.TreePanel;
import org.datacrow.core.resources.DcResources;

public class TreePanelMenuBar extends DcMenuBar implements ActionListener {
    
    protected final int modIdx;
    protected final TreePanel treePanel;
    
    public TreePanelMenuBar(int modIdx, TreePanel treePanel) {
        this.modIdx = modIdx;
        this.treePanel = treePanel;

        DcMenu menuEdit = ComponentFactory.getMenu(DcResources.getText("lblEdit"));
        DcMenuItem menuExpandAll = ComponentFactory.getMenuItem(DcResources.getText("lblExpandAll"));
        DcMenuItem menuCollapseAll = ComponentFactory.getMenuItem(DcResources.getText("lblCollapseAll"));
        
        menuCollapseAll.addActionListener(this);
        menuCollapseAll.setActionCommand("collapseAll");
        
        menuExpandAll.addActionListener(this);
        menuExpandAll.setActionCommand("expandAll");
        
        menuEdit.add(menuCollapseAll);
        menuEdit.add(menuExpandAll);
        
        

        
        setPreferredSize(new Dimension(100, 22));
        setMaximumSize(new Dimension(100, 22));
        setMinimumSize(new Dimension(50, 22));
        
        
        add(menuEdit);
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("collapseAll")) {
            treePanel.collapseChildren(treePanel.getTopNode());
        } else if (ae.getActionCommand().equals("expandAll")) {
            treePanel.expandChildren(treePanel.getTopNode());
        } else {
            
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(GUI.getInstance().setRenderingHint(g));
    }
}

