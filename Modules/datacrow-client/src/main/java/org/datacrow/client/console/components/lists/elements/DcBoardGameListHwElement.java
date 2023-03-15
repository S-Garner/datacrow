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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JTextArea;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcMediaObject;
import org.datacrow.core.objects.Picture;
import org.datacrow.core.objects.helpers.BoardGame;

public class DcBoardGameListHwElement extends DcObjectListHwElement {

	private JTextArea descriptionField;
    
    public DcBoardGameListHwElement(int module) {
        super(module);
    }
    
    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        if (descriptionField != null)
            descriptionField.setBackground(lighter(color));
    }     
    
    @Override
    public Collection<Picture> getPictures() {
        Collection<Picture> pictures = new ArrayList<Picture>();
        pictures.add((Picture) dco.getValue(BoardGame._Q_PICTURE1));
        return pictures;
    }

    @Override
    public void build() {
        setLayout(Layout.getGBL());
        
        JLabel titleLabel = getLabel(BoardGame._A_TITLE, true, label1Length);
        if (DcModules.getCurrent().isAbstract())
            titleLabel.setIcon(dco.getModule().getIcon16());
        
        addComponent(titleLabel, 0, 0);
        addComponent(getLabel(BoardGame._A_TITLE, false, field1Length), 1, 0);

        addComponent(getLabel(BoardGame._C_YEAR, true, label2Length), 2, 0);
        addComponent(getLabel(BoardGame._C_YEAR, false, field2Length), 3, 0);
        
        addComponent(getLabel(BoardGame._I_CATEGORIES, true, label1Length), 0, 1);
        addComponent(getLabel(BoardGame._I_CATEGORIES, false, field1Length), 1, 1);
        addComponent(getLabel(BoardGame._F_PUBLISHERS, true, label2Length), 2, 1);
        addComponent(getLabel(BoardGame._F_PUBLISHERS, false, field2Length), 3, 1);     

        addComponent(getLabel(BoardGame._J_NR_OF_PLAYERS, true, label1Length), 0, 2);
        addComponent(getLabel(BoardGame._J_NR_OF_PLAYERS, false, field1Length), 1, 2);
        addComponent(getLabel(DcMediaObject._E_RATING, true, label2Length), 2, 2);
        addComponent(getRatingValueLabel(), 3, 2);
        
        descriptionField = ComponentFactory.getTextArea();
        descriptionField.setPreferredSize(dimDescriptionFld);
        descriptionField.setText(getShortDescription((String) dco.getValue(BoardGame._B_DESCRIPTION)));
        add(descriptionField, Layout.getGBC( 0, 3, 4, 1, 10.0, 10.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 5, 5, 0), 0, 0));
        add(getPicturePanel(getPictures()), Layout.getGBC( 4, 0, 1, 4, 10.0, 10.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));        
    } 
    
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		descriptionField = null;
	}
}
