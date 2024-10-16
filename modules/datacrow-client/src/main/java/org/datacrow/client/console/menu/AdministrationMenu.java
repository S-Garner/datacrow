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

package org.datacrow.client.console.menu;

import org.datacrow.client.console.components.DcMenu;
import org.datacrow.client.plugins.PluginHelper;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.modules.DcPropertyModule;
import org.datacrow.core.modules.ExternalReferenceModule;
import org.datacrow.core.resources.DcResources;

public class AdministrationMenu extends DcMenu {
    
	public AdministrationMenu(DcModule module) {
        super(DcResources.getText("lblAdministration"));
        
        DcModule child = module.getChild();
        
        int sourceIdx;
        for (DcPropertyModule pm : DcModules.getPropertyModules(module)) {
            sourceIdx = pm.isServingMultipleModules() ? pm.getIndex() : pm.getIndex() - module.getIndex();
            if (child != null && child.hasReferenceTo(sourceIdx) && !pm.isServingMultipleModules()) {
                add(pm, module.getObjectName() + " " + pm.getObjectNamePlural());
                add((DcPropertyModule) DcModules.get(sourceIdx + child.getIndex()), 
                    child.getObjectName() + " " + DcModules.get(sourceIdx + child.getIndex()).getObjectNamePlural());
            } else {
                add(pm, pm.getObjectNamePlural());
            }
        }

        // add references to property modules for the child
        if (child != null) {
            for (DcPropertyModule pm : DcModules.getPropertyModules(child)) {
                sourceIdx = pm.isServingMultipleModules() ? pm.getIndex() : pm.getIndex() - child.getIndex();
                if (!module.hasReferenceTo(sourceIdx))
                    add(pm, child.getObjectName() + " " + pm.getObjectNamePlural());
            }
        }
        
        if (module.isTopModule() &&  module.getTemplateModule() != null) {
        	addSeparator();
        	
            PluginHelper.add(this, "ManageTemplate", 
                    module.getObjectName() + " " +  module.getTemplateModule().getObjectNamePlural(), 
                    module.getIndex());

            if (module.getChild() != null && module.getChild().getTemplateModule() != null) {
                PluginHelper.add(this, "ManageTemplate", 
                               module.getChild().getObjectName() + " " +  module.getTemplateModule().getObjectNamePlural(), 
                               module.getChild().getIndex());
            }
        }
    }
    
    private void add(DcPropertyModule pm, String title) {
        if (pm != null && !(pm instanceof ExternalReferenceModule))
            PluginHelper.add(this, "ManageItem", title, pm.getIndex());
    }
}
