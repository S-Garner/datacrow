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

import java.util.LinkedList;
import java.util.List;

import org.datacrow.client.console.GUI;
import org.datacrow.client.console.wizards.IWizardPanel;
import org.datacrow.client.console.wizards.Wizard;
import org.datacrow.client.console.wizards.WizardException;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.console.IMasterView;
import org.datacrow.core.migration.itemimport.ItemImporters.ImporterType;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;

public class ItemImporterWizard extends Wizard {

	public static final int _STEP_MODULE_SELECT = 1;
    public static final int _STEP_MAPPING = 3;
    
	private final ItemImporterDefinition definition;
	
	public ItemImporterWizard() {
		super();
		
		setTitle(getWizardName());
		setHelpIndex("dc.migration.wizard.importer");
		setIconImage(IconLibrary._icoItemImport.getImage());
		
		this.definition = new ItemImporterDefinition();
		setSize(DcSettings.getDimension(DcRepository.Settings.stItemImporterWizardFormSize));
		setCenteredLocation();
	}
	
	protected ItemImporterDefinition getDefinition() {
		return definition;
	}
	
	@Override
	public DcModule getModule() {
		if (definition == null) {
			return super.getModule();
		} else {
			return DcModules.get(definition.getModule());
		}
	}
	
	@Override
	public int getModuleIdx() {
		if (definition == null) {
			return super.getModuleIdx();
		} else {
			return definition.getModule();
		}
	}  	

	@Override
    protected boolean isRestartSupported() {
	    return false;
    }
	
    @Override
    public void finish() throws WizardException {
    	// TODO: need to cancel the importer - do this from the final step though!
        if (!isCancelled()) {
            IMasterView view = GUI.getInstance().getSearchView(DcModules.getCurrent().getIndex());
            if (view != null) view.refresh();
        }
        close();
    }

    @Override
    protected String getWizardName() {
        return DcResources.getText("lblItemImportWizard");
    }
    
    @Override
    public void next() throws WizardException {
    	
    	getCurrent().apply();
    	
        if (getDefinition().getType().equals(ImporterType.XML)) {
            if (!skip.contains(Integer.valueOf(_STEP_MAPPING)))
                skip.add(Integer.valueOf(_STEP_MAPPING));
            if (!skip.contains(Integer.valueOf(_STEP_MODULE_SELECT)))
                skip.add(Integer.valueOf(_STEP_MODULE_SELECT));
        } else {
            skip.remove(Integer.valueOf(_STEP_MAPPING));
            skip.remove(Integer.valueOf(_STEP_MODULE_SELECT));
        }
        super.next();
    }

    @Override
    protected List<IWizardPanel> getWizardPanels() {
    	List<IWizardPanel> panels = new LinkedList<IWizardPanel>();
    	panels.add(new ItemImporterSelectionPanel(this));
    	panels.add(new ItemImporterModuleSelectionPanel(this));
    	panels.add(new ItemImporterDefinitionPanel(this));
    	panels.add(new ItemImporterMappingPanel(this));
    	panels.add(new ItemImporterTaskPanel(this));
    	return panels;
    }

    @Override
    protected void initialize() {}

    @Override
    protected void saveSettings() {
        DcSettings.set(DcRepository.Settings.stItemImporterWizardFormSize, getSize());
    }
}
