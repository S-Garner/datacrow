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
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcCheckBox;
import org.datacrow.client.console.components.DcColorSelector;
import org.datacrow.client.console.components.DcFileField;
import org.datacrow.client.console.components.IComponent;
import org.datacrow.client.console.wizards.WizardException;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.settings.Setting;
import org.datacrow.core.utilities.filefilters.DcFileFilter;

public class ItemImporterDefinitionPanel extends ItemImporterWizardPanel {

	private final Map<String, IComponent> settings = new HashMap<String, IComponent>();
	
	private final DcFileField source =  ComponentFactory.getFileField(false, false);
	
	private final ItemImporterWizard wizard;
    
    public ItemImporterDefinitionPanel(ItemImporterWizard wizard) {
        this.wizard = wizard;
        build();
    }
    
    private JComponent getUIComponent(Setting setting) {
        if (setting.getComponentType() == ComponentFactory._COLORSELECTOR)
            return new DcColorSelector(setting.getKey());
        
        return ComponentFactory.getComponent(
                -1, -1, -1, 
                setting.getComponentType(), 
                setting.getLabelText(), 400);
    }

    @Override
	public void onActivation() {

		if (wizard.getDefinition() == null)
			return;

    	
    	removeAll();
		build();

		settings.clear();
		
		source.setFileFilter(new DcFileFilter(wizard.getDefinition().getSupportedFileTypes()));
		source.setFile(wizard.getDefinition().getFile());
		
		int y = 1;
		Setting setting;
		JComponent c;
		JLabel label;
        for (String key : wizard.getDefinition().getSettingKeys()) {
        	setting = DcSettings.getSetting(key) != null ? DcSettings.getSetting(key) : 
        		wizard.getModule().getSettings().getSetting(key);
        	
        	c = getUIComponent(setting);
        	settings.put(key, (IComponent) c);
        	label = ComponentFactory.getLabel(setting.getLabelText());
        	
        	if (!(c instanceof DcCheckBox)) {
	        	add(label, 
	        	         Layout.getGBC( 0, y, 1, 1, 1.0, 1.0
	                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
	                     new Insets( 0, 5, 5, 5), 0, 0));
	            add(c,   Layout.getGBC( 1, y, 1, 1, 1.0, 1.0
	                    ,GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL,
	                     new Insets( 0, 5, 5, 5), 0, 0));
        	} else {
                add(c,   Layout.getGBC( 0, y, 2, 1, 1.0, 1.0
                        ,GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL,
                         new Insets( 0, 5, 5, 5), 0, 0));	        	    
        	}
        	
            ((IComponent) c).setValue(setting.getValue());
             
        	y++;
        }
	}

	@Override
    public Object apply() throws WizardException {
        if (source.getFile() == null)
            throw new WizardException(DcResources.getText("msgNoFileSelected"));
        
        if (!source.getFile().exists() || !source.getFile().canRead())
            throw new WizardException(DcResources.getText("msgFileCannotBeUsed"));
        
        // store the settings
        // note: I have made sure this works for both module and application settings 
        Setting setting;
        for (String key : settings.keySet()) {
            setting = DcSettings.getSetting(key) != null ? DcSettings.getSetting(key) : wizard.getModule().getSettings().getSetting(key);
            setting.setValue(settings.get(key).getValue());
        }        
        
        wizard.getDefinition().setFile(source.getFile());
        return wizard.getDefinition();
    }

    @Override
    public void cleanup() {
    	settings.clear();
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgImportSettings");
    }

    private void build() {
        setLayout(Layout.getGBL());

        //**********************************************************
        //Create Import Panel
        //**********************************************************
        add(source, Layout.getGBC( 0, 0, 2, 1, 1.0, 1.0
           ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
            new Insets( 0, 5, 5, 5), 0, 0));
    }
}
