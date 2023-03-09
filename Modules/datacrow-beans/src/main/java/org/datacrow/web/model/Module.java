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

package org.datacrow.web.model;

import java.util.ArrayList;
import java.util.Collection;

import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.web.DcBean;

public class Module extends DcBean {

    public static final int _TYPE_TOP_MODULE = 0;
    public static final int _TYPE_SUB_MODULE = 1;
    public static final int _TYPE_PROPERTY_MODULE = 2;
    public static final int _TYPE_TEMPLATE_MODULE = 3;
    public static final int _TYPE_CHILD_MODULE = 4;
    
    private int index;
    private String label;
    
    private String icon16;
    private String icon32;
    
    private String itemPluralName;
    private String itemName;
    
    private String icon16Path;
    private String icon32Path;
    
    private Collection<Module> subModules = new ArrayList<Module>();
    private Collection<Module> propertyModules = new ArrayList<Module>();
    private Collection<Module> templateModules = new ArrayList<Module>();
    
    private final boolean advancedView;
    private final int type;
    
    private Module childModule;
    
    public Module(DcModule module, int type) {
        super();

        this.index = module.getIndex();
        this.label = module.getLabel();
        this.itemPluralName = module.getObjectNamePlural();
        this.itemName = module.getObjectName();
        
        if (module.getChild() != null)
            this.childModule = new Module(module.getChild(), _TYPE_CHILD_MODULE);
        
        this.icon16 = "moduleicon" + index + "_16";
        this.icon32 = "moduleicon" + index + "_32";
        
        this.icon16Path = "resources/default/images/" + index + "_16.png";
        this.icon32Path = "resources/default/images/" + index + "_32.png";
        
        this.type = type;
        this.advancedView = DcModules.get(index).getType() != DcModule._TYPE_PROPERTY_MODULE;
    }
    
    public boolean isEditingAllowed() {
        return isEditingAllowed(DcModules.get(index));
    }
    
    public String getItemPluralName() {
        return itemPluralName;
    }
    
    public boolean isCanHaveChildren() {
        return childModule != null;
    }
    
    public Module getChildModule() {
        return childModule;
    }
    
    public boolean isAdvancedView() {
        return advancedView;
    }
    
    public int getOverviewRowLimit() {
        return advancedView ? 10 : 50;
    }

    public void addSubModule(Module module) {
        subModules.add(module);
    }
    
    public void addPropertyModule(Module module) {
        
        if (module.getLabel().startsWith(label))
            module.setLabel(module.getLabel().substring(label.length() + 1));
        
        if (module.getLabel().startsWith(itemName))
            module.setLabel(module.getLabel().substring(itemName.length() + 1));
        
        propertyModules.add(module);
    }
    
    public void addTemplateModule(Module module) {
        templateModules.add(module);
    }
    
    public Collection<Module> getPropertyModules() {
        return propertyModules;
    }

    public Collection<Module> getTemplateModules() {
        return templateModules;
    }
    
    public Collection<Module> getSubModules() {
        return subModules;
    }
    
    public String getItemName() {
        return itemName;
    }

    public String getIcon16Path() {
        return icon16Path;
    }

    public String getIcon32Path() {
        return icon32Path;
    }


    public String getIcon() {
        return DcModules.isTopModule(index) ? getIcon32() : getIcon16(); 
    }
    
    public String getIcon16() {
        return icon16;
    }

    public String getIcon32() {
        return icon32;
    }
    
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getType() {
        return type;
    }
}
