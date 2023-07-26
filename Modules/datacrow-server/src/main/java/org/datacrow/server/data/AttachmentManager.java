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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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
	
	public void loadAttachment(Attachment attachment) {
		File storageFile = attachment.getStorageFile();
		storageFile.getParentFile().mkdirs();

		File zippedFile = new File(storageFile.getAbsolutePath() + ".zip");
		@SuppressWarnings("resource")
		ZipFile zipFile = null;
		InputStream is = null;
		
		try {
			zipFile = new ZipFile(zippedFile);
			ZipEntry zipEntry = zipFile.getEntry(attachment.getName());
            
            if (zipEntry != null) {
            	is = zipFile.getInputStream(zipEntry);
                byte[] data = CoreUtilities.readBytesFromStream(is);
                attachment.setData(data);
            }			
			
		} catch (Exception e) {
			logger.error("Could not load contents for " + attachment.getStorageFile(), e);
        } finally {
        	try { if (zipFile != null) zipFile.close(); } catch (Exception e) {logger.error("Could not close resource");}
        	try { if (is != null) is.close(); } catch (Exception e) {logger.error("Could not close resource");}
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
		File file = new File(attachment.getStorageFile().getAbsolutePath() + ".zip");
		delete(file);
	}
	
	private void delete(File file) {
		if (!file.delete()) {
			logger.warn("Could not delete attachment [" + file.toString() + "]. Will try and delete on exit.");
			file.deleteOnExit();
		}		
	}
	
	public void saveAttachment(Attachment attachment) {
		storeAttachment(attachment);
	}
	
	private void storeAttachment(Attachment attachment) {
		File storageFile = attachment.getStorageFile();
		storageFile.getParentFile().mkdirs();

		File zippedFile = new File(storageFile.getAbsolutePath() + ".zip");
		
		@SuppressWarnings("resource")
		FileOutputStream fos = null;
        ZipOutputStream zipOut = null;
        
        try {
            fos = new FileOutputStream(zippedFile);
            zipOut = new ZipOutputStream(fos);
            zipOut.setLevel(Deflater.BEST_COMPRESSION);
            
            // add the version and add the comment entered by the customer
            zipOut.putNextEntry(new ZipEntry(attachment.getName()));
            zipOut.write(attachment.getData());
            zipOut.closeEntry();
            
            // add all the file
        } catch (Exception e) {
        	logger.error("Could not store attachment " + storageFile, e);
        } finally {
        	try { if (fos != null) fos.close(); } catch (Exception e) {logger.error("Could not close resource");}
        	try { if (zipOut != null) zipOut.close(); } catch (Exception e) {logger.error("Could not close resource");}
        }
	}
		
	public Collection<Attachment> getAttachments(String ID) {
		File itemAttachmentDir = new File(dir, ID);
		
		Collection<Attachment> attachments = new ArrayList<>();
		
		if (itemAttachmentDir.list() != null) {
			for (String filename : itemAttachmentDir.list())
				attachments.add(new Attachment(ID, filename.substring(0, filename.length() - 4)));
		}
		
		return attachments;
	}
}
