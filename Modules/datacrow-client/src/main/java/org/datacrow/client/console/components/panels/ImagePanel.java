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

package org.datacrow.client.console.components.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JPanel;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcPictureField;
import org.datacrow.core.pictures.Picture;

/**
 * @author RJ
 *
 */
public class ImagePanel extends JPanel {
	
	private final DcPictureField picField;
	private final Picture picture;
	
	public ImagePanel(Picture picture) {
		this.picture = picture;
//		
		picField = ComponentFactory.getPictureField(true, false);
		build();
	}
	
	public void load() {
//		picture.loadImage(false);
//		picField.setValue(picture);
	}
	
	public void clear() {
		picField.clear();
	}
	
	private void build() {
        setLayout(Layout.getGBL());
        
        add(picField, Layout.getGBC(0, 0, 1, 1, 10.0, 10.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 0, 0));
	}
}