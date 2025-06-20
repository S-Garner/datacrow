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

package org.datacrow.client.util.filefilters;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;

import org.datacrow.client.util.Utilities;
import org.datacrow.core.resources.DcResources;

public class PictureFileFilter extends FileFilter {

    @Override
    public boolean accept(File file) {
        
        List<String> list = Arrays.asList(ImageIO.getReaderFileSuffixes());  
        String extension = Utilities.getExtension(file);
        
        if (file.isDirectory()) {
            return true;
        } else if (list.contains(extension)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getDescription() {
        return DcResources.getText("lblPicFileFilter");
    }
}
