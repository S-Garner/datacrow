package org.datacrow.core.utilities;

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

public class ImageConverter extends Thread {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ImageConverter.class.getName());
	
	private final IImageConverterListener listener;
	
	public ImageConverter(IImageConverterListener listener) {
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
			              .filter(file -> !Files.isDirectory(file) && !file.getParent().endsWith("icons") && !file.toString().endsWith("_small.jpg") && !file.toFile().getName().startsWith("icon_"))
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
        	File tgt = null;
        	File small = null;
        	
            for (String imageFile : images) {
            	try {
	        		src = new File(imageFile);

	            	try {
		        		if (src.getParent().endsWith("images")) { // upgrade path
	
		        			cpy = src;
		        			String ID = src.getName().substring(0, src.getName().indexOf("_"));
		        				        			
		        			tgt = new File(new File(src.getParent(), ID), src.getName());
		        			tgt.getParentFile().mkdirs();
		        			
		        			image = new DcImageIcon(cpy);
	        				CoreUtilities.writeMaxImageToFile(image, tgt);

		        			// if the scaling did not work then simply copy the file to the target.
		        			if (!tgt.exists())
		        				CoreUtilities.copy(src, tgt, false);	
		        			
		        			// delete the existing scaled version
		        			small = new File(imageFile.replace(".jpg", "_small.jpg")); 
		        			if (small.exists())
		        				small.delete();
		        			
		        			// update all variables to the newly located images
		        			imageFile = tgt.toString();
		        			
		        		} else {
		        			// normal path
		        			cpy = new File(src.getParent(), CoreUtilities.getUniqueID() + ".jpg");
		        			
		        			CoreUtilities.copy(src, cpy, false);
		        			image = new DcImageIcon(cpy);
		        			
		        			tgt = src;
		        			
		        			CoreUtilities.writeMaxImageToFile(image, src);
		        		}
		        		
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
