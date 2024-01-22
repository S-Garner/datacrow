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
		
		
		try (Stream<Path> streamDirs = Files.list(Paths.get(imageDir))) {
			imageFolders = streamDirs
		              .filter(file -> Files.isDirectory(file))
		              .map(Path::getFileName)
		              .map(Path::toString)
		              .collect(Collectors.toSet());
			
			for (String imageFolder : imageFolders) {
				try (Stream<Path> streamFiles = Files.list(Paths.get(imageFolder))) {
		        	images.addAll(streamFiles
			              .filter(file -> !Files.isDirectory(file) && !file.toString().endsWith("_small.jpg") && !file.toFile().getName().startsWith("icon_"))
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
		
		return images;
	}
	
	@Override
	public void run() {
		Set<String> images;
    	String imageDir = DcConfig.getInstance().getImageDir();
    	
        try {
        	images = getImages();
        	
        	listener.notifyToBeProcessedImages(images.size());
        	
        	DcImageIcon image;
        	File src;
        	File cpy;
        	
            for (String imageFile : images) {
            	try {
	        		src = new File(imageDir, imageFile);
	        		cpy = new File(imageDir, CoreUtilities.getUniqueID() + ".jpg");
	
	            	CoreUtilities.copy(src, cpy, true);
	            	
	            	image = new DcImageIcon(cpy);
	
	            	try {
	        			CoreUtilities.writeMaxImageToFile(image, new File(imageDir, imageFile));
	        		} catch (Error e) {
	        			if (e instanceof OutOfMemoryError)
	        				throw e;
	        			
	        			logger.error("Skipping resizing of image [" + src + "] due to an error.", e);
	        		}
	            	
	            	if (!imageFile.startsWith("icon_")) 
	            		CoreUtilities.writeScaledImageToFile(image, new File(imageDir, imageFile.replace(".jpg", "_small.jpg")));
	
		            try {
		            	sleep(20);
		            } catch (Exception e) {
		            	logger.debug(e, e);
		            }
	            	
	            	image.flush();
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
