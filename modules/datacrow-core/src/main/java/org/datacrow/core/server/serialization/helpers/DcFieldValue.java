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

package org.datacrow.core.server.serialization.helpers;

public class DcFieldValue {
    
    private final int fieldIndex;
    private final int moduleIndex;
    private final Object value;
    private final boolean changed;
    
    public DcFieldValue(int moduleIndex, int fieldIndex, Object value, boolean changed) {
        this.fieldIndex = fieldIndex;
        this.moduleIndex = moduleIndex;
        this.value = value;
        this.changed = changed;
    }
    
    public int getFieldIndex() {
        return fieldIndex;
    }

    public int getModuleIndex() {
        return moduleIndex;
    }
    
    public Object getValue() {
        return value;
    }
    
    public boolean isChanged() {
        return changed;
    }
}
