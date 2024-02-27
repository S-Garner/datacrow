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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.datacrow.core.DcConfig;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.server.Connector;
import org.datacrow.core.utilities.CoreUtilities;

/**
 * @author RJ
 */
public class PictureManager {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(PictureManager.class.getName());
	
	private static final PictureManager instance;
	
	private final FilenameFilter pictureFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith("jpg") && !name.toLowerCase().contains("_small");
		}
	};
	
	static {
		instance = new PictureManager();
	}
	
	public static PictureManager getInstance() {
		return instance;
	}

	public void savePicture(Picture picture) throws Exception {

		DcImageIcon imageIcon;
		
		File target = picture.getTargetFile();
		File dir = target.getParentFile();
		
		dir.mkdirs();
		
		String name = target.getName();
		imageIcon = picture.getImageIcon();

		if (!name.startsWith("picture")) {
			String[] files = dir.list(pictureFilter);
			int number = files == null || files.length == 0 ? 1 : files.length + 1;
			name = "picture" + number + ".jpg";
			picture.setFilename(new File(dir, name).toString());
		}
		
		CoreUtilities.writeMaxImageToFile(imageIcon, picture.getTargetFile());
		CoreUtilities.writeScaledImageToFile(imageIcon, picture.getTargetScaledFile());
		
		if (exists(picture))
			deletePicture(picture);
	}
	
	private boolean exists(Picture newPic) {

		boolean exists = false;
		
		BufferedImage bi1 = null;
		BufferedImage bi2 = null;

		for (Picture oldPic : getPictures(newPic.getObjectID())) {
			
			if (oldPic.getTargetFile().equals(newPic.getTargetFile()))
				continue;
			
			try {
				bi1 = ImageIO.read(newPic.getTargetFile());
				bi2 = ImageIO.read(oldPic.getTargetFile());
				
				exists = CoreUtilities.isSameImage(bi1, bi2);

			} catch (Exception e) {
				
				logger.error("Error whilst comparing images", e);
				
			} finally {
				if (bi1 != null)
					bi1.flush();
				
				if (bi2 != null)
					bi2.flush();
			}
			
			if (exists)
				break;
		}
		
		return exists;
	}
	
	public void savePictureOrder(String objectID, LinkedList<String> filenames) {
		try {
			Collection<Picture> pictures = getPictures(objectID);
			
			Map<String, String> mapping = setAside(pictures);

			File dir = new File(DcConfig.getInstance().getImageDir(), objectID);
			
			int fieldNr;
			String newName;
			for (String oldName : mapping.keySet()) {
				newName = (String) mapping.get(oldName);
				fieldNr = filenames.indexOf(oldName) + 1;
				
				if (fieldNr >= 0) {
					new File(dir, newName).renameTo(new File(dir, "picture" + fieldNr + ".jpg"));
					new File(dir, newName + "_small").renameTo(new File(dir, "picture" + fieldNr + "_small.jpg"));
				}
			}
		} catch (Exception e) {
			logger.error("Error renumbering pictures", e);
		}
	}
	
	public void deletePictures(String objectID) {
		for (Picture picture : getPictures(objectID)) {
			picture.getTargetFile().delete();
			picture.getTargetScaledFile().delete();
		}
		
		folderCleanup(objectID);
		updateNumbering(objectID);	
	}
	
	public void deletePicture(Picture picture) {
		picture.getTargetFile().delete();
		picture.getTargetScaledFile().delete();
		
		folderCleanup(picture.getObjectID());
		updateNumbering(picture.getObjectID());
	}

	private void folderCleanup(String objectID) {
	
		File dir = new File(DcConfig.getInstance().getImageDir(), objectID);
		
		String[] files = dir.list();
		if (files == null || files.length == 0) {
			if (!dir.delete())
				dir.deleteOnExit();
		}
	}
	
	private Map<String, String> setAside(Collection<Picture> pictures) {
		
		Map<String, String> mapping = new LinkedHashMap<String, String>();
		
		File target;
		String newName;
		
		for (Picture picture : pictures) {
			target = picture.getTargetFile();
			newName = CoreUtilities.getUniqueID();
			
			mapping.put(target.getName(), newName);
			
			picture.getTargetFile().renameTo(new File(target.getParent(), newName));
			picture.getTargetScaledFile().renameTo(new File(target.getParent(), newName + "_small"));
		}
		
		return mapping;
	}
	
	private void updateNumbering(String ID) {
		try {
			Collection<Picture> pictures = getPictures(ID);
			
			Map<String, String> mapping = setAside(pictures);

			File dir = new File(DcConfig.getInstance().getImageDir(), ID);
			
			String newName;
			int numbering = 1;
			for (Object key : mapping.keySet()) {
				newName = (String) mapping.get(key);
				
				new File(dir, newName).renameTo(new File(dir, "picture" + numbering + ".jpg"));
				new File(dir, newName + "_small").renameTo(new File(dir, "picture" + numbering + "_small.jpg"));
				
				numbering++;
			}

		} catch (Exception e) {
			logger.error("Error renumbering pictures", e);
		}
	}
	
	public Collection<Picture> getPictures(String ID) {
		Collection<Picture> pictures = new LinkedList<Picture>();
		
		File dir = new File(DcConfig.getInstance().getImageDir(), ID); 
		
		if (dir.exists()) {
			
			Connector conn = DcConfig.getInstance().getConnector();
			Picture picture;
			
			try (Stream<Path> streamDirs = Files.list(Paths.get(dir.toString()))) {
				Set<String> images = streamDirs
			              .filter(file -> !Files.isDirectory(file) && !file.toString().contains("_small"))
			              .map(Path::toString)
			              .collect(Collectors.toSet());
				
				List<String> orderedImages = new ArrayList<String>(images);
				Collections.sort(orderedImages);

				for (String image : orderedImages) {
					
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
