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
 * @author Robert Jan van der Waals
 */
public class Picture implements Serializable {

	private static final long serialVersionUID = 1L;

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(Picture.class.getName());
    
    protected boolean edited = false;
    protected boolean deleted = false;
    
    private String url;
    private String thumbnailUrl;
    
    private DcImageIcon imageIcon;
    
    private final String filename;
    
    private boolean loaded = false;
    
    public Picture(String filename) {
    	this.filename = filename;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setImageIcon(DcImageIcon imageIcon) {
    	this.imageIcon = imageIcon;
    }
    public void loadImage(boolean external) {

		if (imageIcon != null) {
			imageIcon.flush();
			imageIcon = new DcImageIcon(imageIcon.getImage());
			loaded = true;
		} else if (!CoreUtilities.isEmpty(filename)) {
			if (new File(filename).exists()) {
				loaded = true;
				imageIcon = new DcImageIcon(filename);
			}
		}

		if (!loaded && !CoreUtilities.isEmpty(getUrl())) {
			try {
				URL url = new URL(getUrl());
				imageIcon = new DcImageIcon(url);
			} catch (Exception e) {
				logger.error("Error while loading image from URL: " + getUrl(), e);
			}
		}

    }
    
    public DcImageIcon getImageIcon() {
    	return imageIcon;
    }
    
    public String getFilename() {
        return filename;
    }
    
    /**
     * Gets the scaled image. If it does not exist it will create a scaled image in the
     * temp folder of the client.
     * @return
     */
    public DcImageIcon getScaledPicture() {
        String filename = getScaledFilename();
        DcImageIcon thumbnail = null;
        if (filename != null) {
            if (DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_CLIENT) {
                try {
                    thumbnail = new DcImageIcon(new URL(thumbnailUrl));
                } catch (Exception e) {
                    logger.warn("Could not load picture from URL " + thumbnailUrl, e);
                }
            } else {
                thumbnail = new DcImageIcon(new File(DcConfig.getInstance().getImageDir(), filename));
            }
        } else {
            if (imageIcon != null) {
            	File file = new File(CoreUtilities.getTempFolder(), CoreUtilities.getUniqueID() + "_small.jpg");
            	file.deleteOnExit();
            	
            	try {
            		CoreUtilities.writeScaledImageToFile(
            				new DcImageIcon(imageIcon.getImage()), file);
            		thumbnail = new DcImageIcon(file);
            	} catch (Exception e) {
            		logger.debug("Could not store scaled temporary image [" + file + "]", e);
            	}
            }
        }
        
        return thumbnail;
    }
    
    public String getScaledFilename() {
        return getScaledFilename(getFilename());
    }

    public String getScaledFilename(String filename) {
        if (filename != null) {
            try {
                int idx = filename.indexOf(".jpg");
                String plain = filename.substring(0, idx);
                String scaledFilename = plain + "_small.jpg";
                return scaledFilename;
            } catch (Exception e) {
                logger.debug("Unable to determine scaled image filename for " + filename + ". Is this a new item?", e);
            }
        }
        return null;
    }
}