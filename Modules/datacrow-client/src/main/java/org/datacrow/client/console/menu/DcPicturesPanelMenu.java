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

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.components.panels.PicturesPanel;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.resources.DcResources;

public class DcPicturesPanelMenu extends JMenuBar {
    
    public DcPicturesPanelMenu(PicturesPanel pp) {
        build(pp);
    }
        
    private void build(PicturesPanel pp) {
        JMenu menuEdit = ComponentFactory.getMenu(DcResources.getText("lblEdit"));

        JMenuItem miAdd = ComponentFactory.getMenuItem(DcResources.getText("lblAdd"));
        JMenuItem miDelete = ComponentFactory.getMenuItem(DcResources.getText("lblDelete"));
        JMenuItem miOpen = ComponentFactory.getMenuItem(DcResources.getText("lblOpen"));
        
        miDelete.setActionCommand("delete");
        miDelete.setIcon(IconLibrary._icoDelete);
        miDelete.addActionListener(pp);
        
        miAdd.setActionCommand("add");
        miAdd.setIcon(IconLibrary._icoAdd);
        miAdd.addActionListener(pp);

        miOpen.setActionCommand("open");
        miOpen.setIcon(IconLibrary._icoOpen);
        miOpen.addActionListener(pp);
        
        menuEdit.add(miOpen);
        menuEdit.addSeparator();
        menuEdit.add(miAdd);
        menuEdit.add(miDelete);
        
        add(menuEdit);
    }
}
