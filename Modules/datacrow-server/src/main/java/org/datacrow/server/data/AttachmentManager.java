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
import java.util.ArrayList;
import java.util.Collection;

import org.datacrow.core.DcConfig;
import org.datacrow.core.attachments.Attachment;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.utilities.CoreUtilities;

/**
 * @author RJ
 *
 */
public class AttachmentManager {

	private transient static DcLogger logger = DcLogManager.getInstance().getLogger(AttachmentManager.class.getName());
	
	private static AttachmentManager instance;
	
	private String dir = DcConfig.getInstance().getAttachmentDir();
	
	static {
		instance = new AttachmentManager();
	}
	
	public static AttachmentManager getInstance() {
		return instance;
	}
	
	public byte[] loadAttachment(Attachment attachment) {
		try {
			return CoreUtilities.readFile(attachment.getStorageFile());
		} catch (IOException e) {
			logger.error("Could not load contents for " + attachment.getStorageFile(), e);
			return null;
		}
	}
	
	/**
	 * Server-side deletion of the attachments
	 * @param ID
	 */
	public void deleteAttachments(String ID) {
		File itemAttachmentDir = new File(dir, ID);
		
		File file;
		for (String filename : itemAttachmentDir.list()) {
			file = new File(itemAttachmentDir, filename);
			delete(file);
		}	
	}
	
	public void deleteAttachment(Attachment attachment) {
		delete(attachment.getStorageFile());
	}
	
	private void delete(File file) {
		if (!file.delete()) {
			logger.warn("Could not delete attachment [" + file.toString() + "]. Will try and delete on exit.");
			file.deleteOnExit();
		}		
	}
	
	public void saveAttachment(Attachment attachment) {
		File storageFile = attachment.getStorageFile();
		storageFile.getParentFile().mkdirs();
		
		try {
			CoreUtilities.writeToFile(attachment.getData(), storageFile);
		} catch (Exception e) {
			logger.error("Could not store attachment " + storageFile, e);
		}
	}
	
	public Collection<Attachment> getAttachments(String ID) {
		return new ArrayList<>();
	}
}
