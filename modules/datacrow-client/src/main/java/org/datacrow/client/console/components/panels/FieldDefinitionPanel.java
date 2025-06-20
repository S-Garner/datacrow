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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.table.TableColumn;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcShortTextField;
import org.datacrow.client.console.components.renderers.CheckBoxTableCellRenderer;
import org.datacrow.client.console.components.renderers.EditableTableCellRenderer;
import org.datacrow.client.console.components.tables.DcTable;
import org.datacrow.core.DcRepository;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcAssociate;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.utilities.definitions.DcFieldDefinition;
import org.datacrow.core.utilities.definitions.DcFieldDefinitions;

/**
 * @author RJ
 *
 */
public class FieldDefinitionPanel extends JPanel implements ActionListener {

    public static final int _COL_ORIG_LABEL = 0;
    public static final int _COL_CUSTOM_LABEL = 1;
    public static final int _COL_ENABLED = 2;
    public static final int _COL_REQUIRED = 3;
    public static final int _COL_DESCRIPTIVE = 4;
    public static final int _COL_UNIQUE = 5;
    public static final int _COL_FIELD = 6;
    public static final int _COL_TAB = 7;
	
    private final DcModule module;
    private final DcTable table;
    
    public FieldDefinitionPanel(DcModule module) {
        this.module = module;
        table = ComponentFactory.getDCTable(false, false);
        buildPanel();
    }

    public void clear() {
    	table.clear();
    }

    private void buildPanel() {
        setLayout(Layout.getGBL());

        table.setColumnCount(8);

        TableColumn c = table.getColumnModel().getColumn(_COL_TAB);
        table.removeColumn(c);

        c = table.getColumnModel().getColumn(_COL_FIELD);
        table.removeColumn(c);

        c = table.getColumnModel().getColumn(_COL_ORIG_LABEL);
        JTextField textField = ComponentFactory.getTextFieldDisabled();
        c.setCellEditor(new DefaultCellEditor(textField));
        c.setHeaderValue(DcResources.getText("lblOriginalLabel"));

        c = table.getColumnModel().getColumn(_COL_CUSTOM_LABEL);
        DcShortTextField textName = ComponentFactory.getShortTextField(20);
        c.setCellEditor(new DefaultCellEditor(textName));
        c.setCellRenderer(EditableTableCellRenderer.getInstance());
        c.setHeaderValue(DcResources.getText("lblCustomLabel"));

        c = table.getColumnModel().getColumn(_COL_ENABLED);
        JCheckBox checkEnabled = new JCheckBox();
        checkEnabled.addActionListener(this);
        checkEnabled.setActionCommand("checkDependencies");
        c.setCellEditor(new DefaultCellEditor(checkEnabled));
        c.setCellRenderer(CheckBoxTableCellRenderer.getInstance());
        c.setHeaderValue(DcResources.getText("lblEnabled"));

        c = table.getColumnModel().getColumn(_COL_REQUIRED);
        JCheckBox checkRequired = new JCheckBox();
        checkRequired.addActionListener(this);
        checkRequired.setActionCommand("checkDependencies");
        c.setCellEditor(new DefaultCellEditor(checkRequired));
        c.setCellRenderer(CheckBoxTableCellRenderer.getInstance());
        c.setHeaderValue(DcResources.getText("lblRequired"));

        c = table.getColumnModel().getColumn(_COL_DESCRIPTIVE);
        JCheckBox checkDescriptive = new JCheckBox();
        checkDescriptive.addActionListener(this);
        checkDescriptive.setActionCommand("checkDependencies");
        c.setCellEditor(new DefaultCellEditor(checkDescriptive));
        c.setCellRenderer(CheckBoxTableCellRenderer.getInstance());
        c.setHeaderValue(DcResources.getText("lblDescriptive"));
        
        c = table.getColumnModel().getColumn(_COL_UNIQUE);
        JCheckBox checkUnique = new JCheckBox();
        checkUnique.addActionListener(this);
        checkUnique.setActionCommand("checkDependencies");
        c.setCellEditor(new DefaultCellEditor(checkUnique));
        c.setCellRenderer(CheckBoxTableCellRenderer.getInstance());
        c.setHeaderValue(DcResources.getText("lblUnique"));
        
        applyDefinitions();

        JScrollPane scroller = new JScrollPane(table);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        add(scroller, Layout.getGBC(0, 0, 1, 1, 50.0, 50.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        
        NavigationPanel panelNav = new NavigationPanel(table);

        add(panelNav,  Layout.getGBC(1, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 5, 5, 5), 0, 0));

        table.applyHeaders();
    }

    private void checkDependencies() {
        table.cancelEdit();
        int row = table.getSelectedRow();
        if (row > -1) {
            DcFieldDefinition definition = getDefinition(row);
            DcField field = (DcField) table.getValueAt(row, _COL_FIELD, true);
            
            if (field.isUiOnly() || field.getIndex() == DcObject._ID) {
                table.setValueAt(Boolean.FALSE, row, _COL_REQUIRED);
                table.setValueAt(Boolean.FALSE, row, _COL_UNIQUE);
            }

            if (DcModules.get(field.getModule()).getType() == DcModule._TYPE_ASSOCIATE_MODULE) {
                if (field.getIndex() == DcAssociate._A_NAME)
                    table.setValueAt(Boolean.TRUE, row, _COL_DESCRIPTIVE);
                else 
                    table.setValueAt(Boolean.FALSE, row, _COL_DESCRIPTIVE);
            }
            
            if (!definition.isEnabled()) {
                table.setValueAt(Boolean.FALSE, row, _COL_DESCRIPTIVE);
                table.setValueAt(Boolean.FALSE, row, _COL_REQUIRED);
                table.setValueAt(Boolean.FALSE, row, _COL_UNIQUE);
            }
        }
    }

    public void save() {
        DcFieldDefinitions definitions = getDefinitions();
        module.setSetting(DcRepository.ModuleSettings.stFieldDefinitions, definitions);

        // other settings depend on the global definitions settings
        definitions.checkDependencies();

        GUI gui = GUI.getInstance();
        if (module.hasSearchView())
            gui.getSearchView(module.getIndex()).applySettings();

        if (module.hasInsertView())
            gui.getInsertView(module.getIndex()).applySettings();
    }

    public void applyDefinitions() {
        table.clear();

        for (DcFieldDefinition definition : module.getFieldDefinitions().getDefinitions()) {
            
            if (module.getField(definition.getIndex()).isSystemField())
                continue;
            
            Object[] row = new Object[8];
            row[_COL_ORIG_LABEL] = module.getField(definition.getIndex()).getSystemName();
            row[_COL_CUSTOM_LABEL] = definition.getLabel();
            row[_COL_ENABLED] = Boolean.valueOf(definition.isEnabled());
            row[_COL_REQUIRED] = Boolean.valueOf(definition.isRequired());
            row[_COL_DESCRIPTIVE] = Boolean.valueOf(definition.isDescriptive());
            row[_COL_UNIQUE] = Boolean.valueOf(definition.isUnique());
            row[_COL_FIELD] = module.getField(definition.getIndex());
            row[_COL_TAB] = definition.getTab(module.getIndex());
            
            table.addRow(row);
        }
    }

    private DcFieldDefinition getDefinition(int row) {
        String name = (String) table.getValueAt(row, _COL_CUSTOM_LABEL, true);
        boolean enabled = ((Boolean) table.getValueAt(row, _COL_ENABLED, true)).booleanValue();
        boolean required = ((Boolean) table.getValueAt(row, _COL_REQUIRED, true)).booleanValue();
        boolean descriptive = ((Boolean) table.getValueAt(row, _COL_DESCRIPTIVE, true)).booleanValue();
        boolean unique = ((Boolean) table.getValueAt(row,_COL_UNIQUE, true)).booleanValue();
        String tab = ((String) table.getValueAt(row, _COL_TAB, true));
        
        DcField field = (DcField) table.getValueAt(row, _COL_FIELD, true);

        if (field.isReadOnly() || field.isUiOnly())
            required = false;

        return new DcFieldDefinition(
                field.getModule(), field.getIndex(), name, enabled, required, descriptive, unique, tab);
    }

    public DcFieldDefinitions getDefinitions() {
        table.cancelEdit();

        DcFieldDefinitions definitions = new DcFieldDefinitions(module.getIndex());
        for (int i = 0; i < table.getRowCount(); i++) {
            definitions.add(getDefinition(i));
        }

        for (DcField field : module.getFields()) {
            if (field.isSystemField()) 
                definitions.add(new DcFieldDefinition(field.getModule(), field.getIndex(), field.getLabel(), false, false, false, false, null));
        }
        
        return definitions;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("checkDependencies"))
            checkDependencies();
    }
}