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

import org.apache.logging.log4j.Level;
import org.datacrow.core.DcConfig;
import org.datacrow.core.server.Connector;
import org.datacrow.web.model.Item;
import org.datacrow.web.util.WebUtilities;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@Named
@SessionScoped
public class ViewItemBean extends ItemBean {

    @Override
    public void setItem(Item item) {
        this.item = item;
        
        Connector conn = DcConfig.getInstance().getConnector();
        
        this.item.setValues(conn.getItem(item.getModuleIdx(), item.getID()));
        this.item.loadAllOtherItems();
        
        // add the item to the bread crumb
        try {
            ViewItemBreadCrumbBean viewItemBreadCrumbBean = (ViewItemBreadCrumbBean) WebUtilities.getBean("viewItemBreadCrumbBean");
            viewItemBreadCrumbBean.addItem(item);
        } catch (Exception e) {
            WebUtilities.log(Level.ERROR, "Could not find / instantiate the Bread Crumb Bean", e);
        }
    }
}
