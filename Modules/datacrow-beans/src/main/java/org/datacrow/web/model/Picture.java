/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.net                             *
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

package org.datacrow.web.model;

import java.io.File;

import org.datacrow.core.DcConfig;
import org.datacrow.core.server.Connector;
import org.datacrow.web.DcBean;

public class Picture extends DcBean {
	
	private String filename;
	
	private File fileLarge;
	private File fileSmall;
	
	private String url;
	
	private String name;

	private boolean deleted = false;
	private boolean edited = false;
	
	private byte[] contents = null;
	
	private boolean icon = false;
	
	public Picture(boolean icon) {
	    this.icon = icon;
	}
	
	public Picture(boolean icon, String filename) {
		this.filename = filename;
		this.icon = icon;
		
		fileLarge = new File(DcConfig.getInstance().getImageDir(), filename + ".jpg");
		
		Connector conn = DcConfig.getInstance().getConnector();
		url = "http://" + conn.getServerAddress() + ":" + conn.getImageServerPort() +"/" + filename + ".jpg";
		
		if (icon)
		    fileSmall = fileLarge;
		else
		    fileSmall = new File(DcConfig.getInstance().getImageDir(), filename + "_small.jpg");
	}
	
	public String getUrl() {
	    return url;
	}
	
	public boolean isIcon() {
	    return icon;
	}
	
	public void setFileLarge(File fileLarge) {
	    this.fileLarge = fileLarge;
	}
	
	public void setFileSmall(File fileSmall) {
	    this.fileSmall = fileSmall;
	}
	
	public boolean isEdited() {
	    return edited;
	}
	
	public void setEdited(boolean b) {
        this.edited = b;
    }
	
	public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean b) {
        this.deleted = b;
    }

    public byte[] getContents() {
        return contents;
    }

    public void setContents(byte[] contents) {
        this.contents = contents;
    }

    public String getName() {
		return name;
	}
	
	public String getFilename() {
		return filename;
	}
	
    public boolean isAlive() {
        if (filename == null || deleted)
            return false;
        else
            return fileSmall.exists() && fileLarge.exists();
    }
}
