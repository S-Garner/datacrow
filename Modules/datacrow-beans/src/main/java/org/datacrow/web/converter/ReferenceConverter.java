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

package org.datacrow.web.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

import org.datacrow.web.ReferencesCache;
import org.datacrow.web.model.Reference;

public class ReferenceConverter implements Converter<Object> {
    
    private List<Reference> additionalReferences = new ArrayList<Reference>();
    
    private int moduleIdx;
    
    public ReferenceConverter(int moduleIdx) {
        this.moduleIdx = moduleIdx;
    }
    
    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        Object o = null;
        
        Collection<Reference> all = ReferencesCache.getInstance().getReferences(moduleIdx);
        all.addAll(additionalReferences);
        
        for (Reference r : all) {
            if (r.getId().equals(value))
                o = r;
        }
        
        return o;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object object) {
        return String.valueOf(((Reference) object).getId());
    }
    
    public void addReference(Reference reference) {
        additionalReferences.add(reference);
    }
}