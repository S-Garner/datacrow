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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.net.URL;

import javax.imageio.ImageIO;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.settings.objects.DcDimension;
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
    
	private String objectID;
	private String filename;
    private String url;
    private String thumbnailUrl;
    
    private transient DcImageIcon imageIcon;
    
    private boolean itemIsNew = false;
    
    private byte[] bytes;
    
    public Picture(String objectID, DcImageIcon imageIcon) {
    	this.imageIcon = imageIcon;
    	this.objectID = objectID;
		this.filename = imageIcon.getFilename();
		
		setItemIsNew(true);
    }
    
    private void setItemIsNew(boolean b) {
    	this.itemIsNew =b;
    }
    
    public Picture(String objectID, File file) {
    	this(objectID, file.toString());
    }
    
    public Picture(String objectID, String filename) {
    	this.objectID = objectID;
    	this.filename = filename;
    }
    
    public void prepareForTransfer(boolean loadBytes) {
    	if (loadBytes) {
    		// scale the image before sending
    		if (DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_CLIENT) {
    			DcDimension dimMax = DcSettings.getDimension(DcRepository.Settings.stMaximumImageResolution);
	    		DcImageIcon icon = getImageIcon(); 
	    		BufferedImage bi = CoreUtilities.getScaledImage(icon, dimMax.getWidth(), dimMax.getHeight());
	    		this.bytes = new DcImageIcon(bi).getBytes();
	    		
	    		bi.flush();
    		} else {
    			this.bytes = getImageIcon().getBytes();
    		}
    	}
    	
    	if (this.imageIcon != null)
    		this.imageIcon.flush();
    	
    	this.imageIcon = null;
    }
    
    public void setFilename(String filename) {
    	this.filename = filename;
    }
    
    public void setObjectID(String objectID) {
    	this.objectID = objectID;
    }
    
    public void load() {
    	if (!CoreUtilities.isEmpty(getUrl())) {
            try {
                URL url = new URL(getUrl());
                imageIcon = new DcImageIcon(url);
            } catch (Exception e) {
                logger.error("Error while loading image from URL: " + getUrl(), e);
            }
    		
    	} else {
    		if (imageIcon == null) {
	     		imageIcon = new DcImageIcon(filename);
	     	} else {
	    		imageIcon = new DcImageIcon(imageIcon.getImage());
	    	}     		
         }
    }
    
    /**
     * Gets the scaled image. If it does not exist it will create a scaled image in the temp folder of the client.
     * @return
     */
    public DcImageIcon getScaledPicture() {
    	
    	DcImageIcon thumbnail = null;
    	
    	if (itemIsNew) {
    		load();
    		return new DcImageIcon(CoreUtilities.getScaledImage(imageIcon));
    		
    	} else {
            if (DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_CLIENT) {
                try {
                    thumbnail = new DcImageIcon(new URL(thumbnailUrl));
                } catch (Exception e) {
                    logger.warn("Could not load picture from URL " + thumbnailUrl, e);
                }
            } else {
            	// no filename, but there is a image icon: store a scaled version to disk.
            	if (imageIcon != null && !getTargetScaledFile().exists()) {
            		File file = new File(CoreUtilities.getTempFolder(), CoreUtilities.getUniqueID() + "_small.jpg");
                	file.deleteOnExit();
                	
                	try {
                		CoreUtilities.writeScaledImageToFile(imageIcon, file);
                		thumbnail = new DcImageIcon(file);
                	} catch (Exception e) {
                		logger.debug("Could not store scaled temporary image [" + file + "]", e);
                	}    		
            	} else {
            		thumbnail = new DcImageIcon(getTargetScaledFile());	
            	}
            }
    	}
        
        return thumbnail;
    }    
    
    public void clear() {
    	if (imageIcon != null)
    		imageIcon.flush();
    }   
    
    public String getObjectID() {
    	return objectID;
    }
    
    public File getTargetFile() {
    	String name = filename == null ? CoreUtilities.getUniqueID() + ".jpg" : 
    		new File(filename).getName();
    	
    	return new File(
    			new File(DcConfig.getInstance().getImageDir(), objectID), name);
    }
    
    public File getTargetScaledFile() {
    	String path = getTargetFile().toString();
    	path = path.substring(0, path.lastIndexOf(".")) + "_small.jpg";
    	return new File(path);
    }    

    public void setUrl(String url) {
        this.url = url;
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

    public void setImageIcon(DcImageIcon imageIcon) {
    	this.imageIcon = imageIcon;
    }
    
    public DcImageIcon getImageIcon() {
    	if (imageIcon == null) {
    		if (bytes != null) {
    			imageIcon = new DcImageIcon(bytes);
    		} else if (DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_CLIENT) {
    			
    			if (url != null) {
        			try {
	    				Image image = ImageIO.read(new URL(url));
	    				imageIcon = new DcImageIcon(image);
	    			} catch (Exception ioe) {
	    				logger.error("Could not read image from URL: " + url, ioe);
	    			}
    			} else {
        			try {
	    				imageIcon = new DcImageIcon(filename);
	    			} catch (Exception ioe) {
	    				logger.error("Could not read image from file: " + filename, ioe);
	    			}
    			}
    		} else {  
    			imageIcon = new DcImageIcon(filename);
    		}
    	}
    	
    	return imageIcon;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public Picture clone() {
    	load();
    	
    	DcImageIcon newImageIcon = new DcImageIcon(imageIcon.getBytes());
    	newImageIcon.setFilename(getFilename());
    	
    	Picture picture = new Picture(objectID, newImageIcon);
    	picture.setItemIsNew(itemIsNew);
    	return picture;
    }
}