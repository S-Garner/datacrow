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
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.datacrow.core.DcRepository;
import org.datacrow.core.console.UIComponents;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcAssociate;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcProperty;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.web.DcBean;
import org.datacrow.web.ReferencesCache;
import org.datacrow.web.converter.ReferenceConverter;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;
import org.primefaces.model.tagcloud.TagCloudModel;

import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

/**
 * A light weight version of the DcField. The heavy weight version is also present
 * in memory, on the server. 
 */
public class Field extends DcBean {

    private static Logger logger = DcLogManager.getLogger(Field.class.getName());

    public static final int _CHECKBOX = 0;
    public static final int _TEXTFIELD = 1;
    public static final int _LONGFIELD = 2;
    public static final int _DROPDOWN = 3;
    public static final int _IMAGE = 4;
    public static final int _URLFIELD = 5;
    public static final int _MULTIRELATE = 6;
    public static final int _DATE = 7;
    public static final int _FILE = 8;
    public static final int _TAGFIELD = 9;
    public static final int _RATING = 10;
    public static final int _ICON = 11;
    public static final int _NUMBER = 12;
    public static final int _DOUBLE = 13;
    public static final int _DURATION = 14;
    
    private int index;
    private int module;
    private int referencedModIdx;
    private int type;
    private int maxTextLength;
    
    private boolean enableDownload = false;
    private boolean changed = false;
    
    private String label;
    private String width;
    private Object value;
    
    private File file;
    
    private boolean isLinkToDetails;
    private boolean readonly;
    private boolean required;
    
    private ReferenceConverter converter;
    
    private String systemName;
    
    public Field(DcField field) {
        
        this.index = field.getIndex();
        this.label = field.getLabel();
        this.module = field.getModule();
        this.required = field.isRequired();
        this.readonly = field.isReadOnly();
        
        if (!readonly)
            this.readonly = !isEditingAllowed(field);
        
        this.systemName = field.getDatabaseFieldName();
        this.referencedModIdx = DcModules.getReferencedModule(getDcField()).getIndex();
        
        if (field.getIndex() == DcObject._SYS_EXTERNAL_REFERENCES)
            this.readonly = true;
        
        DcModule m = DcModules.get(module);
        
        if (m.getType() == DcModule._TYPE_PROPERTY_MODULE &&
            field.getIndex() == DcProperty._A_NAME)
            this.required = true;

        if (m.getType() == DcModule._TYPE_ASSOCIATE_MODULE &&
            field.getIndex() == DcAssociate._A_NAME)
            this.required = true;
        
        if (getDcField().getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
            type = _DROPDOWN;
        } else  if (getDcField().getFieldType() == UIComponents._RATINGCOMBOBOX) {
            type = _RATING;
        } else  if (getDcField().getFieldType() == UIComponents._TIMEFIELD) {
            type = _DURATION;            
        } else if (getDcField().getValueType() == DcRepository.ValueTypes._BIGINTEGER ||
                   getDcField().getValueType() == DcRepository.ValueTypes._LONG ||
                   getDcField().getFieldType() == UIComponents._NUMBERFIELD) {
            type = _NUMBER;
        } else if (getDcField().getValueType() == DcRepository.ValueTypes._DOUBLE) {
            type = _DOUBLE;    
        } else if (getDcField().getValueType() == DcRepository.ValueTypes._PICTURE) {
            type = _IMAGE;
        } else if (getDcField().getValueType() == DcRepository.ValueTypes._ICON) {
            type = _ICON;            
        } else if (getDcField().getValueType() == DcRepository.ValueTypes._BOOLEAN) {
            type = _CHECKBOX;
        } else if (getDcField().getFieldType() == UIComponents._LONGTEXTFIELD) {
            type = _LONGFIELD;
        } else if (getDcField().getFieldType() == UIComponents._URLFIELD) {
            type = _URLFIELD;
        } else if (getDcField().getFieldType() == UIComponents._TAGFIELD) {
            type = _TAGFIELD;
        } else if (getDcField().getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
            type = _MULTIRELATE;
        } else if ((getDcField().getValueType() == DcRepository.ValueTypes._DATE ||
                    getDcField().getValueType() == DcRepository.ValueTypes._DATETIME) &&
                   getDcField().getIndex() != DcObject._SYS_LOANDUEDATE) {
            type = _DATE;
        } else if (getDcField().getFieldType() == UIComponents._FILEFIELD ||
                   getDcField().getFieldType() == UIComponents._FILELAUNCHFIELD) {
            type = _FILE;
        } else {
            type = _TEXTFIELD;
        } 
    }
    
    private void setFileInformation() {
        
        if (CoreUtilities.isEmpty(value)) return;
        
        file = new File(value.toString());
        
        if (!file.exists()) return;
        
        enableDownload = true;
    }
    
    public List<Reference> getMatchingReferences(String query) {
        List<Reference> all = ReferencesCache.getInstance().getReferences(getReferenceModuleIdx());
        List<Reference> result = new ArrayList<Reference>();
        
        boolean fullMatch = false;
        for (Reference ref : all) {
            if (ref.getLabel().toLowerCase().startsWith(query.toLowerCase()))
                result.add(ref);
            
            if (ref.getLabel().toLowerCase().equals(query))
                fullMatch = true;
        }
        
        if (!fullMatch) {
            Reference reference = new Reference(query, CoreUtilities.getUniqueID(), DcModules._TAG);
            reference.setExists(false);
            result.add(0, reference);
            
            if (converter != null)
                converter.addReference(reference);
        }
        
        return result;
    }
    
    public List<Reference> getAllReferences() {
        List<Reference> references = new ArrayList<>();
        references.addAll(ReferencesCache.getInstance().getReferences(getReferenceModuleIdx()));
        return references;
    }
    
    @SuppressWarnings("unchecked")
    public TagCloudModel getTagModel() {
        TagCloudModel model = new DefaultTagCloudModel();
        
        if (getValue() instanceof Collection) {
            for (Reference ref : (Collection<Reference>) getValue()) {
                model.addTag(ref);
            }
        }
        return model;
    }

    public boolean isRequired() {
        return required;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMaxTextLength() {
        return maxTextLength;
    }

    public void setMaxTextLength(int maxTextLength) {
        this.maxTextLength = maxTextLength;
    }

    public int getType() {
        return type;
    }
    
    public int getReferenceModuleIdx() {
        return referencedModIdx;
    }

    public void setWidth(int width) {
        this.width = String.valueOf(width);
    }

    public boolean isLinkToDetails() {
        return isLinkToDetails;
    }

    public void setLinkToDetails(boolean isLinkToDetails) {
        this.isLinkToDetails = isLinkToDetails;
    }

    public Converter<?> getConverter() {
        if (converter == null)
            converter = new ReferenceConverter(getReferenceModuleIdx());
        
        return converter;
    }
    
    public boolean isEnableDownload() {
        return enableDownload;
    }
    
    public StreamedContent getStreamedContent() {
        DefaultStreamedContent dsc = null;
        try {
            @SuppressWarnings("resource")
            FileInputStream fis = new FileInputStream(file);
            String mimeType = FacesContext.getCurrentInstance().getExternalContext().getMimeType(value.toString());
            dsc = DefaultStreamedContent.builder().contentType(mimeType).name(file.getName()).stream(() -> fis).build();
        } catch (Exception e) {
            logger.error("Could not initialize streamed content for file download", e);
        }
        return dsc;
    }

    public void delete() {
        if (value instanceof Picture) {
            ((Picture) value).setDeleted(true);
        } else {
            value = null;
        }
        
        setChanged(true);
    }
    
    /**
     * Retrieves the value for this field. This is only used for input forms.
     */
    public Object getValue() {
        return value;
    }
    
    /**
     * Retrieves the value for this field. This is only used for input forms.
     */
    public Object getDisplayValue() {
        if (value instanceof String) {
            return (String) value;
        } else if (type == _DURATION && value instanceof Date) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            return formatter.format((Date) value);
        } else if (type == _DATE && value instanceof Date) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            return formatter.format((Date) value);
        }
        return value == null ? "" : value.toString();
    }
    
    protected void setValueLowLevel(Object value) {
        this.value = value;
        
        if (getType() == _FILE)
            setFileInformation();
    }

    public void setValue(Object value) {
        
        if (CoreUtilities.isEmpty(this.value) && CoreUtilities.isEmpty(value))
            return;
    	
        if (type == _DURATION && value instanceof Date) {
            if (!value.equals(this.value)) {
                changed = true;
                this.value = value;
            }
        } else if (type == _TAGFIELD) {
            @SuppressWarnings("unchecked")
            List<Reference> tags = (List<Reference>) value;
            List<Reference> newTags = new ArrayList<Reference>();

            if (tags != null)
                newTags.addAll(tags);
            
            @SuppressWarnings("unchecked")
            List<Reference> currentTags = (List<Reference>) this.value;
            if (newTags.size() != currentTags.size()) {
                changed = true;
            } else {
                
                for (Reference ct : currentTags) {
                    boolean equals = false;
                    for (Reference nt : newTags) {
                        equals |= ct.equals(nt);
                    }
                    changed |= equals;
                }
            } 
            
            this.value = newTags;
            
        } else if (type == _MULTIRELATE) {
            Object[] references = (Object[]) value;
            List<Reference> newRefs = new ArrayList<Reference>();
            for (Object r : references) {
                newRefs.add((Reference) r);
            }
            
            @SuppressWarnings("unchecked")
            List<Reference> currentRefs = (List<Reference>) this.value;
            currentRefs = currentRefs == null ? new ArrayList<Reference>() : currentRefs;
            
            if (newRefs.size() != currentRefs.size()) {
                changed = true;
            } else {
                
                for (Reference cr : currentRefs) {
                    boolean equals = false;
                    for (Reference nr : newRefs) {
                        equals |= cr.equals(nr);
                    }
                    changed |= equals;
                }
            } 
            
            this.value = newRefs;
        } else if ((CoreUtilities.isEmpty(this.value) && !CoreUtilities.isEmpty(this.value)) ||
        	(!CoreUtilities.isEmpty(this.value) && CoreUtilities.isEmpty(this.value)) ||
        	(value == null && this.value != null) ||
        	!value.equals(this.value)) {
            
			if (!readonly)
				this.changed = true;
			
			this.value = value;
		}
        
        if (getType() == _FILE)
            setFileInformation();
    }
    
    /** 
     * Indicates whether navigation is allowed. This is only applicable for reference fields.
     * If set to false, the user is not allowed to navigate to the underlying item.
     */
    public boolean isNavigationAllowed() {
        return  isAuthorized(DcModules.get(referencedModIdx)) && 
                index != DcObject._SYS_EXTERNAL_REFERENCES;
    }
    
    public boolean isNavigationEditAllowed() {
        return isEditingAllowed(DcModules.get(referencedModIdx));
    }

    
    public DcModule getDcModule() {
        return DcModules.get(module);
    }
    
    public DcField getDcField() {
        return getDcModule().getField(index);
    }
    
    public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

    public boolean isDropDown() {
        return type == _DROPDOWN;
    }
    
    public boolean isIcon() {
        return type == _ICON;
    }    
    
    public boolean isImage() {
        return type == _IMAGE;
    }

    public boolean isCheckbox() {
        return type == _CHECKBOX;
    }

    public boolean isTagField() {
        return type == _TAGFIELD;
    }

    public boolean isRating() {
        return type == _RATING;
    }
    
    public boolean isUrl() {
        return type == _URLFIELD;
    }
    
    public boolean isFile() {
        return type == _FILE;
    }    
    
    public boolean isTextfield() {
        return type == _TEXTFIELD;
    }

    public boolean isLongTextfield() {
        return type == _LONGFIELD;
    }

    public boolean isMultiRelate() {
        return type == _MULTIRELATE;
    }

    public boolean isDate() {
        return type == _DATE;
    }
    
    public boolean isNumber() {
        return type == _NUMBER;
    }
    
    public boolean isDoubleValue() {
        return type == _DOUBLE;
    }
    
    public boolean isDuration() {
        return type == _DURATION;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public String getLabel() {
        return label;
    }

    public String getWidth() {
        return width;
    }

    public int getIndex() {
        return index;
    }
    
    public String getSystemName() {
        return systemName;
    }
    
    public boolean isPictureAlive() {
        return value != null && value instanceof Picture && ((Picture) value).isAlive();
    }
}
