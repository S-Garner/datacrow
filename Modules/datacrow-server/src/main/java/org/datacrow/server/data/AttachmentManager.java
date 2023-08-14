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
import java.util.Properties;
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

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(AttachmentManager.class.getName());
	
	private static final AttachmentManager instance;
	
	private final String dir = DcConfig.getInstance().getAttachmentDir();
	
	static {
		instance = new AttachmentManager();
	}
	
	public static AttachmentManager getInstance() {
		return instance;
	}

	public void saveAttachment(Attachment attachment) {
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

            // add file information
            zipOut.putNextEntry(new ZipEntry("fileinfo.properties"));
            
            StringBuilder sb = new StringBuilder();
            sb.append("created=");
            sb.append(CoreUtilities.toString(attachment.getCreated()));
            sb.append("\r\n");
            sb.append("size=");
            sb.append(attachment.getSize());

            zipOut.write(sb.toString().getBytes(), 0, sb.length());
            zipOut.closeEntry();
            
            // add the file itself
            zipOut.putNextEntry(new ZipEntry(attachment.getName()));
            zipOut.write(attachment.getData());
            zipOut.closeEntry();
            
        } catch (Exception e) {
        	logger.error("Could not store attachment " + storageFile, e);
        } finally {
        	try { if (zipOut != null) zipOut.close();} catch (Exception e) { logger.error("Could not close resource");}
        	try { if (fos != null) fos.close(); } catch (Exception e) {logger.error("Could not close resource");}
        }
	}
	
	public void loadAttachment(Attachment attachment) {
		loadAttachment(attachment, true);
	}
	
	@SuppressWarnings("resource")
	private void loadAttachment(Attachment attachment, boolean includeData) {
		File storageFile = attachment.getStorageFile();
		File zippedFile = new File(storageFile.getAbsolutePath() + ".zip");
		ZipFile zipFile = null;
		InputStream is = null;
		ZipEntry zipEntry;
		
		try {
			zipFile = new ZipFile(zippedFile);
			
			if (includeData) {
				zipEntry = zipFile.getEntry(attachment.getName());
	            
	            if (zipEntry != null) {
	            	is = zipFile.getInputStream(zipEntry);
	                byte[] data = CoreUtilities.readBytesFromStream(is);
	                attachment.setData(data);
	            }
	
	            try { if (is != null) is.close(); } catch (Exception e) {logger.error("Could not close resource");}
			}
            
			zipEntry = zipFile.getEntry("fileinfo.properties");
            if (zipEntry != null) {
	            is = zipFile.getInputStream(zipEntry);
	            Properties p = new Properties();
	            p.load(is);

	            String sCreated = (String) p.get("created");
	            String sSize = (String) p.get("size");
	            
	            attachment.setCreated(CoreUtilities.toDate(sCreated.replace(' ', 'T')));
	            attachment.setSize(Long.valueOf(sSize));
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
		String[] files = itemAttachmentDir.list();
		
		if (files == null) return;
		
		File file;
		for (String filename : files) {
			file = new File(itemAttachmentDir, filename);
			delete(file);
		}
		
		cleanup(itemAttachmentDir);
	}
	
	public void deleteAttachment(Attachment attachment) {
		File file = new File(attachment.getStorageFile().getAbsolutePath() + ".zip");
		delete(file);
		cleanup(file.getParentFile());
	}
	
	private void cleanup(File dir) {
		String[] files = dir.list();
		
		// remove the folder when all attachments have gone
		if (files == null || files.length == 0)
			if (!dir.delete())
				dir.deleteOnExit();
	}
	
	public Collection<Attachment> getAttachments(String ID) {
		File itemAttachmentDir = new File(dir, ID);
		
		Collection<Attachment> attachments = new ArrayList<>();
		
		if (itemAttachmentDir.list() != null) {
			
			Attachment attachment;
			for (String filename : itemAttachmentDir.list()) {
				attachment = new Attachment(ID, filename.substring(0, filename.length() - 4));
				loadAttachment(attachment, false);
				attachments.add(attachment);
			}
		}
		return attachments;
	}
	
	private void delete(File file) {
		if (!file.delete()) {
			logger.warn("Could not delete attachment [" + file.toString() + "]. Will try and delete on exit.");
			file.deleteOnExit();
		}		
	}
}
