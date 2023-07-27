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

import java.awt.Color;

import javax.swing.JColorChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.settings.objects.DcColor;

public class DcColorSelector extends JColorChooser implements IComponent, ChangeListener {
    
    private final String settingsKey;
    
    private Color color;

    public DcColorSelector(String settingsKey) {
        super();
        getSelectionModel().addChangeListener(this);
        this.settingsKey = settingsKey;
    }

    @Override
    public Object getValue() {
        return new DcColor(color.getRed(), color.getGreen(), color.getBlue());
    }

    @Override
    public void setValue(Object o) {
    	DcColor c = (DcColor) o;
        color = new Color(c.getR(), c.getG(), c.getB());
        setColor(color);
    }

    @Override
    public void clear() {
        color = null;        
    }
    
    @Override
    public void setEditable(boolean b) {
        super.setEnabled(b);
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        Color temp = getColor();
        if (color != null && !color.equals(temp)) {
            color = temp;
            DcSettings.set(settingsKey, color);
        }
    }
    
    @Override
    public void refresh() {}
}
