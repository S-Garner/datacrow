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
import java.io.IOError;
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

/**
 * @author RJ
 *
 */
public class PictureManager {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(PictureManager.class.getName());
	
	private static final PictureManager instance;
	
	private final String dir = DcConfig.getInstance().getImageDir();
	
	static {
		instance = new PictureManager();
	}
	
	public static PictureManager getInstance() {
		return instance;
	}

	public void savePicture(Picture picture) {}
	
	public void deletePictures(String objectID) {}
	
	public void deletePicture(Picture picture) {}
	
	public Collection<Picture> getPictures(String ID) {
		Collection<Picture> pictures = new ArrayList<>();
		
		File dir = new File(DcConfig.getInstance().getImageDir(), ID); 
		
		if (dir.exists()) {
			
			try (Stream<Path> streamDirs = Files.list(Paths.get(dir.toString()))) {
				Set<String> images = streamDirs
			              .filter(file -> !Files.isDirectory(file) && !file.toString().endsWith("_small.jpg"))
			              .map(Path::getFileName)
			              .map(Path::toString)
			              .collect(Collectors.toSet());

				for (String image : images)
					pictures.add(new Picture(ID, image));
				
			} catch (IOException ioe) {
				logger.error("An error has occured whilst retrieving pictures for ID [" + ID + "]", ioe);
			}
		}
		
		return pictures;
	}
}
