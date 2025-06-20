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

package org.datacrow.core.objects;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.attachments.Attachment;
import org.datacrow.core.data.DcIconCache;
import org.datacrow.core.enhancers.IValueEnhancer;
import org.datacrow.core.enhancers.ValueEnhancers;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.helpers.ExternalReference;
import org.datacrow.core.objects.helpers.Movie;
import org.datacrow.core.objects.helpers.Software;
import org.datacrow.core.objects.template.Templates;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.Base64;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.Hash;
import org.datacrow.core.utilities.StringUtils;
import org.datacrow.core.utilities.definitions.DcFieldDefinition;
import org.datacrow.core.utilities.definitions.DcFieldDefinitions;

/**
 * This class is what it is all about. Each DcObject represents an item 
 * within Data Crow. DcObjects are very generic by nature. There are no direct
 * getters and setters for their values. Instead the values are stored in a Map object
 * and the values are retrieved by using the field indices. 
 * <br>
 * It's recommended before starting new development of plugins to create so called
 * helper classes for your new module. Examples of helper classes are {@link Software}
 * and {@link Movie}.
 * <br>
 * DcObjects are managed and maintained by the Data Manager class ({@link DataManager}.
 * Each DcObject belongs to a (@link {@link DcModule}).
 * 
 * @author Robert Jan van der Waals
 */
public class DcObject implements Comparable<DcObject>, Serializable {
    
	private static final long serialVersionUID = 1L;

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DcObject.class.getName());

    private final Map<Integer, DcValue> values = new HashMap<Integer, DcValue>();
    protected transient final List<DcObject> children = new ArrayList<DcObject>();

    public static final int _ID = 0;
    public static final int _VALUE = 217;
    
    public static final int _SYS_MODULE = 201;
    public static final int _SYS_AVAILABLE = 202;
    public static final int _SYS_LENDBY = 203;
    public static final int _SYS_LOANDURATION = 204;
    public static final int _SYS_CREATED = 205;
    public static final int _SYS_MODIFIED = 206;
    public static final int _SYS_SERVICE = 207;
    public static final int _SYS_SERVICEURL = 208;
    public static final int _SYS_FILEHASH = 209;
    public static final int _SYS_FILESIZE = 210;
    public static final int _SYS_FILENAME = 211;
    public static final int _SYS_FILEHASHTYPE = 212;

    public static final int _SYS_CONTAINER = 213;
    public static final int _SYS_DISPLAYVALUE = 214;
    
    public static final int _SYS_LOANDUEDATE = 215;
    public static final int _SYS_LOANSTATUSDAYS = 216;
    public static final int _SYS_LOANSTATUS = 219;
    public static final int _SYS_LOANSTARTDATE = 220;
    public static final int _SYS_LOANENDDATE = 221;
    public static final int _SYS_LOANALLOWED = 223;
    
    public static final int _SYS_TAGS = 222;
    public static final int _SYS_EXTERNAL_REFERENCES = 218;
    
    public static final int _SYS_PICTURE_01 = 231;
    public static final int _SYS_PICTURE_02 = 232;
    public static final int _SYS_PICTURE_03 = 233;
    public static final int _SYS_PICTURE_04 = 234;
    public static final int _SYS_PICTURE_05 = 235;
    public static final int _SYS_PICTURE_06 = 236;
    public static final int _SYS_PICTURE_07 = 237;
    public static final int _SYS_PICTURE_08 = 238;
    public static final int _SYS_PICTURE_09 = 239;
    public static final int _SYS_PICTURE_10 = 240;
    
    protected final int module;
    
    private boolean isNew = true;
    private transient boolean loaded = false;  
    private transient boolean lastInLine = true;
    private transient boolean validate = true;
    private transient boolean updateGUI = true;
    
    // these are new pictures, to be saved after the main item has been saved only
    private final LinkedList<Picture> newPictures = new LinkedList<Picture>();
    
    // these are new attachments, to be saved after the main item has been saved only
    private final Collection<Attachment> newAttachments = new ArrayList<Attachment>();
    
    /**
     * Creates a new instance.
     * @param module
     */
    public DcObject(int module) {
        this.module = module;

        // initialize the values map
        int[] fields = getModule().getFieldIndices();
        for (int i = 0; i < fields.length; i++) {
            values.put(fields[i], new DcValue());
        }
        markAsUnchanged();
    } 

    /**
     * Indicates the item is last in line of a save or delete action. 
     * Items last in line (of a batch) will cause additional GUI updates to be performed.
     */
    public boolean isLastInLine() {
        return lastInLine;
    }

    /**
     * Indicates the item is last in line of a save or delete action. 
     * Items last in line (of a batch) will cause additional GUI updates to be performed.
     */
    public void setLastInLine(boolean lastInLine) {
        this.lastInLine = lastInLine;
    }

    /**
     * Indicates whether ANY interface updates should be performed.
     */
    public boolean isUpdateGUI() {
		return updateGUI;
	}
    
    public void clearNewPictures() {
    	newPictures.clear();
    }
    
    public void clearNewAttachments() {
    	newAttachments.clear();
    }
    
    public void addNewPicture(Picture picture) {
    	if (!this.newPictures.contains(picture))
    		newPictures.add(picture);
    }
    
    public void addNewPictures(Collection<Picture> pictures) {
    	for (Picture picture : pictures)
    		this.newPictures.add(picture);
    }

    public void addNewAttachment(Attachment attachment) {
    	if (!this.newAttachments.contains(attachment))
    		this.newAttachments.add(attachment);
    }    

    public Collection<Attachment> getNewAttachments() {
    	return newAttachments;
    }

    public Collection<Picture> getNewPictures() {
    	return newPictures;
    }

    /**
     * Indicate whether ANY interface updates should be performed.
     * Setting this to false will only cause the database to be updated but will no push the update
     * to the GUI. By default this value is set to true.
     */
	public void setUpdateGUI(boolean updateGUI) {
		this.updateGUI = updateGUI;
	}

	public boolean isLoaded() {
        return loaded;
    }

    public void reload() {
        loaded = isNew;
        load(getFieldIndices());
    }
    
    public void load(int[] fields) {
    	load(fields, false);
    }
    
    /**
     * Loads the item from the database.
     * Initializes images, references and loan information.
     */
    public void load(int[] fields, boolean overruleLoadCheck) {
    	
    	long start = logger.isDebugEnabled() ? new Date().getTime() : 0;
    	
    	if ((loaded && !overruleLoadCheck) || isNew) return;
    	
    	fields = fields == null ? getFieldIndices() : fields;
        Connector conn = DcConfig.getInstance().getConnector();
        DcObject dco = conn.getItem(module, getID(), fields);
        copy(dco, fields, true, true);
    	
		if (logger.isDebugEnabled()) {
			logger.info("Item " + toString() + " was loaded in " + (new Date().getTime() - start) + "ms");
		}
        
		markAsUnchanged();
		
		loaded = true;
    }    
    
    public int getSystemDisplayFieldIdx() {
        return getModule().getSystemDisplayFieldIdx();
    }

    public boolean isDestroyed() {
        return values == null;
    }
    
    /**
     * Educated guess..
     */
    public int getDisplayFieldIdx() {
        return getModule().getDisplayFieldIdx();
    }
    
    /**
     * The default sort field index. In case the user has not specified the field to sort on 
     * this value will be used. 
     */
    public int getDefaultSortFieldIdx() {
        return DcObject._ID;
    } 
    
    /**
     * Returns the name of this object based on the field settings. If the field settings do
     * no specify any descriptive fields the default name field index is used as defined in the
     * module definition.
     */
    protected String getName() {
        String name = "";
        boolean descriptiveFound = false;
        for (DcFieldDefinition definition :  DcModules.get(module).getFieldDefinitions().getDefinitions()) {
            if (definition.isDescriptive() && definition.isEnabled()) {
                int idx = definition.getIndex();
                String disp = getDisplayString(idx);
                descriptiveFound = true;
                if (disp.length() > 0)
                    name += (name.length() > 0 ? ", " + disp : disp);
            }
        }
        
        if (!descriptiveFound)
            name = getDisplayString(getModule().getNameFieldIdx());    
        
        return name;
    }
    
    public String getScaledImageUrl() {
    	Connector conn = DcConfig.getInstance().getConnector();
    	String url = "http://" + conn.getServerAddress() + ":" + 
        		conn.getImageServerPort() +"/" + getID() + "/picture1_small.jpg";
    	return url;
    }
    
    public String getImageUrl() {
    	Connector conn = DcConfig.getInstance().getConnector();
    	String url = "http://" + conn.getServerAddress() + ":" + 
        		conn.getImageServerPort() +"/" + getID() + "/picture1.jpg";
    	return url;
    }    
    
    public DcImageIcon getScaledImage() {
    	
    	if (getID() == null)
    		return null;
    	
    	if (newPictures.size() > 0 && isNew()) {
    		
    		return new DcImageIcon(
    				CoreUtilities.getScaledImage(newPictures.getFirst().getImageIcon()));
    		
    	} else {
        	try {
    	    	if (DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_CLIENT) {
    	    		
    	            URL url = new URL(getScaledImageUrl());
    	            
    	            return new DcImageIcon(url);
    			
    			} else {
    				File file = new File(
    		    			new File(DcConfig.getInstance().getImageDir(), getID()), "picture1_small.jpg");
    		
    				if (file.exists())
    					return new DcImageIcon(file);
    			}
        	} catch (Exception e) {
        		logger.error("An error occured while retrieving the scaled image", e);
        	}
    	}
    	
    	return null;
    }
    
    /**
     * Is this object capable of storing IDs? In most cases the answer should be yes.
     */
    public boolean hasPrimaryKey() {
        return getField(_ID) != null;
    }
    
    /**
     * Retrieves the value objects.
     */
    public Map<Integer, DcValue> getValues() {
        return values;
    }
    
    /**
     * Set the value using the database field name as key.
     * @param column
     * @param value
     */
    public void setValueForColumn(String column, Object value) {
        for (DcField field : getFields()) {
            if (field.getDatabaseFieldName().equalsIgnoreCase(column)) {
                setValue(field.getIndex(), value);
                break;
            }
        }
    }
    
    /**
     * Set the value using the system name of the field.
     * @param sysName
     * @param value
     */
    public void setValueForName(String sysName, Object value) {
        for (DcField field : getFields()) {
            if (field.getSystemName().equalsIgnoreCase(sysName)) {
                setValue(field.getIndex(), value);
                break;
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public String getExternalReference(String type) {
        Collection<DcObject> references = (Collection<DcObject>) getValue(_SYS_EXTERNAL_REFERENCES);
        references = references == null ? new ArrayList<DcObject>() : references;
        for (DcObject mapping : references) {
            DcObject reference = ((DcMapping) mapping).getReferencedObject();

            if (    reference != null &&
                    reference.getValue(ExternalReference._EXTERNAL_ID_TYPE) != null &&
                    reference.getValue(ExternalReference._EXTERNAL_ID_TYPE).equals(type)) {
                
                return (String) reference.getValue(ExternalReference._EXTERNAL_ID);
            }
        }
        return null;
    }
    
    /**
     * Adds or updates the existing external key of the specified type.
     * @param type The type of the key
     * @param key The external key / ID
     */
    @SuppressWarnings("unchecked")
    public void addExternalReference(String type, String key) {
        Collection<DcObject> c = (Collection<DcObject>) getValue(_SYS_EXTERNAL_REFERENCES);
        c = c == null ? new ArrayList<DcObject>() : c;
        
        Collection<DcObject> references = new ArrayList<DcObject>();
        for (DcObject mapping : c) {
            if (mapping != null) 
                references.add(mapping);
        }
        
        c.clear();
        setValue(_SYS_EXTERNAL_REFERENCES, references);
        boolean set = false;
        try {
            for (DcObject mapping : references) {
                DcObject reference = ((DcMapping) mapping).getReferencedObject();
                if (reference != null && reference.getDisplayString(ExternalReference._EXTERNAL_ID_TYPE).equals(type)) {
                    reference.setValue(ExternalReference._EXTERNAL_ID, key);
                    set = true;
                }
            }
        } catch (Exception e) {
            logger.error("Could not determine if external ID already exists", e);
        }
        
        if (!set && !CoreUtilities.isEmpty(type)) { 
            DcObject er = DcModules.get(module + DcModules._EXTERNALREFERENCE).getItem();
            er.setValue(ExternalReference._EXTERNAL_ID, key);
            er.setValue(ExternalReference._EXTERNAL_ID_TYPE, type);
            er.setIDs();
            createReference(DcObject._SYS_EXTERNAL_REFERENCES, er);
        }
    }

    /**
     * The database table name.
     */
    public String getTableName() {
    	return getModule().getTableName();
    }

    public String getTableShortName() {
        return getModule().getTableShortName();
    }

    /**
     * Retrieves the database column count. This count will differ from the field count as
     * some fields are calculated and not stored in the database.
     * @return
     */
    public int getDatabaseFieldCount() {
        int count = 0;
        for (DcField field : getFields())
            count += field.isUiOnly() ? 0 : 1;

        return count;
    }

    public String getLabel(int index) {
        return getField(index).getLabel();
    }

    public void initializeReferences(int index, boolean full) {
        DcField field = getField(index);
        
        if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
            int mappingModIdx = DcModules.getMappingModIdx(getModule().getIndex(), field.getReferenceIdx(), field.getIndex());
            Collection<DcObject> mo = DcConfig.getInstance().getConnector().getReferences(mappingModIdx, getID(), full);
            
            // initialize the referenced object on the server side
            for (DcObject dco : mo)
                ((DcMapping) dco).getReferencedObject();
            
            setValue(index, mo);
        } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE &&
                getValue(field.getIndex()) != null) {
                Object o = getValue(field.getIndex());
                if (o instanceof String)
                    setValue(index, DcConfig.getInstance().getConnector().getItem(field.getReferenceIdx(), (String) o));    
        }
    }
    
    /**
     * Loads the actual reference information. Uses the Data Manager to retrieve the 
     * references and stores them in this object.
     */
    public void initializeReferences() {
        int[] fields = getFieldIndices();
        for (int i = 0; i < fields.length; i++) {
            DcField field = getField(fields[i]);
            
            if (field == null) continue;
            
            if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                initializeReferences(fields[i], true);
            }
        }
    }
    
    /**
     * Does this field contains a value?
     * @param index
     */
    public boolean isFilled(int index) {
        return !CoreUtilities.isEmpty(getValue(index));
    }

    /**
     * Remove all children from this object
     */
    public void removeChildren() {
        if (children != null)
            children.clear();
    }

    public void setNew(boolean b) {
        this.isNew = b;
    }
    
    public boolean isNew() {
        return isNew;
    }
    
    /**
     * Load all children.
     */
    public void loadChildren(int[] fields) {
        if (getModule().getChild() == null || isNew()) 
            return;
        
        children.clear();
        int childIdx = getModule().getChild().getIndex();
        for (DcObject dco : DcConfig.getInstance().getConnector().getChildren(getID(), childIdx, fields)) {
            children.add(dco);
        }
    }  

    public void setChildren(Collection<DcObject> children) {

        this.children.clear();
        this.children.addAll(children);
        
        for (DcObject child : children)
            child.setValue(child.getParentReferenceFieldIndex(), getID());
    }

    public void addChild(DcObject child) {
        if (child.getParentReferenceFieldIndex() == DcObject._SYS_CONTAINER) {
        	child.createReference(child.getParentReferenceFieldIndex(), this);
        } else {
        	boolean changed = child.isChanged(child.getParentReferenceFieldIndex());
        	child.setValue(child.getParentReferenceFieldIndex(), getID());
        	child.setChanged(child.getParentReferenceFieldIndex(), changed);
            children.add(child);
        }
    }

    /**
     * Retrieves the child objects belonging to this item.
     * @return The children or null of none.
     */
    public List<DcObject> getChildren() {
    	if ((children == null || children.size() == 0) && getModule().getChild() != null)
    		loadChildren(null);
    	
        return getCurrentChildren();
    }
    
    /**
	 * Gets the children as they have been currently set (without reloading them).
	 */
    public List<DcObject> getCurrentChildren() {
        return children != null ? new ArrayList<DcObject>(children) : new ArrayList<DcObject>();
    }
    
    /**
     * Retrieves the ID of the parent of this object. 
     * @return The parent ID or null.
     */
    public String getParentID() {
        Object o = getValue(getParentReferenceFieldIndex());
        
        if (o instanceof DcObject)
            return ((DcObject) o).getID();
        else 
            return (String) o;
    }

    /**
     * Retrieves the index of the field which is used to hold the link to the parent. 
     */
    public int getParentReferenceFieldIndex() {
        return getModule().getParentReferenceFieldIndex();
    }

    public DcImageIcon getIcon() {
        return DcIconCache.getInstance().getIcon(this);
    }
    
    /**
     * The icon used to represent this item.
     */
    public DcImageIcon createIcon() {
        DcField field = getModule().getIconField();
        
        if (field != null) {
            String value = (String) getValue(field.getIndex());
            DcImageIcon icon = null;
            
            if (value != null && value.length() > 1) 
                icon = CoreUtilities.base64ToImage(value);
            
            return icon;
        }

        return null;
    }

    public DcField getFileField() {
        return getModule().getFileField();
    }

    /**
     * Retrieves the filename value. This will only generate a result if the object
     * has a file field.
     * @return The file name or null.
     */
    public String getFilename() {
        DcField field = getFileField();
        return field != null ? CoreUtilities.getValidPath((String) getValue(field.getIndex())) : null; 
    }

    /**
     * Retrieves the module to which this object belongs.
     */
    public DcModule getModule() {
        return DcModules.get(module);
    }
    
    /**
     * Retrieves the module to which this object belongs.
     */
    public int getModuleIdx() {
        return module;
    }

    /**
     * Retrieves all fields belonging to this object .
     */
    public Collection<DcField> getFields() {
        return getModule().getFields();
    }

    /**
     * Mark all fields as unchanged. This does not reset the values to their
     * original values! (use {@link #load()})
     */
    public void markAsUnchanged() {
        try {
            for (DcValue value : values.values())
                value.setChanged(false);
        } catch (Exception ignore) {}
        
        // 22032008: Removed "markAsUnchanged" on child objects!
        // this broke saving the permissions (and possibly other item save's).
    }
    
    /**
     * Update the loan information.
     */
    public void setLoanInformation() {
        if (getModule().canBeLend()) {
            Loan loan = DcConfig.getInstance().getConnector().getCurrentLoan(getID());
            setLoanInformation(loan);
        }
    }
    
    /**
     * Update the loan information based on the supplied loan object.
     */
    public void setLoanInformation(Loan loan) {
        
        if (getModule().canBeLend()) {

            boolean overdue = loan.isOverdue();
            boolean available = loan.isAvailable(getID());
            
            String status =  overdue ? DcResources.getText("lblLoanOverdue") : 
                             available ? DcResources.getText("lblAvailable") : 
                             DcResources.getText("lblLoanLent");
            
            Long daysTillOverdue = loan.getDaysTillDueDate();
            daysTillOverdue = daysTillOverdue == null ? Long.valueOf(0) : daysTillOverdue;
            
            Long daysLoaned = loan.getDaysLoaned();
            daysLoaned = daysLoaned == null ? Long.valueOf(0) : daysLoaned;
            
            setValue(DcObject._SYS_LOANSTARTDATE, loan.getValue(Loan._A_STARTDATE));
            setValue(DcObject._SYS_LOANENDDATE, loan.getValue(Loan._B_ENDDATE));
            setValue(DcObject._SYS_LENDBY, loan.getPerson());
            setValue(DcObject._SYS_AVAILABLE, loan.isAvailable(getID()));
            setValue(DcObject._SYS_LENDBY, loan.getPerson());
            setValue(DcObject._SYS_LOANDUEDATE, loan.getDueDate());
            setValue(DcObject._SYS_LOANDURATION, loan.getDaysLoaned());
            setValue(DcObject._SYS_LOANSTATUS, status);
            setValue(DcObject._SYS_LOANSTATUSDAYS, available ? null : Long.valueOf(overdue ? daysTillOverdue * -1 : daysLoaned));
        }
    }

    /**
     * Sets a value on this object.
     * @param index The field index.
     * @param o The value to be set.
     */
    @SuppressWarnings("unchecked")
    public void setValue(int index, Object o) {
        
        if (isDestroyed()) {
            logger.warn("System tried to set a value while the object was already destroyed");
            return;
        }
        
        DcValue value = getValueDef(index);
        DcField field = getField(index);
        
        if (value != null) {
            if (index == _SYS_EXTERNAL_REFERENCES && getValue(index) != null && o != null ) {
                mergeReferences((Collection<DcMapping>) o);
            } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                o = CoreUtilities.sort((List<DcObject>) o);
                value.setValue(o, field);
            } else {
                value.setValue(o, field);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void mergeReferences(Collection<DcMapping> mappings) {
        Collection<DcMapping> currentMappings = (Collection<DcMapping>) getValue(DcObject._SYS_EXTERNAL_REFERENCES);
            
        // external references are always merged!
        for (DcMapping mapping : mappings) {
            DcObject reference = mapping.getReferencedObject();
            boolean exists = false;
            
            if (currentMappings != null) {
                for (DcMapping mappingCurrent : currentMappings) {
                    DcObject referenceCurrent = mappingCurrent.getReferencedObject();
                    exists = referenceCurrent != null && referenceCurrent.getValue(ExternalReference._EXTERNAL_ID_TYPE).equals(reference.getValue(ExternalReference._EXTERNAL_ID_TYPE));
                    if (exists) {
                        mappingCurrent.setValue(DcMapping._B_REFERENCED_ID, referenceCurrent.getID());
                        getValueDef(DcObject._SYS_EXTERNAL_REFERENCES).setChanged(true);
                        break;
                    }
                }
            }
            
            if (mapping.getReferencedObject() == null) {
                logger.debug("External referenced object was empty. Exteneral reference has not been created for " + this);
            } else if (!exists) {
                DcMapping newMapping = (DcMapping) DcModules.get(DcModules.getMappingModIdx(
                        getModule().getIndex(), mapping.getReferencedObject().getModule().getIndex(), DcObject._SYS_EXTERNAL_REFERENCES)).getItem();
                newMapping.setValue(DcMapping._A_PARENT_ID, getID());
                newMapping.setValue(DcMapping._B_REFERENCED_ID, mapping.getReferencedObject().getID());
                newMapping.setReference(mapping.getReferencedObject());
                currentMappings.add(newMapping);
                getValueDef(DcObject._SYS_EXTERNAL_REFERENCES).setChanged(true);
            } 
        }
    }

    /**
     * Applies the value directly on this item. All checks are bypasses.
     * @param index The field index.
     * @param o The value to be set.
     */
    public void setValueLowLevel(int index, Object o) {
        DcValue value = getValueDef(index);
        if (value != null)
            value.setValueLowLevel(o, getModule().getField(index));
    }

    /**
     * Marks the object as changed.  
     */
    public void markAsChanged() {
        for (DcValue value : values.values())
            value.setChanged(true);

        if (children != null)
            for (DcObject child : children) child.markAsChanged();
    }

    /**
     * Checks whether the object holds unchanged values.
     * @see DcValue#isChanged()
     */
    public boolean isChanged() {
        
        if (isDestroyed()) {
            logger.warn("System tried to check if a value was changed while the object was already destroyed");
            return false;
        }
        
        try {
            for (DcField field : getFields()) {
                boolean changed = getValueDef(field.getIndex()).isChanged();
                if ((!field.isUiOnly() || field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) && changed)
                    return true;
            }
        } catch (Exception e) {
            logger.debug("Item was probably already destroyed!", e);
        }
        return false;
    }

    /**
     * Checks whether the specified field holds a changed value.
     * @see DcValue#isChanged()
     * @param index The field index
     */
    public boolean isChanged(int index) {
        if (isDestroyed()) {
            logger.warn("System tried to check if the value was changed while the object was already destroyed");
            return false;
        }

        return getValueDef(index) != null ? getValueDef(index).isChanged() : false;
    }

    /**
     * Manually mark a field as changed
     * @see DcValue#isChanged()
     * @param index The field index
     * @param b Changed true / false
     */
    public void setChanged(int index, boolean b) {
        if (isDestroyed()) {
            logger.warn("System tried to mark a field as changed while the object was already destroyed");
        } else {
            getValueDef(index).setChanged(b);
        }
    }

    /**
     * Indicates whether the field is enabled.
     * This depends on the field settings which can be altered by the user.
     * @see DcFieldDefinitions
     * @see DcField#isEnabled()
     * @param index
     */
    public boolean isEnabled(int index) {
    	DcField field = getModule().getField(index);
        return field == null ? false : field.isEnabled();
    }

    /**
     * Checks whether the field is marked as required.
     * This depends on the field settings which can be altered by the user.
     * @see DcFieldDefinitions
     * @param index The field index
     */
    public boolean isRequired(int index) {
        return getModule().getField(index).isRequired();
    }

    /**
     * Indicates whether the specified field can be searched on.
     * @param index The field index
     */
    public boolean isSearchable(int index) {
        return getModule().getField(index).isSearchable();
    }

    /**
     * The internal ID
     */
    public String getID() {
        return hasPrimaryKey() ? ((String) getValue(_ID)) : null;
    }

    /**
     * Retrieves all fields on which cannot be searched.
     */
    public Collection<DcField> getNotSearchableFields() {
        Collection<DcField>  notSearchable = new ArrayList<DcField>();
        for (DcField field : getFields()) {
            if (!field.isSearchable())
                notSearchable.add(field);
        }
        return notSearchable;
    }
    
    /**
     * Unloads this items. Its resources are freed and its pictures are unloaded.
     * The item is unusable after this operation (!).
     */
    public void release() {
        getModule().release(this);
    }
    
    /**
     * Retrieves the maximum field / value length.
     * @param index The field index
     */
    public int getMaxFieldLength(int index) {
        return getField(index).getMaximumLength();
    }

    /**
     * Retrieves the value for the specified field.
     * @param index The field index.
     */
    public Object getValue(int index) {
        
        Object value = null;
        
        if (values == null || getValueDef(index) == null)
            return null;
        
        if (isDestroyed()) {
            logger.warn("System tried to retrieve a value while the object was already destroyed");
        } else if (getField(index) == null) {
            logger.warn("Field with index " + index + " does not exist for module " + getModule());
        }
        
        if (index == _SYS_DISPLAYVALUE) {
            value = toString();
        } else if (index == _SYS_MODULE) {
            value = getModule();
        } else {
            value = getValueDef(index).getValue();
        }
        
        return value;
    }

    public String getNormalizedString(int index) {
        String s = getDisplayString(index);
        return StringUtils.normalize2(s);
    }
    
    /**
     * Gets the display value for the specified field.
     * @see DcObject#_SYS_DISPLAYVALUE 
     * @param index The field index
     */
    public String getDisplayString(int index) {
    	
    	if (values.size() == 0)
    		return "";
    	
        if (index == _SYS_DISPLAYVALUE)
            return getValueDef(getSystemDisplayFieldIdx()).getDisplayString(getField(getSystemDisplayFieldIdx()));
        else if (index == _SYS_MODULE)
            return getModule().getObjectNamePlural();
    
        return getValueDef(index) != null ? getValueDef(index).getDisplayString(getField(index)) : "";
    }

    /**
     * Retrieves the field type.
     * @see ComponentFactory
     * @param index The field index.
     */
    public int getFieldType(int index) {
        return getField(index).getFieldType();
    }

    /**
     * Retrieves the database column name.
     * @param index The field index.
     * @return The database field name or null for UI only fields.
     */
    public String getDatabaseFieldName(int index) {
        return getField(index).getDatabaseFieldName();
    }

    /**
     * Applies the enhancers on this item.
     * @see ValueEnhancers 
     * @param update Indicates if the item is new or existing.
     */
    public void applyEnhancers(boolean update) {
        Object value;
        Object newVal;
        Object oldVal;
        for (DcField field : getFields()) {
            value = getValue(field.getIndex());
            for (IValueEnhancer enhancer : field.getValueEnhancers()) {
                if (enhancer.isEnabled() && 
                    (update && enhancer.isRunOnUpdating() || !update && enhancer.isRunOnInsert())) {
                    
                    newVal = enhancer.apply(field, value);
                    oldVal = getValue(field.getIndex());
                    
                    if (newVal != null && (oldVal == null || !newVal.equals(oldVal)))
                        setValue(field.getIndex(), newVal);
                }
            }
        }
    }    
    
    public void beforeDelete() throws ValidationException {
        Collection<DcObject> items = DcConfig.getInstance().getConnector().getReferencingItems(getModuleIdx(), getID());
        if (items.size() > 0) 
            throw new ValidationException(DcResources.getText("msgCannotDeleteDueToReferences", items.toString()));
    }

    /**
     * Indicates if validation should take place when the item is saved.
     */
    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    /**
     * Retrieves the field
     * @param index Field index
     */
    public DcField getField(int index) {
        return getModule().getField(index);
    }

    /**
     * Checks the integrity of the item. 
     * @param update Indicates if the item is new or not.
     * @throws ValidationException
     */
    public void checkIntegrity() throws ValidationException {
        
        if (getParentReferenceFieldIndex() > 0 && getParentID() == null)
            throw new ValidationException("Parent Key cannot be NULL for child records.");
        
        if (!validate) return;
        
        if (DcSettings.getBoolean(DcRepository.Settings.stCheckRequiredFields))
            validateRequiredFields();

        if (DcSettings.getBoolean(DcRepository.Settings.stCheckUniqueness))
            isUnique();
        
        checkForSelfReferencing();
    }
    
    @SuppressWarnings("unchecked")
	private void checkForSelfReferencing() throws ValidationException {

    	DcObject reference;
    	Collection<DcMapping> mappings;
    	
    	boolean isSelfReferencing = false;
    	String fields = "";
    	
    	for (DcField field : getFields()) {
    		if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
    			reference = (DcObject) getValue(field.getIndex());
    			
    			if (reference != null && reference.getID().equals(getID())) {
    				fields += (fields.length() > 0 ? ", " : "") + field.getLabel();
    				isSelfReferencing = true;
    			}
 			
    		}

    		if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
    			mappings = (Collection<DcMapping>) getValue(field.getIndex());
    			
    			if (mappings != null) {
    				for (DcMapping mapping : mappings) {
    					if (mapping.getValue(DcMapping._B_REFERENCED_ID).equals(getID())) {
    	    				fields += (fields.length() > 0 ? ", " : "") + field.getLabel();
    	    				isSelfReferencing = true;
    					}
    				}
    			}
    		}
    	}

    	if (isSelfReferencing)
    		throw new ValidationException(DcResources.getText("msgItemCannotReferenceItself", fields));
    }

    /**
     * Checks if the item is unique.
     * @param o The item to be checked.
     * @param update Indicates if the item is new or not.
     * @throws ValidationException
     */
    public void isUnique() throws ValidationException {
        boolean bUnique = DcConfig.getInstance().getConnector().checkUniqueness(this, !isNew());
        if (!bUnique && validate) {
            
            String fields = "";
            for (DcFieldDefinition definition : getModule().getFieldDefinitions().getDefinitions()) {
                if (definition.isUnique()) {
                    fields += fields.length() > 0 ? ", " : "";
                    fields += getField(definition.getIndex()).getLabel();
                }
            }
                
        	throw new ValidationException(DcResources.getText("msgItemNotUnique", new String[] {toString(), fields}));
        }
    }

    /**
     * Retrieves all field indices.
     */
    public int[] getFieldIndices() {
        return getModule().getFieldIndices();
    }

    public void setIDs() {
        if (hasPrimaryKey()) {
            String ID = getID();
            
            if (CoreUtilities.isEmpty(ID))
                ID = CoreUtilities.getUniqueID();

            if (!ID.equals(getID()))
            	setValue(DcObject._ID, ID);
            
            if (children != null) {
            	
                for (DcObject child : children) {
                    if (child.hasPrimaryKey()) {
                        child.setIDs();
                        
                        if (!ID.equals(child.getValue(child.getParentReferenceFieldIndex())))
                        	child.setValue(child.getParentReferenceFieldIndex(), ID);
                    }
                }
            }
        }
    }

    public DcValue getValueDef(int index) {
        return values == null ? null : values.get(index);
    }

    protected void validateRequiredFields() throws ValidationException {
        
        if (!validate) return;
        
        String s = "";
        for (DcField field : getFields()) {
            
            // if the item is being updated, but this field has not been changed: ignore.
            if (!isChanged(field.getIndex()) && !isNew())
                continue;
            
            if (field.isRequired()) {
                if (!isFilled(field.getIndex())) {
                    if (s.length() > 0) s += ",";
                    s += getLabel(field.getIndex());
                }
            }
        }
        
        if (s.trim().length() > 1)
            throw new ValidationException(DcResources.getText("msgMissingRequiredValues", 
                                          new String[] {s, toString()}));
    }

    /**
     * Merges the values of this and the source item.
     * Only empty values are updated with the values of the source item.  
     * @param dco The source item.
     */
    public void merge(DcObject dco) {
        copy(dco, false, false);
    }
    
    /**
     * Copies all values from the specified Data Crow object.
     * @param overwrite Indicates whether existing values should be overwritten.
     * @param allowDeletes Allows existing values to be cleared.
     * @param dco Source item.
     */
    public void copy(DcObject dco, boolean overwrite, boolean allowDeletes) {
        copy(dco, dco.getFieldIndices(), overwrite, allowDeletes);
        
        for (Attachment a : dco.getNewAttachments())
        	addNewAttachment(a.clone());
        
        for (Picture p : dco.getNewPictures())
        	addNewPicture(p.clone());        
    }
    
    @SuppressWarnings("unchecked")
    public void copy(DcObject dco, int[] fields, boolean overwrite, boolean allowDeletes) {
        for (int i = 0; i < fields.length; i++) {
            int field = fields[i];

            // Do not overwrite when:
            // - the to be copied value is empty and deletes are not allowed
            // - overwriting is not allowed and the current value is not empty
            if (!dco.isFilled(field) && !allowDeletes)
                continue;
            else if (!overwrite && isFilled(field))
                continue;
            
            DcImageIcon oldIcon;
            if (field != _ID) {
            	Object o = dco.getValue(field);
            	if (o instanceof DcImageIcon) {
            		oldIcon = (DcImageIcon) o;
            		DcImageIcon icon = new DcImageIcon(oldIcon.getImage());
            		icon.setFilename(oldIcon.getFilename());
            		setValue(field, icon);
            	} else if (o != null && getField(field).getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
            	    Collection<DcMapping> newMappings = new ArrayList<DcMapping>();
            	    
            	    for (DcObject mapping : (Collection<DcObject>) o)
            	        newMappings.add((DcMapping) mapping.clone());
            	    
            	    setValue(field, newMappings);
            	} else if (o != null && getField(field).getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) { 
            		
            		if (o instanceof DcObject) {
            			setValue(field, ((DcObject) o).clone());
            		} else {
            			// bypass cloning of object in case string is set (ID) - see DcValue - set _DCOBJECTREFERENCE value)
            			setValue(field, o);
            		}
                } else {
                    setValue(field, o);    
                }
            }
        }
    }

    public void applyTemplate() {
        if (getModule().getTemplateModule() != null) {
            DcTemplate template = Templates.getDefault(getModule().getTemplateModule().getIndex());
            if (template != null)
                applyTemplate(template);
        }
    }
    
    public void applyTemplate(DcTemplate template) {
        if (template == null) {
            applyTemplate();
            return;
        } 
        
        int[] fields = getFieldIndices();
        for (int i = 0; i < fields.length; i++) {
            int idx = fields[i];

            // 20121112 - do not allow templates to overwrite existing values
            if (isFilled(idx) || !template.isFilled(idx))
                continue;
            
            Object templateVal = template.getValue(idx); 
            if (    idx != _ID && 
                    idx != _SYS_EXTERNAL_REFERENCES && 
                    templateVal != null) {
                
                setValue(idx, template.getValue(idx));    
            }
        }
    }  
    
    protected Date getCurrentDate() {
    	return new Date();
    }
    
    /**
     * Clones this objects. All values are copies as well as its children.
     * The clone operates on copies of the pictures and the children and can be used entirely
     * in parallel with its clone(s).
     * 
     * Note; this is not a shallow copy and costs just as much resources as its original.
     * After using the clone it is best to discard it by calling the destroy method.
     */
    @Override
    public DcObject clone() {
        DcObject dco = getModule().getItem();
        
        dco.copy(this, true, true);
        
        if (hasPrimaryKey())
        	dco.setValue(DcObject._ID, getID());
        
        // protect field changed flags;
        for (int fieldIdx : getFieldIndices())
        	dco.setChanged(fieldIdx, isChanged(fieldIdx));
        

        if (children != null) {
        	DcObject clone;
            for (DcObject child : children) {
            	clone = child.clone();
                dco.addChild(clone);
            }
        }
        
        // protect the is new flag
        dco.setNew(isNew());
        
        return dco;
    }
    
    @Override
    public int hashCode() {
        return getID() != null ? getID().hashCode() : super.hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
    
    @Override
    public boolean equals(Object o) {
        
        if (o == this) return true;
        
        if (o == null) return false;
        
        boolean equals = false;
        
        if (o instanceof DcObject) {
            String id1 = ((DcObject) o).getID();
            String id2 = getID();
            if ((id1 == null && id2 != null) || (id1 != null && id2 == null) )
                return false;
            if (id1 == null && id2 == null) 
                return false;
            else if (id1 != null)
                return id1.equals(id2);
            else 
                return false;
        }
        
        return equals;
    }
    
    public void afterSave() {
    	for (Picture picture : newPictures) {
    		picture.setObjectID(getID());
    		DcConfig.getInstance().getConnector().savePicture(picture);
    	}
    	
    	for (Attachment attachment : newAttachments ) {
    		attachment.setObjectID(getID());
    		DcConfig.getInstance().getConnector().saveAttachment(attachment);
    	}
    }
    
    public void afterDelete() {}
    
    public void beforeSave() throws ValidationException {
    	
    	if (isNew()) {
    		markAsChanged();
    		applyEnhancers(false);
    	} else {
    	    applyEnhancers(true);
    	}
    	
    	checkIntegrity();
    	
        if (getModule().isFileBacked())
            Hash.getInstance().calculateHash(this);
        
        if ( getField(DcObject._SYS_EXTERNAL_REFERENCES) != null &&
             getExternalReference(DcRepository.ExternalReferences._PDCR) == null &&
            !CoreUtilities.isEmpty(getDisplayString(getSystemDisplayFieldIdx()))) {

            // Only create this ONCE. It is supposed to remain the same for ever
        	addExternalReference(DcRepository.ExternalReferences._PDCR, 
            		getDisplayString(getSystemDisplayFieldIdx()));
        }
        
        for (DcObject child : getChildren())
            child.beforeSave();
        
        saveIcon();
        
        if (isNew()) {
        	setValue(_SYS_CREATED, getCurrentDate());
        	setIDs();
        }

        if (isChanged())
        	setValue(_SYS_MODIFIED, getCurrentDate());
    }
    
    protected void saveIcon() {
        for (DcField field : getFields()) {
        
            if (field.getValueType() != DcRepository.ValueTypes._ICON) continue;
            if (!isChanged(field.getIndex())) continue;
            
            String value = (String) getValue(field.getIndex());
            
            if (value != null && value.length() > 0) {
                byte[] bytes = Base64.decode(value.toCharArray());
                DcImageIcon current = new DcImageIcon(bytes);
                
                if (current.getIconHeight() > 128 || current.getIconWidth() > 128) {
                    BufferedImage img = CoreUtilities.toBufferedImage(current, 128, 128, BufferedImage.TYPE_INT_ARGB);
                    try {
                        bytes = CoreUtilities.getBytes(new DcImageIcon(img), DcImageIcon._TYPE_PNG);
                        setValue(field.getIndex(), new String(Base64.encode(bytes)));
                    } catch (Exception e) {
                        logger.error("Could not save scaled image for object with ID " + getID(), e);
                    }
                }
            }
        }
    }
    
    @Override
    public int compareTo(DcObject o) {
        return toString().compareTo(o.toString());
    }
    
    /**
     * Adds a referenced item to the specified parent object.
     * @param parent The item to which the reference will be added.
     * @param reference The to be referenced item.
     * @param fieldIdx The field holding the reference.
     */
    @SuppressWarnings("unchecked")
    public void addMapping(DcObject reference, int fieldIdx) {
        DcMapping mapping = (DcMapping) DcModules.get(DcModules.getMappingModIdx(getModule().getIndex(), reference.getModule().getIndex(), fieldIdx)).getItem();
        mapping.setValue(DcMapping._A_PARENT_ID, getID());
        mapping.setValue(DcMapping._B_REFERENCED_ID, reference.getID());
        mapping.setReference(reference);
        
        Collection<DcMapping> mappings = (Collection<DcMapping>) getValue(fieldIdx);
        mappings = mappings == null ? new ArrayList<DcMapping>() : mappings;
        
        // check if a mapping exists already
        for (DcMapping m : mappings) {
            if (m.getReferencedID().equals(reference.getID()) || m.toString().equals(reference.toString()))
                return;
        }
        
        mappings.add(mapping);
        setValue(fieldIdx, mappings);
        setChanged(fieldIdx, true);
    }    
    
    /**
     * Creates a reference to the specified object. The provided value can either
     * be a DcObject or a display string. In the latter case the display string will
     * be used to retrieve the DcObject. If no object is found it will be created and
     * saved. The online search is used to retrieve additional information.
     *  
     * @param fieldIdx The field index to set the reference on.
     * @param value The referenced value.
     * @return If an object has been created for the specified value this object will be
     * returned. Else null will be returned.
     */
    public DcObject createReference(int fieldIdx, Object value) {
        String name = value != null ? value instanceof String ? (String) value : value.toString() : null;
        
        if (CoreUtilities.isEmpty(name)) return null;
        
        // method 1: item is provided and exists
        int moduleIdx = DcModules.getReferencedModule(getField(fieldIdx)).getIndex();
        DcModule module = DcModules.get(moduleIdx);
        DcObject ref = value instanceof DcObject ? (DcObject) value : null;
        
        Connector conn = DcConfig.getInstance().getConnector();
        
        // check if we are dealing with an external reference
        if (ref == null && module.getType() == DcModule._TYPE_EXTERNALREFERENCE_MODULE) {
            ref = conn.getItemByDisplayValue(moduleIdx, name);

        } else if (ref == null && module.getType() != DcModule._TYPE_EXTERNALREFERENCE_MODULE) {
            
            // method 2: simple external reference + display value comparison
            ref = conn.getItemByKeyword(moduleIdx, name);
    
            if (ref == null && fieldIdx != DcObject._SYS_EXTERNAL_REFERENCES) {
                ref = module.getItem();
                
                if (module.getType() == DcModule._TYPE_ASSOCIATE_MODULE)
                    ref.setValue(DcAssociate._A_NAME, name);
                else
                    ref.setValue(ref.getSystemDisplayFieldIdx(), name);
                
                ref.setIDs();
            }
        }
        
        if (ref != null) {
            if (getField(fieldIdx).getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION)
                addMapping(ref, fieldIdx);
            else
                setValue(fieldIdx, ref);
        }
        
        return ref;
    }
    
    public void cleanup() {
        children.clear();
        values.clear();
        newAttachments.clear();
        newPictures.clear();
        
        loaded = false;
    }    
}
