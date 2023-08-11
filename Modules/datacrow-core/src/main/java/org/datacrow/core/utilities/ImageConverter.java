package org.datacrow.core.utilities;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	
	@Override
	public void run() {
		Set<String> images;
    	String imageDir = DcConfig.getInstance().getImageDir();
    	
        try (Stream<Path> stream = Files.list(Paths.get(imageDir))) {
        	images = stream
	              .filter(file -> !Files.isDirectory(file) && !file.toString().endsWith("_small.jpg"))
	              .map(Path::getFileName)
	              .map(Path::toString)
	              .collect(Collectors.toSet());

        	listener.notifyToBeProcessedImages(images.size());
        	
        	DcImageIcon image;
        	File src;
        	File cpy;
        	
            for (String imageFile : images) {
        		src = new File(imageDir, imageFile);
        		cpy = new File(imageDir, CoreUtilities.getUniqueID() + ".jpg");
            	
            	CoreUtilities.copy(src, cpy, true);
            	
            	image = new DcImageIcon(cpy);

            	try {
        			CoreUtilities.writeMaxImageToFile(image, new File(imageDir, imageFile));
        		} catch (Error e) {
        			if (e instanceof OutOfMemoryError)
        				throw e;
        			
        			logger.error("Skipping resizing of image [" + src + "] dur to an error.", e);
        		}
            	
            	CoreUtilities.writeScaledImageToFile(image, new File(imageDir, imageFile.replace(".jpg", "_small.jpg")));

	            try {
	            	sleep(20);
	            } catch (Exception e) {
	            	logger.debug(e, e);
	            }
            	
            	image.flush();
            	cpy.delete();
            	
            	listener.notifyImageProcessed();
            }
            
            DcSettings.set(DcRepository.Settings.stImageConversionNeeded, Boolean.FALSE);
            
            listener.notifyFinished();
            
        } catch (Exception e) {
        	listener.notifyError(DcResources.getText("msgImageConversionFailed"));
        }
	}
}
