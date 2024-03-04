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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.datacrow.core.DcConfig;
import org.datacrow.core.attachments.Attachment;
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
    }
    
    private File getImageFile(Picture picture) {
        File dir = new File(exportDir, exportName +  "_images/" + picture.getObjectID() + "/");
        dir.mkdirs();
        return new File(dir, picture.getTargetFile().getName());
    }
    
    private File getAttachmentFile(Attachment attachment) {
        File dir = new File(exportDir, exportName +  "_attachments/" + attachment.getObjectID() + "/");
        dir.mkdirs();
        return new File(dir, attachment.getStorageFile().getName());
    }   
    
    public String getAttachmentURL(Attachment attachment) {
        File attachmentFile = getAttachmentFile(attachment);
        copyAttachment(attachment, attachmentFile);

        try {
        	String attachmentFileName = URLEncoder.encode(attachmentFile.getName(), "UTF8");
	        if (settings.getBoolean(ItemExporterSettings._ALLOW_RELATIVE_FILE_PATHS))
	            return "./" + exportName + "_attachments/" + attachment.getObjectID() + "/" + attachmentFileName;
	        else 
	            return "file:///" + attachmentFile.getParent().replace('\\', '/') + "/" + attachmentFileName;
        } catch (UnsupportedEncodingException ueu) {
        	logger.error("An error occured whilst trying to create an URL for attachment " + attachment, ueu);
        }
        
        return null;
    }
    
    public String getImageURL(Picture p) {
        String url = "";
        File src = p.getTargetFile(); 
        File imageFile = getImageFile(p);
        
        if (settings.getBoolean(ItemExporterSettings._COPY_IMAGES)) {
            copyImage(p, imageFile);

            if (settings.getBoolean(ItemExporterSettings._ALLOW_RELATIVE_FILE_PATHS))
                url = "./" + exportName + "_images/" + p.getObjectID() + "/" + imageFile.getName();
            else 
            	return "file:///" + imageFile.toString().replace('\\', '/');
        } else {
        	url = !CoreUtilities.isEmpty(p.getUrl()) ? p.getUrl() : "file:///" + src.toString();
        }

        return url;
    }
    
    private void copyAttachment(Attachment attachment, File target) {
        try {
        	
    		DcConfig.getInstance().getConnector().loadAttachment(attachment);
    		CoreUtilities.writeToFile(attachment.getData(), target);
            
            
        } catch (Exception e) {
            logger.error("An error occurred while copying image to " + target, e);
        }
    }     
    
    private void copyImage(Picture picture, File target) {
        try {
            picture.load();
            
            if (settings.getBoolean(ItemExporterSettings._SCALE_IMAGES)) {
                int width = settings.getInt(ItemExporterSettings._IMAGE_WIDTH);
                int height = settings.getInt(ItemExporterSettings._IMAGE_HEIGHT);
                CoreUtilities.writeScaledImageToFile(picture.getImageIcon(), target, width, height);
            } else {
                CoreUtilities.writeToFile(picture.getImageIcon(), target);
            }
        } catch (Exception e) {
            logger.error("An error occurred while copying image to " + target, e);
        }
    }    
}
