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

public class PictureOverviewReorderMenu extends JToolBar {
    
    public PictureOverviewReorderMenu(ActionListener al) {
    	
		DcButton miSave = ComponentFactory.getIconButton(IconLibrary._icoSave);
		miSave.setActionCommand("save_order");
		miSave.setToolTipText(DcResources.getText("lblSave"));
		miSave.addActionListener(al);

		DcButton miCancel = ComponentFactory.getIconButton(IconLibrary._icoCancel);
		miCancel.setActionCommand("edit");
		miCancel.setToolTipText(DcResources.getText("lblCancel"));
		miCancel.addActionListener(al);
		
		add(miSave);
		add(miCancel);
    }
}
