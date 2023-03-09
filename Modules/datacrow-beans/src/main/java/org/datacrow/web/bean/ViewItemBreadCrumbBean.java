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

package org.datacrow.web.bean;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.datacrow.web.DcBean;
import org.datacrow.web.model.Item;
import org.datacrow.web.util.DcMenuItem;
import org.datacrow.web.util.WebUtilities;
import org.primefaces.event.MenuActionEvent;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@Named
@SessionScoped 
public class ViewItemBreadCrumbBean extends DcBean {

    private MenuModel model;
    
    private List<DcMenuItem> items = new ArrayList<DcMenuItem>();

    public void reset() {
        model = new DefaultMenuModel();
        items.clear();
    }
    
    public ViewItemBreadCrumbBean() {
        reset();
    }
    
    public void addItem(Item item) {
        DcMenuItem mi = new DcMenuItem(item);
        
        if (!items.contains(mi)) {
            
            if (items.size() == 0)
                mi.setIcon("ui-icon-home");
            
            mi.setUpdate(":viewItemDetail");
            mi.setOncomplete("PF('viewItemDetail').show()");
            
            mi.setCommand("#{viewItemBreadCrumbBean.selectItem}");
            mi.setId(String.valueOf(items.size()));

            model.getElements().add(mi);
            items.add(mi);
        } else {
            DefaultMenuModel newModel = new DefaultMenuModel();
            items.clear();
            
            List<MenuElement> elements = model.getElements();
            MenuElement element;
            for (int i = 0; i < elements.size(); i++ ) {
                element = elements.get(i);
                
                newModel.getElements().add(element);
                items.add((DcMenuItem) element);
                
                if (element.equals(mi))
                    break;
            }
            
            model = newModel;
        }
    }
    
    public MenuModel getModel() {
        return model;
    }
    
    public void selectItem(MenuActionEvent event) {
        DcMenuItem menuItem = (DcMenuItem) event.getMenuItem();
        
        Item item = menuItem.getItem();

        // add the item to the bread crumb
        try {
            ItemBean itemBean = (ItemBean) WebUtilities.getBean("viewItemBean");
            itemBean.setItem(item);
        } catch (Exception e) {
            WebUtilities.log(Level.ERROR, "Could not find / instantiate the Bread Crumb Bean", e);
        }
    }
}
