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
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.core.migration.itemimport.ItemImporter;
import org.datacrow.core.migration.itemimport.ItemImporters;
import org.datacrow.core.resources.DcResources;

public class ItemImporterSelectionPanel extends ItemImporterWizardPanel {

	private final Collection<ItemImporter> readers = new ArrayList<ItemImporter>();
	
	private final ItemImporterWizard wizard;
	private final ButtonGroup bg;
	
    public ItemImporterSelectionPanel(ItemImporterWizard wizard) {
    	this.wizard = wizard;
    	this.bg = new ButtonGroup();
    	
    	build();
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgSelectImportMethod");
	}

	@Override
    public Object apply() {
        String command = bg.getSelection().getActionCommand();
        for (ItemImporter reader : readers) {
            if (reader.getKey().equals(command))
                wizard.getDefinition().setType(reader.getType());;
        }
	    return wizard.getDefinition();
    }

    @Override
    public void cleanup() {
        readers.clear();
    }      
    
    private void build() {
        setLayout(Layout.getGBL());
        
        int y = 0;
        int x = 0;
        
        JRadioButton rb;
        // we'll get the importers of the main module - they're all the same any way..
        for (ItemImporter reader : ItemImporters.getInstance().getImporters(wizard.getModuleIdx())) {
        	readers.add(reader);
        	rb = ComponentFactory.getRadioButton(reader.getName(), reader.getIcon(), reader.getKey());
            bg.add(rb);
            add(rb, Layout.getGBC( x, y++, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets( 0, 5, 5, 5), 0, 0));
            
            if (y == 1)
                rb.setSelected(true);
        }
    }
}
