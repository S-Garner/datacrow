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

import org.datacrow.client.console.components.lists.elements.DcListElement;
import org.datacrow.client.console.components.lists.elements.DcPictureListElement;
import org.datacrow.client.console.components.renderers.DcListRenderer;
import org.datacrow.client.console.views.ISortableComponent;
import org.datacrow.core.pictures.Picture;

public class DcPicturesList extends DcList implements ISortableComponent {
    
    public DcPicturesList() {
        super(new DcListModel<Object>());
        setCellRenderer(new DcListRenderer<Object>(true));
        setLayoutOrientation(JList.VERTICAL_WRAP);
    }    

    public List<Picture> getPictures() {
        List<Picture> pictures = new ArrayList<Picture>();
        for (DcListElement element : getElements())
        	pictures.add(((DcPictureListElement) element).getPicture());

        return pictures;
    }
    
    public List<Picture> getSelectedPictures() {
        int[] rows = getSelectedIndices();
        Object element;
        
        List<Picture> pictures = new ArrayList<Picture>();
        
        if (rows != null) {
            for (int row : rows) {
                element = getModel().getElementAt(row);
                pictures.add(((DcPictureListElement) element).getPicture());
            }
        }
        
        return pictures;
    }
    
    public Picture getSelectedPicture() {
    	DcPictureListElement element = (DcPictureListElement) getSelectedValue();
        return element != null ? element.getPicture() : null;
    }
    
    public void add(Picture picture) {
        getDcModel().addElement(new DcPictureListElement(picture));
        ensureIndexIsVisible(getModel().getSize());
    }
    
    public void remove(Picture picture) {
        for (DcListElement element : getElements()) {
            if (((DcPictureListElement) element).getPicture().equals(picture))
                getDcModel().removeElement(element);                
        }
    }    
}
