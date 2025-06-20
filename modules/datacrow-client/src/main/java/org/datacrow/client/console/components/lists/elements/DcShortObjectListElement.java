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
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcProperty;

public class DcShortObjectListElement extends DcObjectListElement {

	private static final FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
    
    public DcShortObjectListElement(int module) {
        super(module);
        setPreferredSize(new Dimension(50000, fieldHeight));
    }
    
    @Override
    public int[] getFields() {
    	// only return one field for performance reasons, especially for abstract module children
    	DcModule module = DcModules.get(getModule());
    	return module.getType() == DcModule._TYPE_PROPERTY_MODULE ? 
    			new int[] {DcProperty._ID, module.getDisplayFieldIdx(), DcProperty._B_ICON} : new int[] {DcObject._ID, module.getDisplayFieldIdx()};
    }

    @Override
    public void build() {
        setLayout(layout);

        JPanel panelInfo = getPanel();
        
        JLabel label = ComponentFactory.getLabel(dco.toString());
        panelInfo.add(label);
        panelInfo.setPreferredSize(new Dimension(50000, fieldHeight));
        add(panelInfo);
    } 
}
