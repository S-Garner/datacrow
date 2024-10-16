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

import java.awt.Graphics;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JToolTip;

import org.datacrow.client.console.GUI;
import org.datacrow.core.objects.DcImageIcon;

public class DcMenuItem extends JMenuItem {
	
    public DcMenuItem(String text) {
        super(text);
    }
    
    @Override
    public void setIcon(Icon icon) {
    	if (icon != null) {
    		if (icon instanceof DcImageIcon) {
    			super.setIcon(((DcImageIcon) icon).toIcon());
    		} else if (icon instanceof ImageIcon) {
    			super.setIcon(new DcImageIcon(((ImageIcon) icon).getImage()).toIcon());
    		} 
    	}
    }

    public DcMenuItem(AbstractAction action) {
        super(action);
    }
    
    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(GUI.getInstance().setRenderingHint(g));
    }     
}
