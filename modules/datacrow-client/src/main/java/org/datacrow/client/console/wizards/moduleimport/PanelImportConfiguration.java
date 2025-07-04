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

package org.datacrow.client.console.wizards.moduleimport;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.windows.settings.SettingsPanel;
import org.datacrow.client.console.wizards.WizardException;
import org.datacrow.core.DcRepository;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.Setting;
import org.datacrow.core.settings.SettingsGroup;
import org.datacrow.core.utilities.CoreUtilities;

public class PanelImportConfiguration extends ModuleImportWizardPanel {

	private static final String _IMPORT_FILE = "import_file";
	
	private final SettingsGroup group = new SettingsGroup("", "");
	
	private SettingsPanel settingsPanel;
	
    public PanelImportConfiguration() {
        build();
    }
    
    @Override
    public Object apply() throws WizardException {
    	ImportDefinition definition = getDefinition();
    	
    	settingsPanel.saveSettings();
    	
    	String filename = group.getSettings().get(_IMPORT_FILE).getValueAsString();
    	
    	if (CoreUtilities.isEmpty(filename)) {
    		throw new WizardException(DcResources.getText("msgNoFileSelected"));
    	} else {
	    	definition.setFile(filename);
    	}
    	
        return definition;
    }

    @Override
    public void onActivation() {
        ImportDefinition definition = getDefinition();
		
		if (definition != null && definition.getFile() != null)
			group.getSettings().get(_IMPORT_FILE).setValue(definition.getFile().toString());
	}

	@Override
    public String getHelpText() {
        return DcResources.getText("msgImportModuleConfigurationHelp");
    }
    
    @Override
    public void cleanup() {
    	settingsPanel = null;
    }    
    
    private void build() {
        setLayout(Layout.getGBL());
        
        group.add(new Setting(DcRepository.ValueTypes._STRING,
                PanelImportConfiguration._IMPORT_FILE, null, ComponentFactory._FILEFIELD,
                "", DcResources.getText("lblModuleImportFile"), true, true, -1));         
        
        settingsPanel = new SettingsPanel(group, true);
        settingsPanel.setVisible(true);
        settingsPanel.initializeSettings();
        
        add(settingsPanel, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets( 5, 5, 5, 5), 0, 0));
    }
}
