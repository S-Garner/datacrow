/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.net                             *
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

package org.datacrow.web.bean;

import java.io.Serializable;

import org.apache.logging.log4j.Level;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.resources.DcResources;
import org.datacrow.web.util.WebUtilities;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class MainMenuBean implements Serializable {

    private MenuModel model;

    public MainMenuBean() {
        model = new DefaultMenuModel();
        
        ModulesBean modulesBean;
        LoginBean loginBean;
        DcModule module;
        try {
            modulesBean = (ModulesBean) WebUtilities.getBean("modulesBean");
            
            if (modulesBean.getSelectedModuleIdx() == -1) return;
            
            module = DcModules.get(modulesBean.getSelectedModuleIdx());
            
            loginBean = (LoginBean) WebUtilities.getBean("loginBean");
            
            if (loginBean.isEditingAllowed(module)) {
                DefaultSubMenu firstSubmenu = new DefaultSubMenu();
                firstSubmenu.setLabel(DcResources.getText("lblEdit"));
                
                DefaultMenuItem item = new DefaultMenuItem();
                item.setTitle(DcResources.getText("lblNewItem", module.getObjectName()));
                
                item.setIcon("fa fa-plus-circle");
                item.setCommand("#{editItemBean.add}");
                item.setUpdate(":editItemDetail");
                item.setOncomplete("PF('editItemDetail').show()");                    
                
                firstSubmenu.getElements().add(item);
                
                model.getElements().add(firstSubmenu);
            }
            
            
            if (loginBean.isAdmin()) {
                DefaultSubMenu firstSubmenu = new DefaultSubMenu();
                firstSubmenu.setLabel(DcResources.getText("lblSettings"));
                
                DefaultMenuItem item = new DefaultMenuItem();
                item.setTitle(DcResources.getText("lblItemFormSettings", module.getObjectName()));
                
                item.setIcon("fa fa-wrench");
                item.setUpdate(":editItemFormsettings");
                item.setOncomplete("PF('editItemFormsettings').show()");                    
                
                firstSubmenu.getElements().add(item);
                model.getElements().add(firstSubmenu);
            }
            
        } catch (Exception e) {
            WebUtilities.log(Level.ERROR, e);
        }
    }

    public MenuModel getModel() {
        return model;
    }
}
