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

package org.datacrow.client.console.components.panels.tree;

import java.io.File;

import org.datacrow.core.IconLibrary;
import org.datacrow.core.objects.DcImageIcon;

public class FileNodeElement extends NodeElement {
    
    private final File file;
    
    public FileNodeElement(String key, File file) {
    	super(key, key, null);
    	this.file = file;
    }

    @Override
    public DcImageIcon getIcon() {
    	if (file == null) {
    		return IconLibrary._icoFolderSystemExists;
    	} else {
    	    if (!file.exists()) {
    	        return IconLibrary._icoFileSystemNotExists;
    	    } else if (file.isDirectory()) {
    	        return IconLibrary._icoFolderSystemExists;
    	    } else {
    	        if (file.getName().equals(displayValue))
    	            return IconLibrary._icoFileSystemExists;
    	        else 
    	            return IconLibrary._icoFolderSystemExists;
    	    }				  
    	}
    }
    
	@Override
    public String toString() {
    	int count = getCount();
        if (count <= 1) 
            return getDisplayValue();
        else 
            return getDisplayValue() + " (" + String.valueOf(count) + ")";    
    }

    @Override
    public boolean equals(Object o) {
        if (getKey() == null || o == null || !(o instanceof FileNodeElement))
            return false;
        else 
            return getKey().equals(((FileNodeElement) o).getKey());
    }
}
