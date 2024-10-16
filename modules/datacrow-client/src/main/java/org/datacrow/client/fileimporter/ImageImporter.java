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

package org.datacrow.client.fileimporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.ImageIO;

import org.datacrow.core.fileimporter.FileImporter;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Image;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.resources.DcResources;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.iptc.IptcDirectory;

/**
 * Imports image files.
 * 
 * @author Robert Jan van der Waals
 */
public class ImageImporter extends FileImporter {
    
    /**
     * Creates a new instance.
     */
    public ImageImporter() {
        super(DcModules._IMAGE);
    }
    
    @Override
	public FileImporter getInstance() {
		return new ImageImporter();
	}
    
    /**
     * The default supported file types.
     */
    @Override
    public String[] getSupportedFileTypes() {
        return ImageIO.getReaderFileSuffixes();
    }
    
    @Override
    public boolean allowReparsing() {
        return true;
    }    
    
    @Override
    public boolean canImportArt() {
        return false;
    }    
    
    @Override
    public DcObject parse(String filename, int directoryUsage) {
        DcObject image = DcModules.get(DcModules._IMAGE).getItem();
        
        try {
            image.setIDs();
            image.setValue(Image._A_TITLE, getName(filename, directoryUsage));
            image.setValue(Image._SYS_FILENAME, filename);
            
            BufferedImage bi = ImageIO.read(new File(filename));
            DcImageIcon icon = new DcImageIcon(bi);
            
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();
            
            image.setValue(Image._F_WIDTH, width != -1 ? Long.valueOf(width) : null);
            image.setValue(Image._G_HEIGHT, height != -1 ? Long.valueOf(height) : null);
            
            icon.getImage().flush();
            icon.setFilename(filename);
            
            image.addNewPicture(new Picture(image.getID(), icon));
            File jpegFile = new File(filename); 
            
            try {
                Metadata metadata = JpegMetadataReader.readMetadata(jpegFile);
                if (metadata.containsDirectoryOfType(ExifIFD0Directory.class)) {
                    Directory exifDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                    
                    try {
                        String camera = exifDirectory.getString(ExifIFD0Directory.TAG_MODEL);
                        image.setValue(Image._Q_CAMERA, camera);
                    } catch (Exception me) {}

                    try {
                        String description = exifDirectory.getString(ExifIFD0Directory.TAG_IMAGE_DESCRIPTION);
                        image.setValue(Image._B_DESCRIPTION, description);
                    } catch (Exception me) {}

                    try {
                        Date date = exifDirectory.getDate(ExifIFD0Directory.TAG_DATETIME);
                        if (date != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            cal.set(Calendar.HOUR_OF_DAY, 0);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            image.setValue(Image._N_DATE, cal.getTime());
                        }
                    } catch (Exception me) {}
                }
                
                if (metadata.containsDirectoryOfType(IptcDirectory.class)) {
                    try {
                        Directory iptcDirectory = metadata.getFirstDirectoryOfType(IptcDirectory.class);
                        String city = iptcDirectory.getString(IptcDirectory.TAG_CITY);
                        String country = iptcDirectory.getString(IptcDirectory.TAG_COUNTRY_OR_PRIMARY_LOCATION_NAME);
                        String state = iptcDirectory.getString(IptcDirectory.TAG_PROVINCE_OR_STATE);
                        
                        String location = "";
                        location += country != null ? country + ", " : "";
                        location += state != null ? state  + ", " : "";
                        location += city != null ? city : "";
                        
                        image.setValue(Image._P_PLACE, location);
                    } catch (Exception me) {}
                }
                
            } catch (JpegProcessingException jpe) {}
            
        } catch (Exception exp) {
            getClient().notify(DcResources.getText("msgCouldNotReadInfoFrom", filename));
        }
        
        return image;
    }
    
	@Override
	public String getFileTypeDescription() {
		return DcResources.getText("lblPicFileFilter");
	}
}
