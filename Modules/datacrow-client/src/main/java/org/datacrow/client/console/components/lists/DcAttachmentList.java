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

import org.datacrow.client.console.components.lists.elements.DcAttachmentListElement;
import org.datacrow.client.console.components.lists.elements.DcListElement;
import org.datacrow.client.console.components.renderers.DcListRenderer;
import org.datacrow.client.console.views.ISortableComponent;
import org.datacrow.core.attachments.Attachment;

public class DcAttachmentList extends DcList implements ISortableComponent {
    
    public DcAttachmentList() {
        super(new DcListModel<Object>());
        setCellRenderer(new DcListRenderer<Object>(true));
        setLayoutOrientation(JList.VERTICAL_WRAP);
    }    

    public List<Attachment> getAttachments() {
        List<Attachment> attachments = new ArrayList<Attachment>();
        for (DcListElement element : getElements())
        	attachments.add(((DcAttachmentListElement) element).getAttachment());

        return attachments;
    }
    
    public List<Attachment> getSelectedAttachments() {
        int[] rows = getSelectedIndices();
        Object element;
        
        List<Attachment> attachments = new ArrayList<Attachment>();
        
        if (rows != null) {
            for (int row : rows) {
                element = getModel().getElementAt(row);
                attachments.add(((DcAttachmentListElement) element).getAttachment());
            }
        }
        
        return attachments;
    }
    
    public Attachment getSelectedAttachment() {
    	DcAttachmentListElement element = (DcAttachmentListElement) getSelectedValue();
        return element != null ? element.getAttachment() : null;
    }
    
    public void add(Attachment attachment) {
        getDcModel().addElement(new DcAttachmentListElement(attachment));
        ensureIndexIsVisible(getModel().getSize());
    }
    
    public void remove(Attachment attachment) {
        for (DcListElement element : getElements()) {
            if (((DcAttachmentListElement) element).getAttachment().equals(attachment))
                getDcModel().removeElement(element);                
        }
    }    
}
