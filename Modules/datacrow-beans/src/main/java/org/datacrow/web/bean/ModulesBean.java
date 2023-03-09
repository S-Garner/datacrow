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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.modules.DcPropertyModule;
import org.datacrow.core.objects.DcField;
import org.datacrow.web.DcBean;
import org.datacrow.web.model.Module;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@Named
@SessionScoped
public class ModulesBean extends DcBean implements Serializable {
    
    private Collection<Module> modules = new ArrayList<Module>();
    private Collection<Module> topModules = new ArrayList<Module>();
    
    private int selectedModuleIdx = -1;
    private int selectedTopModuleIdx = -1;
    
    private Module selectedModule;
    
    public ModulesBean() {}
    
    @PostConstruct
    public void load() {
        selectedTopModuleIdx = -1;
        selectedModuleIdx = -1;
        
        selectedModule = null;
        
        modules.clear();
        topModules.clear();
        
        Module topModule;
        for (DcModule module : DcModules.getModules()) {
            if (    isAuthorized(module) &&
                    module.isTopModule() && 
                    module.isEnabled() && 
                   !module.isAbstract() &&
                  (!module.hasDependingModules() || 
                    module.getIndex() == DcModules._CONTACTPERSON ||
                    module.getIndex() == DcModules._CONTAINER) &&
                    module.getIndex() != DcModules._USER) {
                
                topModule = new Module(module, Module._TYPE_TOP_MODULE);
                
                for (DcField field : module.getFields()) {
                    
                    if (isAuthorized(field)) {
                        DcModule referencedMod = DcModules.getReferencedModule(field);
                        if (    isAuthorized(referencedMod) &&
                                referencedMod.getType() != DcModule._TYPE_EXTERNALREFERENCE_MODULE) {
                            
                            if (    referencedMod.isEnabled() &&
                                    referencedMod.getIndex() != module.getIndex() && 
                                  !(referencedMod instanceof DcPropertyModule) &&
                                    referencedMod.getIndex() != DcModules._CONTACTPERSON &&
                                    referencedMod.getIndex() != DcModules._CONTAINER) {
                                Module sm = new Module(referencedMod, Module._TYPE_SUB_MODULE);
                                modules.add(sm);
                                topModule.addSubModule(sm);
                            } else if (referencedMod instanceof DcPropertyModule) {
                                Module pm = new Module(referencedMod, Module._TYPE_PROPERTY_MODULE);
                                modules.add(pm);
                                topModule.addPropertyModule(pm);
                            }
                        }
                    }
                }
                
                DcModule templateMod = module.getTemplateModule();
                if (templateMod != null) {
                    Module tm = new Module(templateMod, Module._TYPE_TEMPLATE_MODULE);
                    modules.add(tm);
                    topModule.addTemplateModule(tm);
                }
                
                DcModule child = module.getChild();
                if (child != null) {
                    templateMod = child.getTemplateModule();
                    if (templateMod != null) {
                        Module tm = new Module(templateMod, Module._TYPE_TEMPLATE_MODULE);
                        modules.add(tm);
                        topModule.addTemplateModule(tm);
                    }
                    
                    // add references to property modules for the child
                    int sourceIdx = 0;
                    for (DcPropertyModule pm : DcModules.getPropertyModules(child)) {
                        sourceIdx = pm.isServingMultipleModules() ? pm.getIndex() : pm.getIndex() - child.getIndex();
                        if (!module.hasReferenceTo(sourceIdx) && !pm.isServingMultipleModules()) {
                            Module m = new Module(pm, Module._TYPE_PROPERTY_MODULE);
                            modules.add(m);
                            topModule.addPropertyModule(m);
                        }
                    }
                }
                
                modules.add(topModule);
                topModules.add(topModule);
                
                if (selectedModuleIdx == -1)
                    setSelectedModuleIdx(topModule.getIndex());
            }
        }
    }
    
    public Module getModule(int idx) {
        Module result = null;
        for (Module module : modules) {
            if (module.getIndex() == idx)
                result = module;
            
            if (module.isCanHaveChildren() && module.getChildModule().getIndex() == idx)
                result = module.getChildModule();
            
            if (result != null) break;
        }
        return result;
    }
    
    public Collection<Module> getTemplateModules() {
        for (Module module : modules) {
            if (module.getIndex() == selectedTopModuleIdx)
                return module.getTemplateModules();
        }
        return new ArrayList<Module>();
    }
    
    public Collection<Module> getPropertyModules() {
        for (Module module : modules) {
            if (module.getIndex() == selectedTopModuleIdx)
                return module.getPropertyModules();
        }
        return new ArrayList<Module>();
    }
    
    public Collection<Module> getSubModules() {
        for (Module module : modules) {
            if (module.getIndex() == selectedTopModuleIdx)
                return module.getSubModules();
        }
        return new ArrayList<Module>();
    }
    
    public boolean isHasSubModules() {
        for (Module module : modules) {
            if (module.getIndex() == selectedTopModuleIdx)
                return module.getSubModules().size() > 0;
        }
        return false;
    }
    
    public boolean isHasPropertyModules() {
        for (Module module : modules) {
            if (module.getIndex() == selectedTopModuleIdx)
                return module.getPropertyModules().size() > 0;
        }
        return false;
    }
    
    public Module getSelectedModule() {
        return selectedModule;
    }
    
    public void setSelectedModuleIdx(int moduleIdx) {
        for (Module module : modules) {
            if (module.getIndex() == moduleIdx) {
                selectedModule = module;
                this.selectedModuleIdx = moduleIdx;
                
                if (module.getType() == Module._TYPE_TOP_MODULE)
                    selectedTopModuleIdx = moduleIdx;
            }
        }
    }
    
    public int getSelectedTopModuleIdx() {
        return selectedTopModuleIdx;
    }

    public int getSelectedModuleIdx() {
        return selectedModuleIdx;
    }
    
    public Collection<Module> getTopModules() {
        return topModules;
    }
}
