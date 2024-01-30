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

import javax.swing.JLabel;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.core.pictures.Picture;

public class DcPictureListElement extends DcListElement {
    
    private static final FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
    private static final Dimension dim = new Dimension(1200, 30);
    private static final Dimension dimLabel = new Dimension(1200, 30);
    
    private final Picture picture;
    
    public DcPictureListElement(Picture picture) {
        this.picture = picture;
        
        setPreferredSize(dim);
        setMinimumSize(dim);
        
        build();
    }

    public Picture getPicture() {
        return picture;
    }
    
    @Override
    public void build() {
        setLayout(layout);
        String s = picture.getFilename();
        
        JLabel labelField = ComponentFactory.getLabel(s);
        labelField.setPreferredSize(dimLabel);
        add(labelField);
    }

	@Override
	public void clear() {
		super.clear();
		
		if (picture != null)
			picture.clear();
	}

    @Override
    public int hashCode() {
        return picture.getFilename().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DcPictureListElement) {
            Picture picture2 = ((DcPictureListElement) obj).getPicture();
            return picture2 != null && picture2.equals(picture);
        }
        return false;
    }
}
