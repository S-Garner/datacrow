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

import javax.swing.JButton;
import javax.swing.JToolBar;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.components.DcPictureField;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.resources.DcResources;

public class DcPictureFieldMenu extends JToolBar {
    
    public DcPictureFieldMenu(DcPictureField pf) {
        build(pf);
    }
        
    private void build(DcPictureField pf) {
    	
    	//PluginHelper.add(this, "NewItemWizard");
    	
    	JButton btnSaveAs = ComponentFactory.getIconButton(IconLibrary._icoPictureSave);
    	btnSaveAs.setToolTipText(DcResources.getText("lblSaveAs"));
    	btnSaveAs.setActionCommand("Save as");
    	btnSaveAs.addActionListener(pf);    	
    	
    	JButton btnAddFromFile = ComponentFactory.getIconButton(IconLibrary._icoPictureAddFromFile);
    	btnAddFromFile.setToolTipText(DcResources.getText("lblOpenFromFile"));
    	btnAddFromFile.setActionCommand("open_from_file");
    	btnAddFromFile.addActionListener(pf);    	

    	JButton btnAddFromURL = ComponentFactory.getIconButton(IconLibrary._icoPictureAddFromURL);
    	btnAddFromURL.setToolTipText(DcResources.getText("lblOpenFromURL"));
    	btnAddFromURL.setActionCommand("open_from_url");
    	btnAddFromURL.addActionListener(pf);    	

    	JButton btnAddFromClipboard = ComponentFactory.getIconButton(IconLibrary._icoPictureAddFromMemory);
    	btnAddFromClipboard.setToolTipText(DcResources.getText("lblOpenFromClipboard"));
    	btnAddFromClipboard.setActionCommand("open_from_clipboard");
    	btnAddFromClipboard.addActionListener(pf);    	
    	
    	JButton btnRotateRight = ComponentFactory.getIconButton(IconLibrary._icoRotateRight);
    	btnRotateRight.setToolTipText(DcResources.getText("lblRotateRight"));
    	btnRotateRight.setActionCommand("rotate_right");
    	btnRotateRight.addActionListener(pf);

    	JButton btnRotateLeft = ComponentFactory.getIconButton(IconLibrary._icoRotateLeft);
    	btnRotateLeft.setToolTipText(DcResources.getText("lblRotateLeft"));
    	btnRotateLeft.setActionCommand("rotate_left");
    	btnRotateLeft.addActionListener(pf);
    	
    	JButton btnGreyscale = ComponentFactory.getIconButton(IconLibrary._icoGrayscale);
    	btnGreyscale.setToolTipText(DcResources.getText("lblGrayscale"));	
    	btnGreyscale.setActionCommand("grayscale");
        btnGreyscale.addActionListener(pf);    	

    	JButton btnSharpen = ComponentFactory.getIconButton(IconLibrary._icoGrayscale);
    	btnSharpen.setToolTipText(DcResources.getText("lblSharpen"));
    	btnSharpen.setActionCommand("sharpen");
    	btnSharpen.addActionListener(pf);    	
    	
    	JButton btnBlur = ComponentFactory.getIconButton(IconLibrary._icoGrayscale);
    	btnBlur.setToolTipText(DcResources.getText("lblBlur"));
    	btnBlur.setActionCommand("blur");
    	btnBlur.addActionListener(pf);    	
    	
    	JButton btnDelete = ComponentFactory.getIconButton(IconLibrary._icoDelete);
    	btnDelete.setToolTipText(DcResources.getText("lblDelete"));	    	
    	btnDelete.setActionCommand("delete");
    	btnDelete.addActionListener(pf);
        
    	add(btnSaveAs);
    	
    	addSeparator();
    	
        add(btnAddFromFile);
        add(btnAddFromURL);
        add(btnAddFromClipboard);
        
        addSeparator();
        
        add(btnRotateLeft);
        add(btnRotateRight);
        
        addSeparator();
        
        add(btnGreyscale);
        add(btnSharpen);
        add(btnBlur);
        
        addSeparator();
        
        add(btnDelete);
    }
}
