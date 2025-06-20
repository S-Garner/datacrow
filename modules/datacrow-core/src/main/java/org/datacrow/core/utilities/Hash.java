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

package org.datacrow.core.utilities;

import java.io.File;
import java.io.IOException;

import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.settings.DcSettings;

import net.jacksum.JacksumAPI;
import net.jacksum.algorithms.AbstractChecksum;


public class Hash {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(Hash.class.getName());
    private static Hash instance;
    private AbstractChecksum checksum;
    
    private Hash() {}
    
    public static Hash getInstance() {
        instance = instance == null ? new Hash() : instance;
        return instance;
    }
    
    public void setHashType(String type) {
        try {
            checksum = JacksumAPI.getChecksumInstance(type);
        } catch (Exception e) {
            logger.error(type + " is not supported. Hash could not be calculated.");
        }
    }
    
    public String calculateHash(String filename) {
        initChecksum();
        String hash = null;
        try {
            checksum.reset();
            checksum.readFile(filename);
            hash = checksum.getValueFormatted().toUpperCase();
        } catch (IOException e) {
            logger.error("Error while trying to calculate hash for file " + filename, e);
        }
        return hash;
    }

    public void calculateHash(DcObject dco) {
        initChecksum();
        
        String filename = dco.getFilename();

        String hash = null;
        String hashType = null;
        Long fileSize = null;
        
        if (!CoreUtilities.isEmpty(filename) && new File(filename).exists()) {
            try {
                String currentHash = (String) dco.getValue(DcObject._SYS_FILEHASH);
                String currentHashType = (String) dco.getValue(DcObject._SYS_FILEHASHTYPE);
                
                Object currentFilesize = dco.getValue(DcObject._SYS_FILESIZE);
                
                // check if the file size should be set
                if (CoreUtilities.isEmpty(currentFilesize) || dco.isChanged(DcObject._SYS_FILENAME)) {
                    fileSize = CoreUtilities.getSize(new File(filename));
                } else {
                    fileSize = currentFilesize instanceof Long ? (Long) currentFilesize : Long.valueOf(0);
                }
                
                dco.setValue(DcObject._SYS_FILESIZE, fileSize);
                
                long max = DcSettings.getLong(DcRepository.Settings.stHashMaxFileSizeKb) * 1000;
                // check if the hash should be changed
                if ( (max == 0 || fileSize <= max) &&
                    ((currentHash == null || (dco.isChanged(DcObject._SYS_FILENAME)  && 
                                             !dco.isChanged(DcObject._SYS_FILEHASH))) || 
                    !checksum.getName().equals(currentHashType))) {
                                           
                    checksum.reset();
                    checksum.readFile(filename);
                
                    hash = checksum.getValueFormatted().toUpperCase();
                    hashType = checksum.getName();
                } else {
                    hash = currentHash;
                    hashType = currentHashType;
                }

                dco.setValue(DcObject._SYS_FILEHASH, hash);
                dco.setValue(DcObject._SYS_FILEHASHTYPE, hashType);
                
                checksum.reset();

            } catch (IOException e) {
                logger.error("Error while trying to calculate hash for file " + filename, e);
            }
        }
    }
    
    private void initChecksum() {
        if (checksum == null || !checksum.getName().equals(DcSettings.getString(DcRepository.Settings.stHashType)))
            setHashType(DcSettings.getString(DcRepository.Settings.stHashType));        
    }
}
