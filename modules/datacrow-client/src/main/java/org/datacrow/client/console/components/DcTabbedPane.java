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

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.JToolTip;

import org.datacrow.client.console.GUI;
import org.datacrow.core.objects.DcImageIcon;

public class DcTabbedPane extends JTabbedPane {
	
    public DcTabbedPane() {
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
    	try {
    		super.paintComponent(GUI.getInstance().setRenderingHint(g));
    	} catch (Exception e) {}
    }
    
    @Override
    public void addTab(String title, Icon icon, Component component) {
    	Icon scaledIcon = icon != null ?  new DcImageIcon(((ImageIcon) icon).getImage()).toIcon() : null;
        super.addTab(title, scaledIcon, component);
    }
    
    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }
}
