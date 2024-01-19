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
import java.util.ArrayList;
import java.util.Collection;

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
	
	public void loadPicture(Picture picture) {
		loadPicture(picture, true);
	}
	
	@SuppressWarnings("resource")
	private void loadPicture(Picture picture, boolean includeData) {}	
	
	/**
	 * Server-side deletion of the attachments
	 * @param ID
	 */
	public void deletePicture(String ID) {}
	
	public void deletePicture(Picture picture) {}
	
	private void cleanup(File dir) {}
	
	public Collection<Picture> getPictures(String ID) {
		Collection<Picture> pictures = new ArrayList<>();
		
		return pictures;
	}
}
