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

package org.datacrow.core.attachments;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Date;

import org.datacrow.core.DcConfig;

public class Attachment {

	private final String name;
	private final String objectID;
	
	// this points to the temp folder / local folder of the client
	private File localFile;
	
	private byte[] data;
	
	private Date created;
	private long size;
	
	public Attachment(String objectID, String name) {
		this.objectID = objectID;
		this.name = name;
	}

	
	public Attachment(String objectID, File file) throws IOException {
		this.objectID = objectID;
		this.name = file.getName();
		
		FileTime creationTime = 
				(FileTime) Files.getAttribute(file.toPath(), "creationTime", LinkOption.NOFOLLOW_LINKS);
		
		Instant instant = creationTime.toInstant();
		setCreated(Date.from(instant));
		setSize(file.length());
	}	
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public String getName() {
		return name;
	}
	
	public String getObjectID() {
		return objectID;
	}
	
	public void clear() {
		data = null;
	}
	
	public void setLocalFile(File localFile) {
		this.localFile = localFile;
	}
	
	public File getLocalFile() {
		return localFile;
	}
	
	public File getStorageFile() {
		File storageFile = new File(DcConfig.getInstance().getAttachmentDir(), objectID);
		return new File(storageFile, name);
	}
	
	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return o != null ? o.hashCode() == hashCode() : false;
	}
}