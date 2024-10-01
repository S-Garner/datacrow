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
import javax.swing.JPanel;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.core.DcRepository;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcProperty;
import org.datacrow.core.settings.DcSettings;

public class DcPropertyListElement extends DcObjectListElement {

	private static final FlowLayout layout = new FlowLayout(FlowLayout.LEFT);

    public DcPropertyListElement(int module) {
        super(module);
    }
    
    @Override
    public void setDcObject(DcObject dco) {
        super.setDcObject(dco);
        build();
    }
    
    @Override
    public int[] getFields() {
    	return DcModules.get(module).getFieldIndices();
    }

    @Override
    public void build() {

    	if (dco == null)
        	return;
    	
    	setLayout(layout);
        JPanel panelInfo = getPanel();
        
        JLabel label = ComponentFactory.getLabel(dco.toString());

        if (dco.getValue(DcProperty._B_ICON) != null)
            label.setIcon(dco.getIcon());
        
        panelInfo.add(label);
        
        int height = DcSettings.getInt(DcRepository.Settings.stIconSize);
        height = height < fieldHeight ? fieldHeight : height;
        
        label.setPreferredSize(new Dimension(800, height));
        panelInfo.setPreferredSize(new Dimension(800, height));
        add(panelInfo);
    } 
}