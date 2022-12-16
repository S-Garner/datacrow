/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.net                             *
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

package org.datacrow.web.bean;

import java.util.ArrayList;
import java.util.List;

import org.datacrow.web.DcBean;
import org.datacrow.web.model.Field;
import org.datacrow.web.model.Reference;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@Named
@SessionScoped
public class FieldBean extends DcBean {

    private Field selectedField;
    
    public FieldBean() {}
    
    public void setSelectedField(Field field) {
        this.selectedField = field;
    }
    
    public Field getSelectedField() {
        return selectedField;
    }

    public List<Reference> getReferences() {
        
        if (selectedField == null)
            return new ArrayList<Reference>();
        
        return selectedField.getAllReferences();
    }

    public Object getValueForString(String s) {
        for (Reference ref : selectedField.getAllReferences()) {
            if (ref.getId().equals(s))
                return ref;
        }
        return null;
    }
}
