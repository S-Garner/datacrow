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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.console.UIComponents;
import org.datacrow.core.enhancers.IValueEnhancer;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.modules.xml.XmlField;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.utilities.definitions.DcFieldDefinition;
import org.datacrow.core.utilities.definitions.DcFieldDefinitions;

/**
 * Fields are part of a Data Crow module. A field defines how it is represented in the
 * UI, which label is used to describe it, if it can be searched on, if it is editable, 
 * the maximum length of its content and so on.
 * 
 * @author Robert Jan van der Waals
 */
public class DcField implements Serializable {

	private static final long serialVersionUID = 1L;
    
    private final String systemName;
    private final String label;
    private final boolean searchable;
    private final boolean readonly;
    private final boolean uiOnly;
    
    private final int fieldType;
    private final int valueType;
    private final int index;
    private final int module;
    private final int sourceModuleIdx;
    private final String databaseFieldName;
    
    private final Collection<IValueEnhancer> enhancers = new ArrayList<IValueEnhancer>();

    private int maximumLength;
    private String resourceKey;
    
    private boolean enabled = true;
    
    /**
     * Creates a new field based on a XML definition.
     * @param field XML definition.
     * @param module The module index to which this field belongs.
     */
    public DcField(XmlField field, int module) {
        this(field.getIndex(), module, field.getName(), field.isUiOnly(), field.isEnabled(),
             field.isReadonly(), field.isSearchable(), field.getMaximumLength(),
             field.getFieldType(), field.getModuleReference(), field.getValueType(), field.getColumn());
    }
    
    /**
     * Creates a new field.
     * @param index The unique field index.
     * @param module The module to which this field belongs.
     * @param label The display label.
     * @param uiOnly Indicates if this field is represented by a database column.
     * @param enabled Indicates if the field will be used. Can be overridden by the user.
     * @param readonly Indicates if the field can be edited.
     * @param searchable Tells if the user can search on this field.
     * @param techinfo Holds technical information?
     * @param maximumLength The maximum value length.
     * @param fieldType The (component) field type.
     * @param modRef The module reference.
     * @param valueType The value type {@link DcRepository.ValueTypes}
     * @param databaseFieldName The database column name.
     */
    public DcField( int index,
                    int module,
                    String label,
                    boolean uiOnly,
                    boolean enabled,
                    boolean readonly,
                    boolean searchable,
                    int maximumLength,
                    int fieldType,
                    int sourceModuleIdx,
                    int valueType,
                    String databaseFieldName) {

        this.enabled = enabled;
        this.index = index;
        this.module = module;
        this.uiOnly = uiOnly;
        this.databaseFieldName = databaseFieldName;
        this.sourceModuleIdx = sourceModuleIdx;
        this.label = label;
        this.systemName = label;
        this.readonly = readonly;
        this.searchable = searchable;
        this.fieldType = fieldType;
        this.valueType = valueType;
        this.maximumLength = maximumLength;
    }
    
    public boolean isLoanField() {
        return
            getIndex() == DcObject._SYS_AVAILABLE ||
            getIndex() == DcObject._SYS_LENDBY ||
            getIndex() == DcObject._SYS_LOANSTATUS ||
            getIndex() == DcObject._SYS_LOANSTATUSDAYS ||
            getIndex() == DcObject._SYS_LOANENDDATE ||
            getIndex() == DcObject._SYS_LOANSTARTDATE ||
            getIndex() == DcObject._SYS_LOANDUEDATE ||
            getIndex() == DcObject._SYS_LOANALLOWED ||
            getIndex() == DcObject._SYS_LOANDURATION;
    }
    
    /**
     * The source module index.
     */
    public int getSourceModuleIdx() {
        return sourceModuleIdx;
    }

    /**
     * The module reference index.
     */
    public int getReferenceIdx() {
        try {
            return DcModules.getReferencedModule(this).getIndex();
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * The unique field index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * The module to which this field belongs.
     */
    public int getModule() {
        return module;
    }

    /**
     * When a field is marked as UI only its value will not be stored in the database.
     */
    public boolean isUiOnly() {
        return uiOnly;
    }
    
    public boolean isSystemField() {
        return getSystemName().endsWith("_persist") || getIndex() == DcObject._ID;
    }

    /**
     * Indicates if the field is enabled. Depends on both the settings and the permissions
     * of the user.
     */
    public boolean isEnabled() {
        
        if (DcModules.get(getModule()) == null ||
            DcModules.get(getModule()).getSettings() == null)
            return false;
        
        boolean authorized = 
                DcConfig.getInstance().getConnector() == null ||
                DcConfig.getInstance().getConnector().getUser() == null ||
                DcConfig.getInstance().getConnector().getUser().isAuthorized(this);
        
        if (!authorized) {
            return false;
        } else {
            DcFieldDefinitions definitions = (DcFieldDefinitions) 
                DcModules.get(getModule()).getSettings().getDefinitions(DcRepository.ModuleSettings.stFieldDefinitions);

            if (definitions != null) {
                for (DcFieldDefinition definition : definitions.getDefinitions()) {
                    if (definition.getIndex() == getIndex()) 
                        return definition.isEnabled();
                }
            }
        }
        
        // if the field definitions do not specify anything, the system default applies 
        return enabled;
    }

    /**
     * Set the database column name.
     */
    public String getDatabaseFieldName() {
        return databaseFieldName;
    }

    /**
     * The component type.
     */
    public int getFieldType() {
        return fieldType;
    }

    /**
     * The value type.
     * @see DcRepository.ValueTypes
     */
    public int getValueType() {
        return valueType;
    }

    public String getOriginalLabel() {
        return label;
    }
    
    /**
     * The display label. 
     * - If the field definitions (field settings) have a custom label defined this value will be used.
     * - If not the field label will be retrieved using the resources.
     * - Else the original label will be used.
     */
    public String getLabel() {
        String s = null;
        
        if (DcModules.get(module) != null) {
            DcFieldDefinitions definitions = DcModules.get(module).getFieldDefinitions();
            if (definitions != null && definitions.get(getIndex()) != null)
                s = definitions.get(getIndex()).getLabel();
        }
        
        if (s != null && s.trim().length() > 0)
            return s;

        if (DcModules.get(module) != null &&
            DcResources.getText(getResourceKey()) != null &&
            DcResources.getText(getResourceKey()).length() > 0)
            
            return DcResources.getText(getResourceKey());

        return label;    
    }
    
    public DcFieldDefinition getDefinition() {
        return DcModules.get(module).getFieldDefinitions().get(getIndex());
    }
    
    /**
     * The key used for setting the value in the resources.
     */
    public String getResourceKey() {
        resourceKey = resourceKey == null ?
                      DcModules.get(module).getModuleResourceKey() + "Field" + getDatabaseFieldName() : resourceKey;

        return resourceKey;
    }

    /**
     * The system name of this field.
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * Mark the field as required.
     */
    public boolean isRequired() {
        if (DcConfig.getInstance().getConnector() == null ||
            DcConfig.getInstance().getConnector().getUser() == null ||
            DcConfig.getInstance().getConnector().getUser().isAuthorized(this)) {
            
            if (DcModules.get(getModule()) == null ||
                DcModules.get(getModule()).getSettings() == null)
                return false;
            
            DcFieldDefinitions definitions = (DcFieldDefinitions) 
                DcModules.get(getModule()).getSettings().getDefinitions(DcRepository.ModuleSettings.stFieldDefinitions);
            
            if (definitions != null) {
                for (DcFieldDefinition definition : definitions.getDefinitions()) {
                    if (definition.getIndex() == getIndex()) 
                        return definition.isRequired();
                }
            }
        }
        return false;
    }

    /**
     * Indicates if the value belonging to this field can be edited.
     * Depends on both the settings and the permissions of the user.
     */
    public boolean isReadOnly() {
        if (    DcConfig.getInstance().getConnector() != null && 
                DcConfig.getInstance().getConnector().getUser() != null &&
                !DcConfig.getInstance().getConnector().getUser().isEditingAllowed(this))
            return true;

        return readonly;
    }

    /**
     * Indicates if the user can search on this field.
     * @return
     */
    public boolean isSearchable() {
        return searchable;
    }

    /**
     * Returns the maximum field length (characters positions).
     * In case the field is of type long text field the maximum value will be
     * the maximum integer (Integer.MAX_VALUE) value (maximum field setting is thus overruled).
     */
    public int getMaximumLength() {
    	if (maximumLength == 0 && getValueType() == DcRepository.ValueTypes._STRING) 
    	    maximumLength = getFieldType() == UIComponents._LONGTEXTFIELD ? 0 : 255;
    	
    	return maximumLength;
    }

    @Override
    public String toString() {
        return getLabel();
    }
    
    /**
     * Remove all the registered value enhancers.
     */
    public void removeEnhancers() {
        enhancers.clear();
    }
    
    /**
     * Register a new value enhancer.
     * @param enhancer
     */
    public void addValueEnhancer(IValueEnhancer enhancer) {
        enhancers.add(enhancer);
    }
    
    /**
     * Retrieves all the registered value enhancers.
     * @return
     */
    public IValueEnhancer[] getValueEnhancers() {
        return enhancers.toArray(new IValueEnhancer[0]);
    }
    
    /**
     * Calculates the database field type definition.
     */
    public String getDataBaseFieldType() {
        String s = "";
        
        if (getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE ||
            getValueType() == DcRepository.ValueTypes._DCPARENTREFERENCE ||
            getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
            
            s = DcRepository.Database._FIELDSTRING + "(36)";
            
        } else if (getValueType() == DcRepository.ValueTypes._STRING) {
            if (getFieldType() == UIComponents._LONGTEXTFIELD)
                s = DcRepository.Database._FIELDOBJECT;
            else
                s = DcRepository.Database._FIELDSTRING + "(" + getMaximumLength() + ")";
        } else if (getValueType() == DcRepository.ValueTypes._DOUBLE) {
            s = DcRepository.Database._FIELDNUMERIC + "(10, 2)";
        } else if (getValueType() == DcRepository.ValueTypes._BIGINTEGER ||
                   getValueType() == DcRepository.ValueTypes._LONG) {
            s = DcRepository.Database._FIELDBIGINT;
        } else if (getValueType() == DcRepository.ValueTypes._ICON ||
                   getValueType() == DcRepository.ValueTypes._BLOB) {
            s = DcRepository.Database._FIELDOBJECT;
        } else if (getValueType() == DcRepository.ValueTypes._BOOLEAN) {
            s = DcRepository.Database._FIELDBOOLEAN;
        } else if (getValueType() == DcRepository.ValueTypes._DATE) {
            s = DcRepository.Database._FIELDDATE;
        } else if (getValueType() == DcRepository.ValueTypes._DATETIME) {
            s = DcRepository.Database._FIELDDATE;
        }
        return s;
    }
}
