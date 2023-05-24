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

public class DcUIScaleComboBox extends DcComboBox {

	public DcUIScaleComboBox() {
        super();
        
        for (long scale = 25; scale < 305; scale+=5) {
        	addItem(new UIScale(Long.valueOf(scale), String.valueOf(scale) + "%"));	
        }
    }
    
    @Override
    public void setValue(Object value) {
        if (value == null) return;
        
        if (value instanceof UIScale) {
            setSelectedItem(value);
        } else {
        	UIScale us;
            for (int i = 0; i < getItemCount(); i++) {
                us = (UIScale) getItemAt(i);
                if (us.getScale().equals(value)) {
                    setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    @Override
    public Object getValue() {
    	UIScale us = (UIScale) getSelectedItem();
        return us.getScale();
    }
    
    private class UIScale {
    
        private Long scale;
        private String name;
        
        public UIScale(long scale, String name) {
        	this.scale = scale;
        	this.name = name;
        }
        
        public Long getScale() {
            return scale;
        }
        
        public String toString() {
            return name;
        }
    }
}