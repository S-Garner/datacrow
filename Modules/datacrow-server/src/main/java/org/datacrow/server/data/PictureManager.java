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

package org.datacrow.server.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.datacrow.core.DcConfig;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.server.Connector;
import org.datacrow.core.utilities.CoreUtilities;

/**
 * @author RJ
 */
public class PictureManager {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(PictureManager.class.getName());
	
	private static final PictureManager instance;
	
	static {
		instance = new PictureManager();
	}
	
	public static PictureManager getInstance() {
		return instance;
	}

	public void savePicture(Picture picture) {
		String filename = picture.getFilename();
		File file = new File(filename);
		
		if (file.exists()) {
			File target = picture.getTargetFile();
			target.getParentFile().mkdirs();
			
			try {
				CoreUtilities.writeMaxImageToFile(picture.getImageIcon(), target);
				CoreUtilities.writeScaledImageToFile(picture.getImageIcon(), picture.getTargetScaledFile());
			} catch (Exception e) {
				logger.error("Image could not be saved", e);
			}
		}
	}
	
	public void deletePictures(String objectID) {}
	
	public void deletePicture(Picture picture) {}
	
	public Collection<Picture> getPictures(String ID) {
		Collection<Picture> pictures = new ArrayList<>();
		
		File dir = new File(DcConfig.getInstance().getImageDir(), ID); 
		
		if (dir.exists()) {
			
			Connector conn = DcConfig.getInstance().getConnector();
			Picture picture;
			
			try (Stream<Path> streamDirs = Files.list(Paths.get(dir.toString()))) {
				Set<String> images = streamDirs
			              .filter(file -> !Files.isDirectory(file) && !file.toString().contains("_small"))
			              .map(Path::toString)
			              .collect(Collectors.toSet());

				for (String image : images) {
					
					picture = new Picture(ID, image);
					
					if (DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_SERVER) {
						
		                String address = "http://" + conn.getServerAddress() + ":" + conn.getImageServerPort() +"/" + dir.getName() + "/";
		                picture.setUrl(address + new File(image).getName());
		                picture.setThumbnailUrl(address + picture.getTargetScaledFile().getName());
		            }
					
					pictures.add(picture);
				}
				
			} catch (IOException ioe) {
				logger.error("An error has occured whilst retrieving pictures for ID [" + ID + "]", ioe);
			}
		}
		
		return pictures;
	}
}
