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

import javax.swing.JPasswordField;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;

public class DcPasswordField extends JPasswordField implements IComponent {
	
    public DcPasswordField() {
        super();
        ComponentFactory.setBorder(this);
    }
    
	@Override
    public void setValue(Object value) {
        String s = value == null ? "" : value.toString();
		super.setText(s);
	}

    @Override
    public void reset() {
    	setText("");
    }
	
	@Override
    public Object getValue() {
        String password = "";
        try {
            password = String.valueOf(getPassword());
        } catch (Exception exp) {}
        return password;
	}

    @Override
    public void clear() {} 
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(GUI.getInstance().setRenderingHint(g));
    }    
    
    @Override
    public void refresh() {}
}
