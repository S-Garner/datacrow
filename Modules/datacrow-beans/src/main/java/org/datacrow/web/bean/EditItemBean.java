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

package org.datacrow.web.bean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.datacrow.core.DcConfig;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;
import org.datacrow.web.ReferencesCache;
import org.datacrow.web.model.Field;
import org.datacrow.web.model.Item;
import org.datacrow.web.model.Module;
import org.datacrow.web.model.Picture;
import org.datacrow.web.model.Reference;
import org.datacrow.web.util.WebUtilities;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named
@SessionScoped
public class EditItemBean extends ItemBean {
    
    private Field field = null;
    
    public String add() {
        
        try {
            // reset the bread crumb
            try {
                EditItemBreadCrumbBean editItemBreadCrumbBean = (EditItemBreadCrumbBean) WebUtilities.getBean("editItemBreadCrumbBean");
                editItemBreadCrumbBean.reset();
            } catch (Exception e) {
                WebUtilities.log(Level.ERROR, "Could not find / instantiate the Bread Crumb Bean", e);
            }
            
            // get the selected module
            ModulesBean modulesBean = (ModulesBean) WebUtilities.getBean("modulesBean");
            
            if (modulesBean.getSelectedModuleIdx() != -1) {
            
                DcModule module = DcModules.get(modulesBean.getSelectedModuleIdx());
                
                DcObject dco = module.getItem();
                dco.setIDs();
                
                Item item = new Item(dco);
                item.setIsNewItem(true);
                item.setTitle(DcResources.getText("lblNewItem", module.getObjectName()));
                
                setItem(item);
            }
            
        } catch (Exception e) {
            WebUtilities.log(Level.ERROR, e);
        }
        
        return "/index";
    }
    
    public String add(Field field) {
        
        int moduleIdx = field.getReferenceModuleIdx();
        DcModule module = DcModules.get(moduleIdx);
        
        DcObject dco = module.getItem();
        dco.setIDs();
        
        Item item = new Item(dco);
        item.setIsNewItem(true);
        item.setTitle(DcResources.getText("lblNewItem", module.getObjectName()));
        
        setItem(item);
        
        this.field = field;
        
        return "/index";
    }
    
    @Override
    public void setItem(Item item) {
        
        this.field = null;
        this.item = item;
        
        if (!item.isNewItem() && !item.isLoaded()) {
            Connector conn = DcConfig.getInstance().getConnector();
            this.item.setValues(conn.getItem(item.getModuleIdx(), item.getID()));
            this.item.loadAllOtherItems();
            this.item.setLoaded(true);
        }
        
        try {
            EditItemBreadCrumbBean editItemBreadCrumbBean = (EditItemBreadCrumbBean) WebUtilities.getBean("editItemBreadCrumbBean");
            editItemBreadCrumbBean.addItem(this.item);
        } catch (Exception e) {
            WebUtilities.log(Level.ERROR, "Could not find / instantiate the Bread Crumb Bean", e);
        }
    }
    
    public String save() {
        Item currentItem = getItem();
        if (currentItem != null) {
            if (currentItem.isNewItem())
                insert(currentItem);
            else
                update(currentItem);
            
            if (currentItem.getModule().getType() == Module._TYPE_PROPERTY_MODULE)
                ReferencesCache.getInstance().forceRefresh(currentItem.getModuleIdx());
        }
        
        return "/index";
    }
    
    @SuppressWarnings("rawtypes")
    private void update(Item item) {
        Connector conn = DcConfig.getInstance().getConnector();
        DcObject dco = conn.getItem(item.getModuleIdx(), item.getID());
        
        dco.markAsUnchanged();
        dco.setNew(false);
        
        Object o;
        boolean changed = false;
        for (Field field : item.getFields()) {
            if (field.isChanged()) {
                
                if ((field.isImage() || field.isIcon()) && field.getValue() instanceof Picture) {
                    Picture p = (Picture) field.getValue();
                    
                    if (p.isEdited()) {
                        dco.setValue(field.getIndex(), new DcImageIcon(p.getContents()));
                        changed = true;
                    } else if (p.isDeleted()) {
                        dco.setValue(field.getIndex(), null);
                        changed = true;
                    }
                } else if (field.isDropDown()) {
                    dco.setValue(field.getIndex(), null);
                    
                    if (field.getValue() instanceof Reference) {
                        o = conn.getItem(field.getReferenceModuleIdx(), ((Reference) field.getValue()).getId());
                        
                        if (o == null) {
                            dco.createReference(field.getIndex(), ((Reference) field.getValue()).getLabel()); 
                        } else {
                            dco.createReference(field.getIndex(), o);
                        }
                    }

                    changed = true;                    
                } else if (field.isMultiRelate() || field.isTagField()) {
                    dco.setValue(field.getIndex(), null);
                    for (Object ref : (List) field.getValue()) {
                        if (ref instanceof Reference) {
                            o = conn.getItem(field.getReferenceModuleIdx(), ((Reference) ref).getId());
                            if (o == null) {
                                dco.createReference(field.getIndex(), ((Reference) ref).getLabel());
                                ReferencesCache.getInstance().forceRefresh(field.getReferenceModuleIdx());
                            } else {
                                dco.createReference(field.getIndex(), o);
                            }
                        }
                    }
                    changed = true;
                } else if (field.isDuration()) {
                    if (field.getValue() instanceof Date) {
                        Date duration = (Date) field.getValue();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(duration);
                        int seconds = cal.get(Calendar.SECOND);
                        seconds = seconds + (cal.get(Calendar.MINUTE) * 60);
                        seconds = seconds + (cal.get(Calendar.HOUR) * 60 * 60);
                        dco.setValue(field.getIndex(), Long.valueOf(seconds));
                        changed = true;
                    }
                } else if (!field.isImage() && !field.isIcon()) {
                    dco.setValue(field.getIndex(), field.getValue());
                    changed = true;
                }
            }
        }
        
        if (changed) {
            try {
                conn.saveItem(dco);
                FacesContext.getCurrentInstance().addMessage(
                        null, 
                        new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Info", 
                        DcResources.getText("msgDataSaved")));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(
                        null, 
                        new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Warning", 
                        e.getMessage()));
            }
            
            item.setLoaded(false);
            setItem(item);
            
            getItemsBean().search();
            
        } else {
            FacesContext.getCurrentInstance().addMessage(
                    null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Info", 
                    DcResources.getText("msgNoChangesToSave")));
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void insert(Item item) {
        Connector conn = DcConfig.getInstance().getConnector();
        
        DcObject dco = DcModules.get(item.getModuleIdx()).getItem();
        dco.setNew(true);
        
        Object o;
        Picture p;
        for (Field field : item.getFields()) {
            if (field.isChanged()) {
                if ((field.isImage() || field.isIcon()) && field.getValue() instanceof Picture) {
                    p = (Picture) field.getValue();
                    dco.setValue(field.getIndex(), new DcImageIcon(p.getContents()));
                } else if (field.isMultiRelate() || field.isTagField()) {
                    for (Object ref : (List) field.getValue()) {
                        o = conn.getItem(field.getReferenceModuleIdx(), ((Reference) ref).getId());
                        if (o == null) {
                            dco.createReference(field.getIndex(), ((Reference) ref).getLabel());
                            ReferencesCache.getInstance().forceRefresh(field.getReferenceModuleIdx());
                        } else {
                            dco.createReference(field.getIndex(), o);
                        }
                    }
                } else if (!field.isImage() && !field.isIcon()) {
                    dco.setValue(field.getIndex(), field.getValue());
                }
            }
        }
        
        try {
            dco.setIDs();
            conn.saveItem(dco);
            FacesContext.getCurrentInstance().addMessage(
                    null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Info", 
                    DcResources.getText("msgDataSaved")));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                    null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Warning", 
                    e.getMessage()));
        }
        
        if (field != null) {
            Reference reference = new Reference(dco.toString(), dco.getID(), dco.getModuleIdx());
            if (field.getType() == Field._MULTIRELATE) {
                @SuppressWarnings("unchecked")
                List<Reference> references = (List<Reference>) field.getValue();
                references = references == null ? new ArrayList<Reference>() : references;
                references.add(reference);
                field.setChanged(true);
            } else {
                field.setValue(reference);
                field.setChanged(true);
            }
        }
        
        setItem(new Item(conn.getItem(item.getModuleIdx(), dco.getID())));
        getItemsBean().search();
    }
}