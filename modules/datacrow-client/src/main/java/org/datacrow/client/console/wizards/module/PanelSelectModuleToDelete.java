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

package org.datacrow.client.console.wizards.module;

import java.util.Collection;

import org.datacrow.client.console.wizards.Wizard;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.resources.DcResources;

public class PanelSelectModuleToDelete extends PanelSelectModule {

    public PanelSelectModuleToDelete(Wizard wizard) {
        super(wizard);
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgSelectModuleToDelete");
    }
    
    @Override
    public Object apply() {
        int module = super.getSelectedModule();
        
        for (DcModule m : DcModules.getCustomModules()) 
            if (m.getIndex() == module) return m.getXmlModule();
        
        return null;
    }    
    
    @Override
    protected Collection<DcModule> getModules() {
        return DcModules.getCustomModules();
    }

    @Override
    protected boolean isModuleAllowed(DcModule module) {
        return module.isCustomModule() && !DcModules.isUsedInMapping(module.getIndex());
    }
}
