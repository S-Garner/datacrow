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

package org.datacrow.core.modules;

import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.console.UIComponents;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcTemplate;
import org.datacrow.core.utilities.definitions.DcFieldDefinition;
import org.datacrow.core.utilities.definitions.DcFieldDefinitions;

/**
 * The template module represents templates. Templates can be applied when creating new
 * items. The template module is based on the module which it is serving.
 *  
 * @author Robert Jan van der Waals
 */
public class TemplateModule extends DcModule {
    
	private static final long serialVersionUID = 1L;

    private final int templatedModIdx;
    
    /**
     * Creates a new instance based on the specified module. The fields of the provided module
     * are added to this module.
     * @param parent
     */
    public TemplateModule(DcModule parent) {
        super(parent.getIndex() + DcModules._TEMPLATE, 
              false, 
              "Template", 
              "",
              "Template",
              "Templates",
              parent.getTableName() + "_template", 
              parent.getTableShortName() + "temp");
        
        this.templatedModIdx = parent.getIndex();
        
        for (DcField field : parent.getFields()) {
            addField(new DcField(field.getIndex(), getIndex(), field.getLabel(), field.isUiOnly(),
                                 field.isEnabled(), field.isReadOnly(), field.isSearchable(),
                                 field.getMaximumLength(), field.getFieldType(), field.getSourceModuleIdx(), field.getValueType(),
                                 field.getDatabaseFieldName()));
        }
    }

    @Override
    public boolean hasInsertView() {
        return false;
    }

    @Override
    public boolean hasSearchView() {
        return false;
    }
    
    /**
     * Retrieves the module this template module has been created for.
     */
    public DcModule getTemplatedModule() {
        return DcModules.get(templatedModIdx);
    }

    @Override
    public DcImageIcon getIcon16() {
        return IconLibrary._icoTemplate;
    }

    @Override
    public DcImageIcon getIcon32() {
        return IconLibrary._icoTemplate;
    }        
    
    /**
     * Creates a new template item.
     * @see DcTemplate
     */
    @Override
    public DcObject createItem() {
        return new DcTemplate(getIndex(), templatedModIdx);
    }    
    
    /**
     * The field settings/definitions.
     * @see DcModule#getFieldDefinitions()
     */    
    @Override
    public DcFieldDefinitions getFieldDefinitions() {

    	DcModule parent = getTemplatedModule();
    	
        if (parent == null || parent.getFieldDefinitions() == null)
            return null;
        
        DcFieldDefinitions fds = parent.getFieldDefinitions();
        DcFieldDefinitions definitions = new DcFieldDefinitions(getIndex());
        
        definitions.add(new DcFieldDefinition(getIndex(), DcTemplate._SYS_TEMPLATENAME, null, true, true, true, true, "lblTemplateProperties"));
        definitions.add(new DcFieldDefinition(getIndex(), DcTemplate._SYS_DEFAULT, null, true, true, true, false, "lblTemplateProperties"));
        
        for (DcFieldDefinition fd : fds.getDefinitions())
            definitions.add(new DcFieldDefinition(getIndex(), fd.getIndex(), fd.getLabel(), fd.isEnabled(), false, false, false, fd.getTab(parent.getIndex())));
        
        return definitions;
    }      

    /**
     * Initializes the standard fields.
     */
    @Override
    protected void initializeFields() {
        super.initializeFields();
        addField(new DcField(DcTemplate._SYS_TEMPLATENAME, getIndex(), "Template Name",
                             false, true, false, true,
                             255, UIComponents._SHORTTEXTFIELD, getIndex(), DcRepository.ValueTypes._STRING,
                             "TemplateName"));
        addField(new DcField(DcTemplate._SYS_DEFAULT, getIndex(), "Default",
                             false, true, false, true, 
                             255, UIComponents._CHECKBOX, getIndex(), DcRepository.ValueTypes._BOOLEAN,
                             "DefaultTemplate"));        
    }
    
    @Override
    public boolean isHasReferences() {
        return true;
    }    
    
    @Override
    public int[] getSupportedViews() {
        return new int[] {};
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof TemplateModule ? ((TemplateModule) o).getIndex() == getIndex() : false);
    }     
}