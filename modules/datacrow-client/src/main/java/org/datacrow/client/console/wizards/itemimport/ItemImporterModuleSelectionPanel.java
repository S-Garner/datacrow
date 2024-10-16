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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.tables.DcTable;
import org.datacrow.client.console.wizards.WizardException;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.resources.DcResources;

public class ItemImporterModuleSelectionPanel extends ItemImporterWizardPanel implements ActionListener {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ItemImporterModuleSelectionPanel.class.getName());
	
	private final DcTable table = ComponentFactory.getDCTable(false, false);
    private final ItemImporterWizard wizard;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    
    public ItemImporterModuleSelectionPanel(ItemImporterWizard wizard) {
        this.wizard = wizard;
        build();
    }
    
	@Override
    public Object apply() throws WizardException {
        String command = buttonGroup.getSelection().getActionCommand();
        int moduleIdx = Integer.parseInt(command);

        ItemImporterDefinition definition = wizard.getDefinition();
        definition.setModule(moduleIdx);
		
        return definition;
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgSelectModuleImport");
    }
    
    @Override
    public void onActivation() {}
    
    private class SelectModuleAction implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent ev) {
        	
        	if (ev.getStateChange() == ItemEvent.SELECTED) {
        	
	        	try {
	        		apply();
	        		wizard.next();
	        	} catch (Exception e) {
	        		logger.error(e, e);
	        	}
        	}
        }
    }   
    
    private void build() {
        setLayout(Layout.getGBL());
        
        JPanel panelModules = new JPanel();
        panelModules.setLayout(Layout.getGBL());
        
		Collection<DcModule> modules = new ArrayList<DcModule>();
		modules.add(DcModules.getCurrent());
		modules.addAll(DcModules.getReferencedModules(DcModules.getCurrent().getIndex()));        
        
        ImageIcon icon;
        JRadioButton radioButton;
        int x = 0;
        int y = 0;
        for (DcModule module : modules) {
            icon = module.getIcon32() == null ? module.getIcon16() : module.getIcon32();
            icon = icon == null ? IconLibrary._icoModuleTypeProperty32 : icon;
            radioButton = ComponentFactory.getRadioButton(module.getLabel(), icon, "" + module.getIndex());
            radioButton.addItemListener(new SelectModuleAction());
            
            buttonGroup.add(radioButton);
            panelModules.add(radioButton, Layout.getGBC( x, y++, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets( 0, 5, 5, 5), 0, 0));
            
            if (y == 6) {
            	x++;
            	y = 0;
            }
        }

        //**********************************************************
        //Main panel
        //**********************************************************
        this.add(panelModules, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
    }

	@Override
	public void cleanup() {
		table.clear();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
