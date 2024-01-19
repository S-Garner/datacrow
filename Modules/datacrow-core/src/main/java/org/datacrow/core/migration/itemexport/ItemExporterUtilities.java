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

package org.datacrow.core.migration.itemexport;

import java.io.File;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.utilities.CoreUtilities;

public class ItemExporterUtilities {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ItemExporterUtilities.class.getName());
    
    private final ItemExporterSettings settings;
    private final String exportDir;
    
    private String exportName;
    
    public ItemExporterUtilities(String exportFilename, ItemExporterSettings settings) {
        File file = new File(exportFilename);
        
        this.settings = settings;
        this.exportName = file.getName();
        this.exportDir = file.getParent();
        this.exportName = exportName.lastIndexOf(".") > -1 ? exportName.substring(0, exportName.lastIndexOf(".")) : exportName;
        
        if (settings.getBoolean(ItemExporterSettings._COPY_IMAGES))
            new File(getImageDir()).mkdirs();
    }
    
    private String getImageDir() {
        return new File(exportDir, exportName +  "_images/").toString();
    }
    
    public String getImageURL(Picture p) {
    	
    	// TODO: reimplement.
    	
    	return "";
    	
//        String url = "";
//        String imageFilename = p.getFilename(); 
//        
//        if (!CoreUtilities.isEmpty(imageFilename)) {
//            if (settings.getBoolean(ItemExporterSettings._COPY_IMAGES)) {
//                copyImage(p,  new File(getImageDir(), imageFilename));
//                
//                if (settings.getBoolean(ItemExporterSettings._ALLOWRELATIVEIMAGEPATHS))
//                    url = "./" + exportName + "_images/" + imageFilename;
//                else 
//                    url = "file:///" +  new File(getImageDir(), imageFilename);
//            } else {
//            	url = !CoreUtilities.isEmpty(p.getUrl()) ? p.getUrl() : "file:///" + imageFilename;
//            }
//        }
//        return url;
    }
    
    private void copyImage(Picture picture, File target) {
//        try {
//            picture.loadImage(false);
//            
//            if (settings.getBoolean(ItemExporterSettings._SCALE_IMAGES)) {
//                int width = settings.getInt(ItemExporterSettings._IMAGE_WIDTH);
//                int height = settings.getInt(ItemExporterSettings._IMAGE_HEIGHT);
//                CoreUtilities.writeScaledImageToFile(picture.getImageIcon(), target, width, height);
//            } else {
//                CoreUtilities.writeToFile(picture.getImageIcon(), target);
//            }
//        } catch (Exception e) {
//            logger.error("An error occurred while copying image to " + target, e);
//        }
    }    
}
