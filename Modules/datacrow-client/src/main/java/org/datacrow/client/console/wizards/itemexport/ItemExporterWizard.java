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

package org.datacrow.client.console.wizards.itemexport;

import java.util.ArrayList;
import java.util.List;

import org.datacrow.client.console.wizards.IWizardPanel;
import org.datacrow.client.console.wizards.Wizard;
import org.datacrow.client.console.wizards.WizardException;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;

public class ItemExporterWizard extends Wizard {

    private ItemExporterDefinition definition;
    private List<String> items;
    
	public ItemExporterWizard(int module, List<String> items) {
		super(module);
		
		setTitle(getWizardName());
		setHelpIndex("dc.migration.wizard.exporter");
		setIconImage(IconLibrary._icoItemExport.getImage());
		
		this.items = items;
		setSize(DcSettings.getDimension(DcRepository.Settings.stItemExporterWizardFormSize));
		setCenteredLocation();
	}

    public ItemExporterDefinition getDefinition() {
        return definition;
    }
    
    public List<String> getItems() {
        return items;
    }

    @Override
    public void finish() throws WizardException {
        
        if (definition != null && definition.getExporter() != null)
            definition.getExporter().cancel();
        
        definition = null;
        items = null;
        close();
    }

    @Override
    protected boolean isRestartSupported() {
        return false;
    }    
    
    @Override
    protected String getWizardName() {
        return DcResources.getText("lblItemExportWizard");
    }
    
    @Override
    protected List<IWizardPanel> getWizardPanels() {
        definition = new ItemExporterDefinition();

        List<IWizardPanel> panels = new ArrayList<IWizardPanel>();
    	panels.add(new ItemExporterSelectionPanel(this));
    	panels.add(new ItemExporterSettingsPanel(this));
    	panels.add(new ItemExporterSelectFieldsPanel(this));
    	panels.add(new ItemExporterTaskPanel(this));
    	return panels;
    }

    @Override
    protected void initialize() {}

    @Override
    protected void saveSettings() {
        DcSettings.set(DcRepository.Settings.stItemExporterWizardFormSize, getSize());
    }
}
