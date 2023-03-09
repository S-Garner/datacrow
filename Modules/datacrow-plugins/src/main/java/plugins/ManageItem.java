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

import javax.swing.ImageIcon;

import org.apache.logging.log4j.Logger;

import org.datacrow.client.console.GUI;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.console.ISimpleItemView;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcPropertyModule;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcTemplate;
import org.datacrow.core.plugin.Plugin;
import org.datacrow.core.resources.DcResources;

public class ManageItem extends Plugin {
    
	private static final long serialVersionUID = -2881008188562123698L;

	private static Logger logger = DcLogManager.getLogger(ManageItem.class.getName());
    
	private String title;
    
    public ManageItem(DcObject dco, DcTemplate template, int viewIdx, int moduleIdx, int viewType) {
        super(dco, template, viewIdx, moduleIdx, viewType);
    }     
    
    public void setTitle(String title) {
        this.title = title; 
    }
    
    @Override
    public boolean isAdminOnly() {
        return false;
    }
    
    @Override
    public boolean isAuthorizable() {
        return true;
    }    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        DcModule module = getModule();
        if (module instanceof DcPropertyModule) {
        	ISimpleItemView view = GUI.getInstance().getItemViewForm(getModuleIdx());
        	view.setVisible(true);
        } else {
            logger.error("Invalid module! Module is not an instance of DcPropertyModule: " + module);
        }
    }
    
    @Override
    public boolean isSystemPlugin() {
        return true;
    }

    @Override
    public ImageIcon getIcon() {
        DcModule module = getModule();
        return module != null && module.getIcon16() != null ? module.getIcon16() : IconLibrary._icoModuleTypeProperty16;
    }

    @Override
    public String getLabel() {
        DcModule module = getModule();
        title = title == null ? super.getLabel() != null ? super.getLabel() : module.getObjectNamePlural() : title;
        return DcResources.getText("lblManageX", title);
    }
    
    @Override
    public String getHelpText() {
        DcModule module = getModule();
        title = title == null ? super.getLabel() != null ? super.getLabel() : module.getObjectNamePlural() : title;
        return DcResources.getText("tpManageItemX", title);
    }  
}
