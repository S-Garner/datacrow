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
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.server.Connector;
import org.datacrow.web.DcBean;
import org.datacrow.web.model.Item;
import org.datacrow.web.model.Reference;
import org.datacrow.web.util.WebUtilities;

import jakarta.el.ELContext;
import jakarta.faces.context.FacesContext;

public abstract class ItemBean extends DcBean {

    private static final long serialVersionUID = 1L;
    
    protected Item item;
    
    public abstract void setItem(Item item);
    
    public void setReference(Reference ref) {
        Connector conn = DcConfig.getInstance().getConnector();
        DcObject dco = conn.getItem(ref.getModule(), ref.getId());
        
        if (dco != null) {
            setItem(new Item(dco));
        } else {
            WebUtilities.log(Level.WARN, "Could not find item with ID " + ref.getId() + " of module " + ref.getModule());
        }
    }
    
    public Item getItem() {
        return item;
    }
    
    protected ItemsBean getItemsBean() {
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        return (ItemsBean) elContext.getELResolver().getValue(elContext, null, "itemsBean");
    }
}
