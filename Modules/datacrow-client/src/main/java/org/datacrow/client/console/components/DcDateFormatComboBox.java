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

import org.datacrow.core.objects.DateFormatValue;

public class DcDateFormatComboBox extends DcComboBox<DateFormatValue> {

    public DcDateFormatComboBox() {
        super();
        
        addItem(new DateFormatValue("EEEEE, d MMMMM yyyy"));
        addItem(new DateFormatValue("EEE, d MMMMM yyyy"));
        addItem(new DateFormatValue("d MMMMM yyyy"));
        addItem(new DateFormatValue("MMMMM,d yyyy"));

        addItem(new DateFormatValue("dd-MM-yyyy"));
        addItem(new DateFormatValue("MM-dd-yyyy"));
        addItem(new DateFormatValue("dd-MM-yyyy"));
        addItem(new DateFormatValue("yyyy-dd-MM"));
        addItem(new DateFormatValue("yyyy-MM-dd"));

        addItem(new DateFormatValue("dd.MM.yyyy"));
        addItem(new DateFormatValue("MM.dd.yyyy"));
        addItem(new DateFormatValue("dd.MM.yyyy"));
        addItem(new DateFormatValue("yyyy.dd.MM"));
        addItem(new DateFormatValue("yyyy.MM.dd"));
        
        addItem(new DateFormatValue("dd/MM/yyyy"));
        addItem(new DateFormatValue("MM/dd/yyyy"));
        addItem(new DateFormatValue("dd/MM/yyyy"));
        addItem(new DateFormatValue("yyyy/dd/MM"));
        addItem(new DateFormatValue("yyyy/MM/dd"));        
        
        addItem(new DateFormatValue("MM.dd.yyyy"));
        addItem(new DateFormatValue("EEE, d MMM yyyy"));
        addItem(new DateFormatValue("EEE, MMM d, ''yy"));
        addItem(new DateFormatValue("yyyy.MMMMM.dd"));
        addItem(new DateFormatValue("YYYY-'W'ww-u"));
    }
    
    @Override
    public void setValue(Object value) {
        if (value == null) return;
        
        if (value instanceof DateFormatValue) {
            setSelectedItem(value);
        } else {
            DateFormatValue dfv;
            for (int i = 0; i < getItemCount(); i++) {
                dfv = (DateFormatValue) getItemAt(i);
                if (dfv.getFormat().equals(value)) {
                    setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    @Override
    public Object getValue() {
        DateFormatValue dfv = (DateFormatValue) getSelectedItem();
        return dfv.getFormat();
    }
}

    
