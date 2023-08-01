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

package org.datacrow.server.db;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.Picture;
import org.datacrow.core.objects.ValidationException;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.server.Connector;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.settings.objects.DcDimension;
import org.datacrow.core.utilities.Base64;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.server.data.DataManager;

/**
 * The Query class creates SQL statements needed to remove, update, insert and 
 * select items from the database. Queries created by this class ensure the integrity 
 * of the data.
 * 
 * Note that the Query class can actually contain several SQL statements.
 * 
 * @author Robert Jan van der Waals
 */
public abstract class Query {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(Query.class.getName());
    
    private final int module;
    
    private final SecuredUser su;
    
    private boolean success = true;
    private boolean log = true;

    /**
     * Constructs a new Query object. 
     * @param queryType type of query
     * @param dco template
     * @param options query options
     * @param requests actions / requests to be executed
     * @throws SQLException
     */
    public Query(
    		SecuredUser su, 
    		int module) {
    	
        this.module = module;
        this.su = su;
    } 
    
    protected SecuredUser getUser() {
    	return su;
    }
    
    protected void setSuccess(boolean success) {
        this.success = success;
    }
    
    protected boolean isSuccess() {
        return success;
    }
    
    public boolean isLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

    public abstract List<DcObject> run();
    
    protected void saveReferences(Collection<DcMapping> references, String parentID) {
    	Connector connector = DcConfig.getInstance().getConnector();
    	
        for (DcMapping mapping : references) {
            try {
                mapping.setValue(DcMapping._A_PARENT_ID, parentID);
                // mappings have been dropped in the previous step; insert these as new
                mapping.setNew(true);
                connector.saveItem(mapping);
            } catch (ValidationException ve) {
                logger.error("An error occured while inserting the following reference " + mapping, ve);
            }                    
        }
    }
    
    @SuppressWarnings("resource")
	protected PreparedStatement getPreparedStament(String sql) throws SQLException {
        return DatabaseManager.getInstance().getConnection(getUser()).prepareStatement(sql);
    }
    
    protected void setValues(PreparedStatement ps, Collection<Object> values) {
        int pos = 1;
        for (Object value : values) {
            try {
                ps.setObject(pos, value);
                pos++;
            } catch (Exception e) {
                logger.error("Could not set value [" + value + "] on position [" + pos + "] for " + ps, e);
                try {ps.setObject(pos, null); pos++;} catch (Exception e2) {
                    logger.error("Could not set [" + pos + "] to NULL (to correct error with value [" + value + "])", e2);
                }
            }
        }
    }
    
    protected Object getQueryValue(DcObject dco, int index) {
        return CoreUtilities.getQueryValue(dco.getValue(index), dco.getField(index));
    }
    
    public int getModuleIdx() {
        return module;
    }
    
    public DcModule getModule() {
        return DcModules.get(getModuleIdx());
    }

    @SuppressWarnings("unchecked")
    protected void createReferences(DcObject dco) {
    	
    	DataManager dm = DataManager.getInstance();
    	
        Object value;
        DcObject reference;
        DcObject existing;
        
        Connector connector = DcConfig.getInstance().getConnector();
        
        for (DcField field : dco.getFields()) {        
            value = dco.getValue(field.getIndex());
            if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                reference = value instanceof DcObject ? (DcObject) value : null;
                if (reference == null) continue;
                
                // also created references for the sub items of this reference...
                createReferences(reference);
                
                try { 
                    existing = dm.getItem(getUser(), reference.getModule().getIndex(), reference.getID(), null);
                    existing = existing == null ? dm.getItemByKeyword(
                    		getUser(), reference.getModule().getIndex(), reference.toString()) : existing;
                    if (existing == null) {
                        reference.setValidate(false);
                    	connector.saveItem(reference);
                    } else {
                        // reuse the existing value
                        dco.setValue(field.getIndex(), existing);
                    }

                } catch (Exception e) {
                    logger.error("Error (" + e + ") while creating a new reference item; " + reference, e);
                }
                
            } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                if (value == null)
                    continue;
                
                for (DcMapping mapping : (Collection<DcMapping>) value) {
                    reference = mapping.getReferencedObject();
                    try { 
                        if (reference == null) continue;
                        
                        // also created references for the sub items of this reference...
                        createReferences(reference);
                        
                        existing = dm.getItem(getUser(), reference.getModule().getIndex(), reference.getID(), null);
                        existing = existing == null ? dm.getItemByKeyword(getUser(), reference.getModule().getIndex(), reference.toString()) : existing;

                        if (existing == null) {
                            reference.setValidate(false);
                            connector.saveItem(reference);
                        } else {
                            mapping.setValue(DcMapping._B_REFERENCED_ID, existing.getID());
                        }
                    } catch (Exception e) {
                        logger.error("Error (" + e + ") while creating a new reference item; " + reference, e);
                    }
                }
            }
        }
    }  
    
    protected void deleteImage(Picture picture) {
        String filename = (String) picture.getValue(Picture._C_FILENAME);

        if (filename == null) return;
        
        String filename1 = DcConfig.getInstance().getImageDir() + filename;

        File file1 = new File(filename1);
        if (file1.exists()) file1.delete();
        
        String filename2 = picture.getScaledFilename(DcConfig.getInstance().getImageDir() + filename);
        
        File file2 = new File(filename2);
        if (file2.exists()) file2.delete();
    }
    
    protected void saveIcon(String icon, DcField field, String ID) {
        File file = new File(DcConfig.getInstance().getImageDir(), "icon_" + ID + ".jpg");
        
        if (!CoreUtilities.isEmpty(icon)) {
            try {
                CoreUtilities.writeToFile(Base64.decode(icon.toCharArray()), file);
            } catch (Exception e) {
                logger.warn("Could not save icon to disk", e);
            }
        } else if (file.exists()){
            file.delete();
        }
    }
    
    protected void saveImage(Picture picture) {
        String filename = picture.getImageFilename();
        
        if (filename == null)  return;
        
        File file = new File(DcConfig.getInstance().getImageDir(), filename);
        String imageFile = file.toString();

        DcDimension maxDimension = DcSettings.getDimension(DcRepository.Settings.stMaximumImageResolution);
        
        try {
            if (file.exists())
                file.delete();
            
            DcImageIcon icon = (DcImageIcon) picture.getValue(Picture._D_IMAGE);
            icon = icon.getCurrentBytes() != null ? new DcImageIcon(icon.getCurrentBytes()) : icon;
            
    		CoreUtilities.writeScaledImageToFile(
    				icon, 
    				file, 
    				DcImageIcon._TYPE_PNG, 
    				maxDimension.getWidth(), 
    				maxDimension.getHeight());
    		CoreUtilities.writeScaledImageToFile(
    				icon,
    				new File(picture.getScaledFilename(imageFile)));
    		
        } catch (Exception e) {
            logger.error("Could not save [" + imageFile + "]", e);
        }
    }
}
