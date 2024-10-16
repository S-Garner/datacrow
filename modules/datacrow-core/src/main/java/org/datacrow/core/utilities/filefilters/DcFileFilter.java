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

import org.datacrow.core.resources.DcResources;

public class DcFileFilter extends javax.swing.filechooser.FileFilter {
	
	private final String[] extensions;
	private final String description;
	
	
    /** 
     * Create a file filter for the give extension
     * @param extension criterium to filter
     */
	public DcFileFilter(String extension) {
		this(new String[] {extension});
	}

    public DcFileFilter(String[] extensions) {
        this(extensions, null);
    }
    
    /** 
     * Create a file filter for the give extension
     * @param extension criterium to filter
     */
	public DcFileFilter(String extension, String description) {
		this(new String[] {extension}, description);
	}

    public DcFileFilter(String[] extensions, String description) {
        this.extensions = extensions;
        this.description = description == null ? createDescription(extensions) : description;
    }
    
    public String createDescription(String[] extensions) {
        String files = "";
        for (int i = 0; i < extensions.length; i++)
            files += (i > 0 ? ", " : "") + extensions[i];
        
        return DcResources.getText("lblFileFiler", files);
    }
    
    /**
     * Check the file with the filter
     * @param file file to check on
     */
    @Override
    public boolean accept(File file) {
        
        if (file == null || file.toString() == null) return false;
        
        if (file.isDirectory()) {
            return true;
        } else {
            String filename = file.toString().toLowerCase();
            for (int i = 0; i < extensions.length; i++) {
                if (filename.endsWith(extensions[i].toLowerCase()))
                    return true;
            }
        }
        return false;
    }
    
    /**
     * Get the description of this filter for displaying purposes
     */
    @Override
    public String getDescription() {
    	return description;
    }
} 