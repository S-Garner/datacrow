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

public class DcIconSizeComboBox extends DcComboBox<Object> {

	public DcIconSizeComboBox() {
        super();
        
        addItem(new IconSize(Long.valueOf(8), "8 * 8"));
        addItem(new IconSize(Long.valueOf(16), "16 * 16"));
        addItem(new IconSize(Long.valueOf(24), "24 * 24"));
        addItem(new IconSize(Long.valueOf(32), "32 * 32"));
    }
    
    @Override
    public void setValue(Object value) {
        if (value == null) return;
        
        if (value instanceof IconSize) {
            setSelectedItem(value);
        } else {
        	IconSize is;
            for (int i = 0; i < getItemCount(); i++) {
                is = (IconSize) getItemAt(i);
                if (is.getSize().equals(value)) {
                    setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    @Override
    public Object getValue() {
    	IconSize is = (IconSize) getSelectedItem();
        return is.getSize();
    }
    
    private class IconSize {
    
        private Long size;
        private String name;
        
        public IconSize(long size, String name) {
        	this.size = size;
        	this.name = name;
        }
        
        public Long getSize() {
            return size;
        }
        
        public String toString() {
            return name;
        }
    }
}

    
