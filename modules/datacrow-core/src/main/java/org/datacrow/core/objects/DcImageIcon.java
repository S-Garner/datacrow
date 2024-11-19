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

package org.datacrow.core.objects;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.CoreUtilities;

public class DcImageIcon extends ImageIcon {

	private static final long serialVersionUID = 1L;

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DcImageIcon.class.getName());

    public static final int _TYPE_JPEG = 0;
    public static final int _TYPE_PNG = 1;
    
	private byte[] bytes;
	private String filename;
	private File file;
	
	private int type = _TYPE_JPEG;

    public DcImageIcon(BufferedImage image) {
        super(image);
        
    	if (image.getColorModel().hasAlpha()) 
    		setType(_TYPE_PNG);
    }    
	
    public DcImageIcon(File file) {
    	this(toBufferedImage(file));
    	
    	this.filename = file.toString();
        this.file = new File(filename);
    }    
    
    public DcImageIcon(byte[] data) {
        this(toBufferedImage(data));
    }

    public DcImageIcon(String filename) {
    	this(new File(filename));
    }  

    public DcImageIcon(Image image) {
        super(image);
    }

    public DcImageIcon(URL location) {
        super(location);
    }
	
    public void setType(int type) {
    	this.type = type;
    }
    
    public int getType() {
    	return type;
    }
    
    public DcImageIcon toIcon() {
		int size = DcSettings.getInt(DcRepository.Settings.stIconSize);
        Image image = CoreUtilities.getScaledImage(this, size, size);
        
    	DcImageIcon scaled = new DcImageIcon(image);
    	scaled.setType(_TYPE_PNG);
    	return scaled;
    }
    
    public void save() {
        
        if (file == null && filename == null) {
            try {
                filename = getDescription();
                if (filename != null)
                    file = new File(filename);
            } catch (Exception e) {
                logger.debug("Failed to get filename as defined in the description", e);
            }
        }
        
        if (file == null) return;
        
        try {
            CoreUtilities.writeToFile(this, file);
        } catch (Exception e) {
            logger.error("Could not save icon to file " + filename, e);
        }
    }
    
    public boolean exists() {
        return file == null ? false : file.exists();
    }
    
    public void flushImage() {
    	if (getImage() != null)
    		getImage().flush();
    }
    
    public void flush() {
    	
    	flushImage();
    	
        bytes = null;
        filename = null;
        file = null;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public File getFile() {
        return file;
    }
    
    public void setFilename(String filename) {
    	this.filename = filename;
    	
    	if (filename != null)
    	    this.file = new File(filename);
    	else
    	    this.file = null;
    }
    
    public byte[] getCurrentBytes() {
        return bytes;
    }
    
    public byte[] getBytes() {
    	if (filename != null && bytes == null) 
            logger.debug("Retrieving bytes from " + filename);
    		
    	try {
    		this.bytes = bytes != null ? bytes :  
    		             filename != null ? CoreUtilities.readFile(file) : CoreUtilities.getBytes(this);
    	} catch (Exception ie) {
    		logger.error("Could not retrieve bytes from " + filename, ie);
    	}
        
    	return bytes;
    }
    
    private static BufferedImage toBufferedImage(byte[] data) {
    	try {
        	InputStream is = new ByteArrayInputStream(data);
        	BufferedImage bi = ImageIO.read(is);
        	return bi;
    	} catch (Exception e) {
    		logger.warn("Failed to read image. Restoring image data");
    		ImageIcon icon = new ImageIcon(data);
    		BufferedImage bi = recreateInvalidImage(icon);
    		icon.getImage().flush();
    		
    		logger.warn("Correction was successful.");
    		
    		return bi;
    	}
    }
    
    private static BufferedImage toBufferedImage(File file) {
    	try {
    		BufferedImage img = ImageIO.read(file);
    		return img;
    	} catch (Exception e) {
    		logger.warn("Failed to read image. Correcting the image. File: [" + file + "]");
    		// This is typically used only for failures - draw the image to a new graphics object
    		ImageIcon icon = new ImageIcon(file.toString());
    		BufferedImage bi = recreateInvalidImage(icon);
    		icon.getImage().flush();
    		
    		logger.warn("Correction for [" + file + "] was successful.");
    		
    		return bi;
    	}
    }
    
    private static BufferedImage recreateInvalidImage(ImageIcon icon) {
	    Image image = icon.getImage();

	    // Create empty BufferedImage, sized to Image
	    BufferedImage bi = 
	       new BufferedImage(
	           image.getWidth(null), 
	           image.getHeight(null), 
	           BufferedImage.TYPE_INT_ARGB);

	    // Draw Image into BufferedImage
	    Graphics g = bi.getGraphics();
	    g.drawImage(image, 0, 0, null);
		
	    return bi;    	
    }
    
    /**
     * Waits until image is fully loaded, so ready for drawing.
     */
    public void waitForLoading() {
        BufferedImage bufferedImage = new BufferedImage(1, 1,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        // prepare observer
        final Object done = new Object();
        ImageObserver imageObserver = new ImageObserver() {
            public boolean imageUpdate(java.awt.Image img, int flags,
                    int x, int y, int width, int height) {
                if (flags < ALLBITS) {
                    return true;
                } else {
                    synchronized (done) {
                        done.notify();
                    }
                    return false;
                }
            }
        };
        // draw Image with wait
        synchronized (done) {
            boolean completelyLoaded = g2.drawImage(getImage(), 0, 0,
                    imageObserver);
            if (!completelyLoaded) {
                while (true) {
                    try {
                        done.wait(0);
                        break;
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        // clean up
        g2.dispose();
    }    
}
