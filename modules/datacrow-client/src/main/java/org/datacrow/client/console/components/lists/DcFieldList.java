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

package org.datacrow.client.console.components.lists;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;

import org.datacrow.client.console.components.lists.elements.DcFieldListElement;
import org.datacrow.client.console.components.lists.elements.DcListElement;
import org.datacrow.client.console.components.renderers.DcListRenderer;
import org.datacrow.client.console.views.ISortableComponent;
import org.datacrow.core.objects.DcField;

public class DcFieldList<V extends Object> extends DcList implements ISortableComponent {
    
    public DcFieldList() {
        super(new DcListModel<Object>());
        setCellRenderer(new DcListRenderer<Object>(true));
        setLayoutOrientation(JList.VERTICAL_WRAP);
    }    

    public List<DcField> getFields() {
        List<DcField> fields = new ArrayList<DcField>();
        for (DcListElement element : getElements())
            fields.add(((DcFieldListElement) element).getField());

        return fields;
    }
    
    public DcField getSelected() {
        DcFieldListElement element = (DcFieldListElement) getSelectedValue();
        return element != null ? element.getField() : null;
    }
    
    public void add(DcField field) {
        getDcModel().addElement(new DcFieldListElement(field));
        ensureIndexIsVisible(getModel().getSize());
    }
    
    public void remove(DcField field) {
        for (DcListElement element : getElements()) {
            if (((DcFieldListElement) element).getField().equals(field))
                getDcModel().removeElement(element);                
        }
    }    
}
