package org.datacrow.core.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.Logger;
import org.datacrow.core.Version;
import org.datacrow.core.log.DcLogManager;

public class ServicesFile {
    
    private static Logger logger = DcLogManager.getLogger(ServicesFile.class.getName());

    private Version version;
    private File file;
    
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
