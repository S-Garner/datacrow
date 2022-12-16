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

package org.datacrow.web.model;

import java.io.File;

import org.datacrow.core.DcConfig;

import org.primefaces.model.tagcloud.DefaultTagCloudItem;

/**
 * Represents a list item.
 */
public class Reference extends DefaultTagCloudItem {

    private String label;
    private String ID;
    private int module;
    
    private boolean iconExists = false;
    private boolean exists = true;

    public Reference(String label, String ID, int module) {
        super();
        
        setStrength(1);
        
        this.label = label;
        this.ID = ID;
        this.module = module;
        
        iconExists = new File(DcConfig.getInstance().getImageDir(),  "icon_" + ID + ".jpg").exists();
    }
    
    public int getModule() {
        return module;
    }
    
    public void setExists(boolean b) {
        exists = b;
    }
    
    public boolean isExisting() {
        return exists;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public String getId() {
        return ID;
    }
    
    public String getIconFilename() {
        return "icon_" + ID;
    }
    
    public boolean isIconExists() {
        return iconExists;
    }
    
    @Override
    public String toString() {
        return label;
    }
 
    @Override   
    public boolean equals(Object other) {
        return (other instanceof Reference) ? ID.equals(((Reference) other).getId()) : false;
    }

    @Override
    public int hashCode() {
        return ID.hashCode() + label.hashCode();
    }    
}
