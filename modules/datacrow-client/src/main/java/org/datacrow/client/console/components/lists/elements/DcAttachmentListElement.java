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

package org.datacrow.client.console.components.lists.elements;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.core.DcRepository;
import org.datacrow.core.attachments.Attachment;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.CoreUtilities;

public class DcAttachmentListElement extends DcListElement {
    
    private static final FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
    private static final Dimension dim = new Dimension(1200, 30);
    private static final Dimension dimLabel = new Dimension(1200, 30);
    
    private final Attachment attachment;
    
    public DcAttachmentListElement(Attachment attachment) {
        this.attachment = attachment;
        
        setPreferredSize(dim);
        setMinimumSize(dim);
        
        build();
    }

    public Attachment getAttachment() {
        return attachment;
    }
    
    @Override
    public void build() {
        setLayout(layout);
        
        String s = attachment.getName();
        s += " (" + CoreUtilities.toFileSizeString(Long.valueOf(attachment.getSize() / 1000));
        s += ", " + new SimpleDateFormat(
                DcSettings.getString(DcRepository.Settings.stDateFormat)).format(attachment.getCreated());
        s += ")";
        
        JLabel labelField = ComponentFactory.getLabel(s);
        labelField.setPreferredSize(dimLabel);
        add(labelField);
    }

	@Override
	public void clear() {
		super.clear();
		
		if (attachment != null)
			attachment.clear();
	}

    @Override
    public int hashCode() {
        return attachment.getStorageFile().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DcAttachmentListElement) {
            Attachment attachment2 = ((DcAttachmentListElement) obj).getAttachment();
            return attachment2 != null && attachment2.equals(attachment);
        }
        
        return false;
    }
}
