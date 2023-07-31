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

package org.datacrow.core.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.datacrow.core.Version;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;

public class ServicesFile {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ServicesFile.class.getName());

    private final Version version;
    private final File file;
    
    public ServicesFile(File file) throws IOException {
        this.file = file;
        this.version = determineVersion();
    }
    
    public Version getVersion() {
        return version;
    }
    
    public void delete() {
        file.delete();
    }
    
    private Version determineVersion() throws IOException {
        String s = "0.0.0";
        
        // delete existing jar files
        ZipFile zf = new ZipFile(file);
        Properties p = new Properties();

        try {
            ZipEntry entry;
            String name;
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                name = entry.getName();
                
                if (name.endsWith("services.properties")) {
                    try {
                        InputStream is = zf.getInputStream(entry);
                        p.load(is);
                        s = p.getProperty("version");
                        is.close();
                    } catch (IOException ie) {
                        logger.error("Could not read version.properties from online services jar file", ie); 
                    }                        
                    break;
                }
            }
        } finally {
            zf.close();       
        }

        return new Version(s);
    }
}
