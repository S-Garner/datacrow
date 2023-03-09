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

package org.datacrow.web.util;

import org.datacrow.web.model.Item;
import org.primefaces.model.menu.DefaultMenuItem;

public class DcMenuItem extends DefaultMenuItem {

    private static final long serialVersionUID = 1L;

    private Item item;
    
    public DcMenuItem(Item item) {
        super();
        setTitle(item.getTitle());
        setValue(item.getTitle());
        this.item = item;
    }
    
    public Item getItem() {
        return item;
    }

    @Override
    public int hashCode() {
        return item.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof DcMenuItem))
            return false;
        else {
            return item.equals(((DcMenuItem) obj).getItem());
        }
    }

    @Override
    public String toString() {
        return item.toString();
    }
}
