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

package org.datacrow.client.console.windows.filtering;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcComboBox;
import org.datacrow.client.console.components.DcLongTextField;
import org.datacrow.client.console.components.DcNumberField;
import org.datacrow.client.console.components.lists.DcFilterEntryList;
import org.datacrow.client.console.components.lists.elements.DcFilterEntryListElement;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.data.DataFilterEntry;
import org.datacrow.core.data.Operator;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcProperty;
import org.datacrow.core.objects.DcSimpleValue;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;

public class DefineFilterEntryPanel extends JPanel implements MouseListener, ActionListener {

	private final DcModule module;
	
    private JPanel panelInput;
    private JComponent c;
    private JButton buttonAdd;
    
    private JComboBox<Object> comboAndOr;
    private JComboBox<Object> comboFields;
    private JComboBox<Object> comboOperators;
    private JComboBox<Object> comboModules;
    
    private DcFilterEntryList list;
    
    public DefineFilterEntryPanel(DcModule module) {
        this.module = module;
        build();
        comboModules.setSelectedIndex(0);
    }
    
    public void applyEntry(DataFilterEntry entry) {
        DcModule module = DcModules.get(entry.getModule());
        DcField field = module.getField(entry.getField());
        Operator operator = entry.getOperator();
        
        comboModules.setSelectedItem(module);
        
        setFields(module);
        
        comboAndOr.setSelectedItem(entry.getAndOr());
        comboFields.setSelectedItem(field);
        comboOperators.setSelectedItem(operator);
        
        if (operator.needsValue()) {
        	Object value = entry.getValue();
        	if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION && 
        	    value instanceof Collection) {
        	    
        		Collection<?> c = (Collection<?>) value;
        		value = c.size() == 1 ? value = c.toArray()[0] : null;
        	} 
        	
            ComponentFactory.setValue(c, value);
        }

        comboOperators.setEnabled(true);
        comboFields.setEnabled(true);
        buttonAdd.setEnabled(true);
    }
    
    public void addEntry() {
        DcField field = (DcField) comboFields.getSelectedItem();
        Operator operator = (Operator) comboOperators.getSelectedItem();
        
        Object value = ComponentFactory.getValue(c);
        if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
        	Collection<Object> c = new ArrayList<Object>();
        	c.add(value);
        	value = c;
        }
        
        if (operator.needsValue() && (value == null || value.equals(""))) {
            GUI.getInstance().displayMessage("msgEnterFilterValue");
            return;
        }
        
        DataFilterEntry entry = 
            new DataFilterEntry((String) comboAndOr.getSelectedItem(),
                                field.getModule(), field.getIndex(), operator, 
                                operator.needsValue() ? value : null);
        
        list.add(entry);
        list.clearSelection();

        setAndOr();
    }
    
    public void clear() {
        list.clear();
        setFields(module);
        setAndOr();
    }
    
    private void setAndOr() {
        if (list.getModel().getSize() == 0) {
            comboAndOr.removeItem(DcResources.getText("lblOr"));
            comboAndOr.setEnabled(false);
        } else {
            comboAndOr.removeAllItems();
            comboAndOr.addItem(DcResources.getText("lblAnd"));
            comboAndOr.addItem(DcResources.getText("lblOr"));
            comboAndOr.setEnabled(true);
            comboAndOr.setSelectedIndex(0);
        }
    }
    
    private void setFields(DcModule module) {
        comboFields.removeActionListener(this);
        comboFields.removeAllItems();
        
        for (DcField field : module.getFields()) {
            if (field.isSearchable() && field.isEnabled())
                comboFields.addItem(field);
        }
        
        comboFields.addActionListener(this);
        
        if (comboFields.getItemCount() > 0)
            comboFields.setSelectedIndex(0);
    }   
    
    
    public void applyOperator(Operator operator) {
        
        DcField field = (DcField) comboFields.getSelectedItem();
        
        if (field != null && 
                (field.getValueType() == DcRepository.ValueTypes._DATE ||
                 field.getValueType() == DcRepository.ValueTypes._DATETIME)) {
        
            boolean changed = false;
            if (    (operator == Operator.DAYS_AFTER ||
                     operator == Operator.DAYS_BEFORE || 
                     operator == Operator.MONTHS_AGO ||
                     operator == Operator.YEARS_AGO)) {
             
            if (c != null) panelInput.remove(c);
                c = ComponentFactory.getNumberField();
                changed = true;
            } else if (c instanceof DcNumberField) {
                if (c != null) panelInput.remove(c);
                c = ComponentFactory.getDateField();
                changed = true;
            }
            
            if (changed) {
                panelInput.add(c, Layout.getGBC( 4, 1, 1, 1, 50.0, 50.0
                        ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                         new Insets( 0,0,0,0), 0, 0));
                revalidate();
            }
        }
    }
    
    
    /**
     * @param field
     */
    public void applyField(DcField field) {
        if (field == null) return;
        if (c != null) panelInput.remove(c);
        
        Dimension size = c != null ? c.getSize() : null; 
        
        if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
            
            DcComboBox<Object> combo = ComponentFactory.getComboBox();
            c = combo;
            combo.addItem(" ");

            Connector connector = DcConfig.getInstance().getConnector();
            if (connector.getCount(field.getReferenceIdx(), -1, null) > 1000) {
                for (DcSimpleValue value : connector.getSimpleValues(field.getReferenceIdx(), false))
                    combo.addItem(value);
            } else {
                int[] fields;
                if (DcModules.get(field.getReferenceIdx()).getType() == DcModule._TYPE_PROPERTY_MODULE) {
                    fields = new int[] {DcObject._ID, DcProperty._A_NAME, DcProperty._B_ICON};
                } else {
                    fields = new int[] {DcObject._ID, DcModules.get(field.getReferenceIdx()).getDisplayFieldIdx()};
                }
                
                List<DcObject> objects = connector.getItems(field.getReferenceIdx(), fields);
                for (DcObject dco : objects)
                	combo.addItem(dco);
            }
            
            combo.setUneditable();
        } else if (field.getFieldType() == ComponentFactory._FILEFIELD || 
        		   field.getFieldType() == ComponentFactory._FILELAUNCHFIELD) {
        	c = ComponentFactory.getShortTextField(255);
        } else {
        	c = ComponentFactory.getComponent(field.getModule(), field.getReferenceIdx(), field.getIndex(), 
        	        field.getFieldType(), field.getLabel(), field.getMaximumLength());	
        }
        
        c = c instanceof DcLongTextField ? ComponentFactory.getShortTextField(field.getMaximumLength()) : c;
        
        int height = comboAndOr != null && comboAndOr.getHeight() > 0 ? comboAndOr.getHeight() : ComponentFactory.getPreferredFieldHeight();
        c.setMinimumSize(new Dimension(100, height));
        c.setPreferredSize(new Dimension(100, height));
        if (size != null && size.getWidth() > 0 && size.getHeight() > 0) {
            size.setSize(size.getWidth(), height);
            c.setPreferredSize(size);
        }
        
        panelInput.add(c, Layout.getGBC( 4, 1, 1, 1, 50.0, 50.0
                      ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                       new Insets( 0,0,0,0), 0, 0));

        revalidate();
    }
    
    public List<DataFilterEntry> getEntries() {
        return list.getEntries();
    }
    
    private void setOperators(DcField field) {
        comboOperators.removeActionListener(this);
        comboOperators.removeAllItems();
        
        if (field == null)
            return;
        
        for (Operator operator : Operator.get(field, false))
            comboOperators.addItem(operator);
        
        comboOperators.addActionListener(this);
        comboOperators.setSelectedIndex(0);
    }    
        
    
    private void build() {
        setLayout(Layout.getGBL());
        
        comboFields = ComponentFactory.getComboBox(new DefaultComboBoxModel<Object>());
        comboOperators = ComponentFactory.getComboBox(new DefaultComboBoxModel<Object>());
        comboModules = ComponentFactory.getComboBox(new DefaultComboBoxModel<Object>());
        comboAndOr = ComponentFactory.getComboBox(new DefaultComboBoxModel<Object>());
        
        comboAndOr.addItem(DcResources.getText("lblAnd"));
        comboAndOr.setEnabled(false);
        
        comboModules.addItem(module);
        if (module.getChild() != null)
            comboModules.addItem(module.getChild());

        comboFields.addActionListener(this);
        comboFields.setActionCommand("fieldSelected");
        
        comboModules.addActionListener(this);
        comboModules.setActionCommand("moduleSelected");
        
        comboOperators.addActionListener(this);
        comboOperators.setActionCommand("operatorSelected");
        
        list = new DcFilterEntryList();
        list.addMouseListener(this);

        JScrollPane scroller = new JScrollPane(list);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        panelInput = new JPanel();
        panelInput.setLayout(Layout.getGBL());
        
        buttonAdd = ComponentFactory.getButton(DcResources.getText("lblAdd"));
        buttonAdd.addActionListener(this);
        buttonAdd.setActionCommand("addEntry");
        
        panelInput.add(ComponentFactory.getLabel(DcResources.getText("lblModule")),  
                 Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 5), 0, 0));
        panelInput.add(ComponentFactory.getLabel(DcResources.getText("lblField")),  
                 Layout.getGBC( 2, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 5), 0, 0));
        panelInput.add(ComponentFactory.getLabel(DcResources.getText("lblOperator")),
                 Layout.getGBC( 3, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 5), 0, 0));
        panelInput.add(ComponentFactory.getLabel(DcResources.getText("lblValue")),
                 Layout.getGBC( 4, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 5), 0, 0));

        panelInput.add(comboAndOr,      Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 5), 0, 0));
        panelInput.add(comboModules,    Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 5), 0, 0));
        panelInput.add(comboFields,     Layout.getGBC( 2, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 5), 0, 0));
        panelInput.add(comboOperators,  Layout.getGBC( 3, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 5), 0, 0));
        
        JPanel panelAction = new JPanel();
        panelAction.add(buttonAdd);

        add(scroller,     Layout.getGBC( 0, 0, 2, 1, 40.0, 40.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        add(panelInput,   Layout.getGBC( 0, 1, 1, 1, 40.0, 1.0
                ,GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 5, 5, 5, 0), 0, 0));
        add(panelAction,  Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        

    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("addEntry")) {
            addEntry();
        } else if (ae.getActionCommand().equals("moduleSelected")) {
            setFields((DcModule) comboModules.getSelectedItem());
        } else if (ae.getActionCommand().equals("fieldSelected")) {
            DcField field = (DcField) comboFields.getSelectedItem();
            applyField(field);
            setOperators(field);
        } else if (ae.getActionCommand().equals("operatorSelected")) {
            Operator operator = (Operator) comboOperators.getSelectedItem();
            if (c != null && operator != null) {
                applyOperator(operator);
                c.setEnabled(operator.needsValue());
                Color color = operator.needsValue() ? Color.WHITE : ComponentFactory.getDisabledColor();
                c.setBackground(color);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getClickCount() == 2 && list.getSelectedIndex() > -1) {
            DcFilterEntryListElement elem = (DcFilterEntryListElement) list.getSelectedValue();
            DataFilterEntry entry = elem.getEntry();
            list.remove();
            setAndOr();
            applyEntry(entry);
            e.consume();
        }
    }
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
}
