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

package plugins;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ImageIcon;

import org.datacrow.client.console.windows.ItemTypeDialog;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.UserMode;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcTemplate;
import org.datacrow.core.plugin.Plugin;
import org.datacrow.core.resources.DcResources;

public class ItemImporterWizard extends Plugin {

	private static final long serialVersionUID = 1L;

	public ItemImporterWizard(DcObject dco, DcTemplate template, int viewIdx, int moduleIdx, int viewType) {
        super(dco, template, viewIdx, moduleIdx, viewType);
    }   
    
    @Override
    public boolean isAdminOnly() {
        return true;
    }
    
    @Override
    public boolean isAuthorizable() {
        return false;
    }    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        DcModule module = DcModules.getCurrent();
        Collection<DcModule> modules = new ArrayList<DcModule>();
        modules.add(module);
        modules.addAll(DcModules.getReferencedModules(module.getIndex()));
        
        ItemTypeDialog dlg = new ItemTypeDialog(modules, DcResources.getText("msgSelectModuleImport"));
        dlg.setVisible(true);

        int moduleIdx = dlg.getSelectedModule();
        
        if (moduleIdx > 0)
            new org.datacrow.client.console.wizards.itemimport.ItemImporterWizard(moduleIdx).setVisible(true);
    }
    
    @Override
    public ImageIcon getIcon() {
        return IconLibrary._icoItemImport16;
    }
    
    @Override
    public boolean isSystemPlugin() {
        return true;
    }

    @Override
    public String getLabel() {
        return DcResources.getText("lblItemImportWizard");
    }
    
    @Override
    public int getXpLevel() {
        return UserMode._XP_EXPERT;
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("tpItemImportWizard");
    }      
}
