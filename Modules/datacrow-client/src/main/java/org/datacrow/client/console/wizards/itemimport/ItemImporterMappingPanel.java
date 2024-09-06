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

package org.datacrow.client.console.wizards.itemimport;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.tables.DcTable;
import org.datacrow.client.console.components.tables.DcTableModel;
import org.datacrow.client.console.wizards.WizardException;
import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.migration.itemimport.ItemImporterFieldMappings;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;

public class ItemImporterMappingPanel extends ItemImporterWizardPanel {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ItemImporterMappingPanel.class.getName());
	
	private final DcTable table = ComponentFactory.getDCTable(false, false);
	
    private final ItemImporterWizard wizard;
    
    JComboBox<Object> comboFields = ComponentFactory.getComboBox();
    
    public ItemImporterMappingPanel(ItemImporterWizard wizard) {
        this.wizard = wizard;
        
        build();
    }
    
	@Override
    public Object apply() throws WizardException {
		ItemImporterFieldMappings mappings = wizard.getDefinition().getMappings();
        
        String source;
        DcField target;
        for (int i = 0; i < table.getRowCount(); i++) {
        	source = (String) table.getValueAt(i, 1, true);
        	target = (DcField) table.getValueAt(i, 2, true);
        	if (target != null)
        		mappings.setMapping(source, target);
        }
        
        return wizard.getDefinition();
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgImportFieldMapping");
    }
    
    @Override
    public void onActivation() {
    	if (wizard.getDefinition() != null && 
    	    wizard.getDefinition().getFile() != null) {
    	    
//    		wizard.getDefinition().getMappings().clear();

            table.clear();

            comboFields.removeAllItems();
            for (DcField field : wizard.getModule().getFields()) {
                if (    (!field.isUiOnly() || 
                         field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) && 
                         field.getIndex() != DcObject._ID)
                    
                    comboFields.addItem(field);
            }
            
            try {
            	ItemImporterFieldMappings mappings = wizard.getDefinition().getMappings();
                int veld = 1;
                for (String source : mappings.getSourceFields())
                    table.addRow(new Object[] {Integer.valueOf(veld++), source, mappings.getTarget(source)});

            } catch (Exception exp) {
                GUI.getInstance().displayErrorMessage(DcResources.getText("msgFileCannotBeUsed") + ": " + exp.getMessage());
                logger.error("Error while reading from file", exp);
            }
    	}
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        
        //**********************************************************
        //Create Import Panel
        //**********************************************************
        DcTableModel model = (DcTableModel) table.getModel();
        model.setColumnCount(3);

        TableColumn columnNr = table.getColumnModel().getColumn(0);
        columnNr.setCellEditor(new DefaultCellEditor(ComponentFactory.getTextFieldDisabled()));
        columnNr.setHeaderValue(DcResources.getText("lblField"));
        
        TableColumn columnName = table.getColumnModel().getColumn(1);
        columnName.setCellEditor(new DefaultCellEditor(ComponentFactory.getTextFieldDisabled()));
        columnName.setHeaderValue(DcResources.getText("lblSourceName"));

        TableColumn columnField = table.getColumnModel().getColumn(2);
        columnField.setHeaderValue(DcResources.getText("lblTargetName"));
        columnField.setCellEditor(new DefaultCellEditor(comboFields));

        table.applyHeaders();

        JScrollPane scroller = new JScrollPane(table);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel panelImportDef = new JPanel();
        panelImportDef.setLayout(Layout.getGBL());

        add(scroller,  Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 5, 5, 5), 0, 0));
    }

	@Override
	public void cleanup() {
		table.clear();
	}
}
