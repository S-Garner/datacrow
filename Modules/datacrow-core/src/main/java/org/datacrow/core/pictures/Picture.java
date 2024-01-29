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

package org.datacrow.core.pictures;

import java.io.File;
import java.io.Serializable;
import java.net.URL;

import org.datacrow.core.DcConfig;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.utilities.CoreUtilities;

/**
 * A picture represents a physical picture file.
 * Every image stored in Data Crow (such as screenshots) is represented by 
 * a picture object.
 * 
 * 
 * TODO:
 * Load from URL for Server implementation.
 * 
 * 
 * @author Robert Jan van der Waals
 */
public class Picture implements Serializable {

	private static final long serialVersionUID = 1L;
	
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(Picture.class.getName());
    
	private final String objectID;
	private final String fileName;
	
	private String path;
	private String thumbnailPath;
	
    private String url;
    private String thumbnailUrl;
    
    private DcImageIcon imageIcon;
    
    public Picture(String objectID, String filename) {
    	this.objectID = objectID;
    	this.fileName = filename;
    	
    	this.path = new File(new File(DcConfig.getInstance().getImageDir(), objectID), filename).toString();
    	this.thumbnailPath = path.replace(".jpg", "_small.jpg");
    }
    
    public void load() {
    	if (imageIcon == null) {
    		imageIcon = new DcImageIcon(path);
    	} else {
    		// reload
    		imageIcon = new DcImageIcon(imageIcon.getImage());
    	}
    }
    
//    public String getUrl() {
//        return url;
//    }
//    
//    public String getThumbnailUrl() {
//        return thumbnailUrl;
//    }
//    
//    public void setThumbnailUrl(String thumbnailUrl) {
//        this.thumbnailUrl = thumbnailUrl;
//    }
//
//    public void setUrl(String url) {
//        this.url = url;
//    }

    public void setImageIcon(DcImageIcon imageIcon) {
    	this.imageIcon = imageIcon;
    }
    
    public DcImageIcon getImageIcon() {
    	if (imageIcon == null)
    		imageIcon = new DcImageIcon(path);
    	
    	return imageIcon;
    }
    
    public String getFilename() {
        return fileName;
    }
    
    /**
     * Gets the scaled image. If it does not exist it will create a scaled image in the
     * temp folder of the client.
     * @return
     */
    public DcImageIcon getScaledPicture() {
    	DcImageIcon thumbnail = null;
    	
        if (DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_CLIENT) {
            try {
                thumbnail = new DcImageIcon(new URL(thumbnailUrl));
            } catch (Exception e) {
                logger.warn("Could not load picture from URL " + thumbnailUrl, e);
            }
        } else {
            thumbnail = new DcImageIcon(thumbnailPath);
        }
        
        return thumbnail;
    }
}