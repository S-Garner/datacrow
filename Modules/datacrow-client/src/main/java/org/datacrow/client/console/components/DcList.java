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

package org.datacrow.client.console.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.ListCellRenderer;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.core.DcRepository;

public class DcList extends JList<Object> {

    public DcList(Collection<Object> data) {
        this();
        this.setListData(data.toArray());
    }

    public DcList() {
        super(new DefaultListModel<Object>());
        setCellRenderer(new CustomCellRenderer<Object>());   
    }
    
    public Collection<JPanel> getData() {
    	Collection<JPanel> data = new ArrayList<JPanel>();
    	for (int i = 0; i < getModel().getSize(); i++) {
    		data.add((JPanel) getModel().getElementAt(i));
    	}
    	return data;
    }
    
    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }
    
    @Override
    public void setFont(Font font) {
    	Font f = ComponentFactory.getSystemFont();
    	
        super.setFont(f);
        Component[] components;
        for (JPanel panel : getData()) {
            components = panel.getComponents();
            for (int j = 0; j < components.length; j++)
                components[j].setFont(f);
        }
    }     
    
    public static class CustomCellRenderer<V> implements ListCellRenderer<Object> {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            JComponent component = (JComponent) value;
            Color selectionColor = ComponentFactory.getColor(DcRepository.Settings.stSelectionColor);
            component.setBackground(isSelected ? selectionColor : Color.WHITE);
            return component;
        }
    }    
}
