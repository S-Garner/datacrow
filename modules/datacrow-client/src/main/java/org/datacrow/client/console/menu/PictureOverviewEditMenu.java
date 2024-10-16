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

import java.awt.event.ActionListener;

import javax.swing.JToolBar;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.components.DcButton;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.resources.DcResources;

public class PictureOverviewEditMenu extends JToolBar {
    
    public PictureOverviewEditMenu(ActionListener al, boolean newItemMode) {
    	
		DcButton miAddFile = ComponentFactory.getIconButton(IconLibrary._icoPictureAddFromFile);
		DcButton miAddURL = ComponentFactory.getIconButton(IconLibrary._icoPictureAddFromURL);
		DcButton miAddMemory = ComponentFactory.getIconButton(IconLibrary._icoPictureAddFromMemory);
		DcButton miDelete = ComponentFactory.getIconButton(IconLibrary._icoDelete);
		
		
        miDelete.setActionCommand("delete");
        miDelete.setToolTipText(DcResources.getText("lblDelete"));
        miDelete.addActionListener(al);
        
        miAddFile.setToolTipText(DcResources.getText("lblOpenFromFile"));
        miAddFile.setActionCommand("add_from_file");
        miAddFile.addActionListener(al);    	

    	miAddURL.setToolTipText(DcResources.getText("lblOpenFromURL"));
    	miAddURL.setActionCommand("add_from_url");
    	miAddURL.addActionListener(al);    	

    	miAddMemory.setToolTipText(DcResources.getText("lblOpenFromClipboard"));
    	miAddMemory.setActionCommand("add_from_clipboard");
    	miAddMemory.addActionListener(al);

    	if (!newItemMode) {
    		DcButton miOpen = ComponentFactory.getIconButton(IconLibrary._icoOpen);
    		
	        miOpen.setActionCommand("open");
	        miOpen.setToolTipText(DcResources.getText("lblOpen"));
	        miOpen.addActionListener(al);
	        
	        add(miOpen);
	        addSeparator();
    	}
    	
        add(miAddFile);
        add(miAddURL);
        add(miAddMemory);
        addSeparator();
        add(miDelete);
        addSeparator();
        
        if (!newItemMode) {
	        DcButton miSort = ComponentFactory.getIconButton(IconLibrary._icoSort);
	        
	    	miSort.setToolTipText(DcResources.getText("lblSort"));
	    	miSort.setActionCommand("sort");
	    	miSort.setToolTipText(DcResources.getText("lblSort"));
	    	miSort.addActionListener(al);
	        
	    	add(miSort);
    	}
    }
}
