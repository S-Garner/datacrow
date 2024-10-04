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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.IImageConverterListener;

/**
 * This converter changes the image size based on the (now hidden) settings.
 */
public class ImageSizeConverter extends Thread {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ImageSizeConverter.class.getName());
	
	private final IImageConverterListener listener;
	
	public ImageSizeConverter(IImageConverterListener listener) {
		this.listener = listener;
	}
	
	private Set<String> getImages() {
		String imageDir = DcConfig.getInstance().getImageDir();
		Set<String> imageFolders;
		
		Set<String> images = new HashSet<>();
		
		// add images located folders
		try (Stream<Path> streamDirs = Files.list(Paths.get(imageDir))) {
			imageFolders = streamDirs
		              .filter(file -> Files.isDirectory(file))
		              .map(Path::getFileName)
		              .map(Path::toString)
		              .collect(Collectors.toSet());
			
			for (String imageFolder : imageFolders) {
				try (Stream<Path> streamFiles = Files.list(Paths.get(new File(imageDir, imageFolder).toString()))) {
		        	images.addAll(streamFiles
			              .filter(file -> !Files.isDirectory(file) && !file.getParent().endsWith("icons") && file.toString().endsWith(".jpg") && !file.toString().endsWith("_small.jpg") && !file.toFile().getName().startsWith("icon_"))
			              .map(Path::toAbsolutePath)
			              .map(Path::toString)
			              .collect(Collectors.toSet()));
		        } catch (Exception e) {
		        	listener.notifyError(DcResources.getText("msgImageConversionFailed"));
		        	break;
		        }
			}
        } catch (Exception e) {
        	listener.notifyError(DcResources.getText("msgImageConversionFailed"));
        }
		
		// add images located in the images folder (these will need to be moved)
		try (Stream<Path> streamFiles = Files.list(Paths.get(imageDir))) {
        	images.addAll(streamFiles
	              .filter(file -> !Files.isDirectory(file) && !file.toString().endsWith("_small.jpg") && !file.toFile().getName().startsWith("icon_"))
	              .map(Path::toAbsolutePath)
	              .map(Path::toString)
	              .collect(Collectors.toSet()));
        } catch (Exception e) {
        	listener.notifyError(DcResources.getText("msgImageConversionFailed"));
        }

	    return images;
	}
	
	@Override
	public void run() {
        try {
        	Set<String> images = getImages();
        	
        	listener.notifyToBeProcessedImages(images.size());
        	
        	DcImageIcon image = null;
        	File src = null;
        	File cpy = null;

            for (String imageFile : images) {
            	try {
	        		src = new File(imageFile);

	            	try {
		        		
	        			cpy = new File(src.getParent(), CoreUtilities.getUniqueID() + ".jpg");
	        			
	        			CoreUtilities.copy(src, cpy, false);
	        			image = new DcImageIcon(cpy);
	        			
	        			CoreUtilities.writeMaxImageToFile(image, src);
		        		
		            	if (!imageFile.startsWith("icon_")) 
		            		CoreUtilities.writeScaledImageToFile(image, new File(imageFile.replace(".jpg", "_small.jpg")));
		
			            try {
			            	sleep(20);
			            } catch (Exception e) {
			            	logger.debug(e, e);
			            }		        		
		        		
	        		} catch (Error e) {
	        			if (e instanceof OutOfMemoryError)
	        				throw e;
	        			
	        			logger.error("Skipping resizing of image [" + src + "] due to an error.", e);
	        		}
	            	

	            	if (image != null)
	            		image.flush();
	            	
	            	if (cpy != null)
	            		cpy.delete();
	            	
            	} catch (Exception e) {
            		logger.warn("Could not convert [" + imageFile + "]. Skipping and keeping the original image.", e);
            	}
            	
            	listener.notifyImageProcessed();
            }
            
            DcSettings.set(DcRepository.Settings.stImageConversionNeeded, Boolean.FALSE);
            
            listener.notifyFinished();
            
        } catch (Exception e) {
        	listener.notifyError(DcResources.getText("msgImageConversionFailed"));
        }
	}
}
