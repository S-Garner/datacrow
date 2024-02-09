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

import org.datacrow.client.console.components.DcPicturePane;
import org.datacrow.core.pictures.Picture;

public class DcPictureListElement extends DcListElement {
    
    private static final FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
    
    private final DcPicturePane fldPicture;

    private final Picture picture;
    
    public DcPictureListElement(Picture picture, int width) {
        this.picture = picture;
        
        setLayout(layout);
        
        Dimension dim = new Dimension(width, width / 2);
        Dimension dimPicLbl = new Dimension(width, width / 2);
        
        fldPicture = new DcPicturePane(true);
        fldPicture.setPreferredSize(dimPicLbl);
        fldPicture.setMinimumSize(dimPicLbl);
        fldPicture.setMaximumSize(dimPicLbl);
        
        setPreferredSize(dim);
        setMinimumSize(dim);
        
        add(fldPicture);
        
        build();
    }

    public Picture getPicture() {
        return picture;
    }
    
    @Override
    public void build() {
        fldPicture.setImageIcon(picture.getImageIcon());
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
