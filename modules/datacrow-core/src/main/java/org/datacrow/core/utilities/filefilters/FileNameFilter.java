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

package org.datacrow.core.utilities.filefilters;

import java.io.File;
import java.io.FilenameFilter;

public class FileNameFilter implements FilenameFilter {

    private final String[] extensions;
    private final boolean allowDirs;
    private final String description;
    
    public FileNameFilter(String[] extensions, String description, boolean allowDirs) {
        this.extensions = extensions;
        this.allowDirs = allowDirs;
        this.description = description;
    }
    
    public FileNameFilter(String extension, String description, boolean allowDirs) {
    	this(new String[] {extension}, description, allowDirs);
    }
    
    public String[] getExtensions() {
        return extensions;
    }

    @Override
    public boolean accept(File dir, String name) {
        boolean isDir = new File(dir, name).isDirectory();
        
        if (isDir && allowDirs) {
            return true;
        } else if (isDir) {
            return false;
        } else {
            for (int i = 0; i < extensions.length; i++) {
                if (name.toLowerCase().endsWith(extensions[i].toLowerCase()))
                    return true;
            }
        }
        return false;
    }
    
    public String getDescription() {
    	return description;
    }
}
