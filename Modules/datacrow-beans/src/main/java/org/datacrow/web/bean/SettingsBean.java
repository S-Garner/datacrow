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

package org.datacrow.web.bean;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.datacrow.core.DcRepository;
import org.datacrow.core.console.UIComponents;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.settings.Settings;
import org.datacrow.web.DcBean;
import org.datacrow.web.util.WebUtilities;
import org.primefaces.model.DualListModel;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class SettingsBean extends DcBean {

    private Settings settings;
    private DcModule module;
    
    private DualListModel<DcField> overviewFields;
    private DualListModel<DcField> itemFormFields;
    
    public SettingsBean() {
        try {
            ModulesBean modulesBean = (ModulesBean) WebUtilities.getBean("modulesBean");
            module = DcModules.get(modulesBean.getSelectedModuleIdx());
            settings = module.getSettings();
        } catch (Exception e) {
            WebUtilities.log(Level.ERROR, e);
        }
    }
    
    public void setOverviewFields(DualListModel<DcField> overviewFields) {
        this.overviewFields = overviewFields;
    }
    
    public void setItemFormFields(DualListModel<DcField> itemFormFields) {
        this.itemFormFields = itemFormFields;
    }
    
    public DualListModel<DcField> getItemFormFields() {
        List<DcField> source = new ArrayList<DcField>();
        List<DcField> target = new ArrayList<DcField>();
        
        if (settings != null) {
            for (int fieldIdx : settings.getIntArray(DcRepository.ModuleSettings.stWebItemFormFields)) {
                
                if (module.getField(fieldIdx) != null)
                    target.add(module.getField(fieldIdx));
            }
        }
        
        for (DcField field : module.getFields()) {
            
            if (!isAuthorized(field) ||
                !field.isEnabled() || 
                (field.isReadOnly() && !field.isUiOnly()) ||  
                 field.isLoanField()) {
                continue;
            }
            
            source.add(field);
        }
        
        source.removeAll(target);
        
        itemFormFields = new DualListModel<DcField>(source, target);
        return itemFormFields;
    }
    
    public DualListModel<DcField> getOverviewFields() {
        List<DcField> source = new ArrayList<DcField>();
        List<DcField> target = new ArrayList<DcField>();
        
        if (settings != null) {
            for (int fieldIdx : settings.getIntArray(DcRepository.ModuleSettings.stWebOverviewFields)) {
                if (module.getField(fieldIdx) != null)
                    target.add(module.getField(fieldIdx));
            }
        }
        
        for (DcField field : module.getFields()) {
            
            if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
               !isAuthorized(field) ||
               !field.isEnabled() || 
                (field.isReadOnly() && !field.isUiOnly()) ||  
                field.isLoanField() ||
                field.getFieldType() == UIComponents._FILEFIELD ||
                field.getFieldType() == UIComponents._FILELAUNCHFIELD) {
                continue;
            }
            
            source.add(field);
        }
        
        source.removeAll(target);
        
        overviewFields = new DualListModel<DcField>(source, target);
        
        return overviewFields;
    }
    
    public String saveItemFormFields() {
        List<DcField> f =  this.itemFormFields.getTarget();
        int[] fields = new int[f.size()]; 
        int idx = 0;
        for (Object o : f) {
            fields[idx++] = Integer.parseInt(o.toString());
        }
        
        settings.set(DcRepository.ModuleSettings.stWebItemFormFields, fields);
        settings.save();
        
        return "/index";
    }
    
    public String saveOverviewFields() {
        List<DcField> f =  this.overviewFields.getTarget();
        int[] overviewFields = new int[f.size()]; 
        int idx = 0;
        for (Object o : f) {
            overviewFields[idx++] = Integer.parseInt(o.toString());
        }
        
        settings.set(DcRepository.ModuleSettings.stWebOverviewFields, overviewFields);
        settings.save();
        
        try {
            ItemsBean itemsBean = (ItemsBean) WebUtilities.getBean("itemsBean");
            itemsBean.search();
        } catch (Exception e) {
            WebUtilities.log(Level.ERROR, e);
        }
        
        return "/index";
    }
}
