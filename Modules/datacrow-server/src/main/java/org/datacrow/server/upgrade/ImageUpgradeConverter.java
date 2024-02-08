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

package org.datacrow.server.upgrade;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.IImageConverterListener;
import org.datacrow.server.data.PictureManager;

/**
 * This image upgrade converter moves the images from the main images folder to the 
 * object ID folders. It expects the image files to be named in the format of version 4.11 older:
 * ObjectID_FieldName.JPG
 */
public class ImageUpgradeConverter extends Thread {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ImageUpgradeConverter.class.getName());
	
	private final IImageConverterListener listener;
	
	public ImageUpgradeConverter(IImageConverterListener listener) {
		this.listener = listener;
	}
	
	private Map<String, List<File>> getImages() {
		String imageDir = DcConfig.getInstance().getImageDir();
		Set<String> filenames = new HashSet<>();
		
		// add images located in the images folder (these will need to be moved)
		try (Stream<Path> streamFiles = Files.list(Paths.get(imageDir))) {
			filenames.addAll(streamFiles
	              .filter(file -> !Files.isDirectory(file) && !file.toString().endsWith("_small.jpg") && !file.toFile().getName().startsWith("icon_"))
	              .map(Path::toAbsolutePath)
	              .map(Path::toString)
	              .collect(Collectors.toSet()));
        } catch (Exception e) {
        	listener.notifyError(DcResources.getText("msgImageConversionFailed"));
        }
		
	    Map<String, List<File>> images = new HashMap<String, List<File>>();
	    
	    File file;
	    String ID;
	    List<File> files;
	    
	    for (String filename : filenames) {
	    	file = new File(filename);
	    	ID = file.getName().substring(0, file.getName().indexOf("_"));
	    	
	    	files = images.containsKey(ID) ? images.get(ID) : new ArrayList<File>();
	    	files.add(file);
	    	images.put(ID, files);
	    }
	    
	    return images;
	}
	
	private void savePicture(String ID, File file) throws Exception {
		
		Picture picture = new Picture(ID, file.toString());
		PictureManager.getInstance().savePicture(picture);
		
		// clean up the old files
		if (!file.delete())
			file.deleteOnExit();
		
		File small = new File(file.toString().replace(".jpg", "_small.jpg")); 
		if (small.exists()) {
			if (!small.delete())
				small.deleteOnExit();
		}

        try {
        	sleep(20);
        } catch (Exception e) {
        	logger.debug(e, e);
        }
	}
	
	@Override
	public void run() {
        try {
        	Map<String, List<File>> images = getImages();
        	
        	listener.notifyToBeProcessedImages(images.keySet().size());
        	
        	String ID;
        	List<File> files;
        	String filename;
        	
        	Iterator<String> iterator = images.keySet().iterator();
        	while (iterator.hasNext()) {
        		ID = iterator.next();
        		files = images.get(ID);
        		
        		try {
        		
	        		if (files.size() == 1) {
	        			// let the picture manager sort out the correct name
	        			savePicture(ID, files.get(0));
	        		} else {
	        			// first save images with the field name front
	        			for (File file : files) {
	        				filename = file.toString();
	        				if (filename.toLowerCase().contains("front"))
	        					savePicture(ID, file);
	        			}
	        			
	        			// next, save the rest of the images in order of appearance
	        			for (File file : files) {
	        				filename = file.toString();
	        				if (!filename.toLowerCase().contains("front"))
	        					savePicture(ID, file);
	        			}
	        		}
	        		
        		} catch (Error e) {
        			logger.error("An image could not be moved due to an error. The upgrade has failed. Please correct the error "
        					+ "and then restart the upgrade", e);
       				throw e;
        		}
        		
        		listener.notifyImageProcessed();
        	}
            
            DcSettings.set(DcRepository.Settings.stImageConversionNeeded, Boolean.FALSE);
            DcSettings.set(DcRepository.Settings.stImageUpgradeConversionNeeded, Boolean.FALSE);
            
            listener.notifyFinished();
            
        } catch (Exception e) {
        	listener.notifyError(DcResources.getText("msgImageConversionFailed"));
        }
	}
}
