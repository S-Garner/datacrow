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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.console.UIComponents;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.MusicTrack;
import org.datacrow.core.server.Connector;
import org.datacrow.web.DcBean;
import org.datacrow.web.bean.ModulesBean;
import org.datacrow.web.util.WebUtilities;

public class Item extends DcBean {
    
	private static final long serialVersionUID = 1L;

	private List<Field> detailFields = new ArrayList<Field>();
	private List<Field> technicalFields = new ArrayList<Field>();
	private List<Field> picturesFields = new ArrayList<Field>();
	private List<Field> iconFields = new ArrayList<Field>();
	
	private List<Field> fields = new ArrayList<Field>();
	private List<Field> overviewFields = new ArrayList<Field>();
	private List<Field> overviewFieldsSpan = new ArrayList<Field>();
	
	private Module module;
    private int moduleIdx;
    
    private String ID;
    private String title;
    private Picture cover = new Picture(false);
    private Picture icon;
    
    private boolean isNew = false;
    private boolean isLoaded = false;
    
    private List<Reference> referencingItems = new ArrayList<Reference>();
    private List<Item> children = new ArrayList<Item>();
    
    public Item(DcObject dco) {
    	this.moduleIdx = dco.getModuleIdx();
    	this.title = dco.toString();
    	this.ID = dco.getID();
    	this.icon = new Picture(true, "icon_" + ID);
    	
    	this.setOverviewFields();
    	this.setDetailFields();
    	this.setFields();
    	
    	this.setValues(dco);
    	
    	try {
    	    ModulesBean modulesBean = (ModulesBean) WebUtilities.getBean("modulesBean");
    	    module = modulesBean.getModule(moduleIdx);
    	} catch (Exception e) {
    	    WebUtilities.log(Level.ERROR, "Could not find / instantiate the Modules Bean", e);
    	}
    }
    
    private void setFields() {
        fields = new ArrayList<Field>();
        fields.addAll(overviewFields);
        
        List<Field> c = new ArrayList<Field>();
        c.addAll(detailFields);
        c.addAll(iconFields);
        c.addAll(picturesFields);
        c.addAll(overviewFieldsSpan);
        c.addAll(technicalFields);
        
        for (Field field : c) {
            if (!fields.contains(field))
                fields.add(field);
        }
    }
    
    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

    public void setIsNewItem(boolean b) {
        this.isNew = b;
    }
    
    public boolean isNewItem() {
        return isNew;
    }
    
    /**
     * Loads all children and referencing items.
     */
    public void loadAllOtherItems() {
        loadReferencingItems();
        loadChildren();
    }
    
    public List<Reference> getReferencingItems() {
        return this.referencingItems;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Gets the main image URL
     * @return URL to the small version of the picture
     */
    public Picture getCover() {
        return cover;
    }
    
    public String getTitle() {
        return title;
    }
    
    public Picture getIcon() {
        return icon;
    }
    
    public int getModuleIdx() {
    	return moduleIdx;
    }
    
    public boolean isHasPictureFields() {
        return picturesFields.size() > 0;
    }
    
    public boolean isHasChildren() {
        return children.size() > 0;
    }
    
    public boolean isCanHaveIcon() {
        return iconFields.size() > 0;
    }
    
    public boolean isHasPictures() {
        boolean hasPictures = false;
        for (Field field : picturesFields) {
            if (field.getValue() instanceof Picture && ((Picture) field.getValue()).isAlive())
                hasPictures = true;
        }
        return hasPictures;
    }
    
    public boolean isHasReferencingItems() {
        return referencingItems.size() > 0;
    }
    
    public Collection<Field> getPictureFields() {
    	return picturesFields;
    }
    
    public Collection<Field> getAlivePictureFields() {
        List<Field> alivePicFields = new ArrayList<Field>();
        for (Field pictureField : picturesFields) {
            if (pictureField.isPictureAlive())
                alivePicFields.add(pictureField);
        }
        return alivePicFields;
    }
    
    public Collection<Field> getIconFields() {
        return iconFields;
    }
    
    private List<Integer> checkFields(int[] fields) {
        List<Integer> c = new ArrayList<Integer>();
        
        if (fields != null) {
            for (int field : fields)
                c.add(Integer.valueOf(field));
        }
        
        if (!c.contains(DcObject._ID))
            c.add(Integer.valueOf(DcObject._ID));
        
        return c;
    }
    
    private void setDetailFields() {
        DcModule m = DcModules.get(moduleIdx);
        DcField field;
        
        for (int fieldIdx : checkFields(m.getSettings().getIntArray(DcRepository.ModuleSettings.stWebItemFormFields))) {
            
            field = m.getField(fieldIdx);
            
            if (!isAuthorized(field) ||
                !field.isEnabled() || 
                 field.isLoanField()) 
                continue;
           
            Field f = new Field(field);
            
            if (field.getIndex() == DcObject._SYS_CREATED ||
                field.getIndex() == DcObject._SYS_EXTERNAL_REFERENCES ||
                field.getIndex() == DcObject._SYS_FILEHASH ||
                field.getIndex() == DcObject._SYS_FILEHASHTYPE ||
                field.getIndex() == DcObject._SYS_MODIFIED ||
                field.getIndex() == DcObject._SYS_MODULE ||
                field.getIndex() == DcObject._SYS_DISPLAYVALUE ||
                field.getIndex() == DcObject._SYS_SERVICE ||
                field.getIndex() == DcObject._SYS_SERVICEURL)
                technicalFields.add(f);
            else if (field.getValueType() == DcRepository.ValueTypes._PICTURE)
                picturesFields.add(f);
            else if (field.getValueType() == DcRepository.ValueTypes._ICON)
                iconFields.add(f);
            else
                detailFields.add(f);
        }
    }
    
    private void setOverviewFields() {
        DcModule m = DcModules.get(moduleIdx);
        DcField field;
        for (int fieldIdx : checkFields(m.getSettings().getIntArray(DcRepository.ModuleSettings.stWebOverviewFields))) {
        	
            field = m.getField(fieldIdx);
            
        	if (!field.isEnabled()) continue;

            if (    !isAuthorized(field) ||
                    !field.isEnabled() || 
                    (field.isReadOnly() && !field.isUiOnly()) || 
                    field.isLoanField()) 
            	continue;
           
            Field f = new Field(field);
            
            if (field.getFieldType() == UIComponents._LONGTEXTFIELD)
                overviewFieldsSpan.add(f);
            else
                overviewFields.add(f);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void setValues(DcObject dco) {
        Picture picture;
        Object value;
        DcField field;
        boolean coverFound = false;
        Collection<Reference> references;
        for (Field wf : getFields()) {
            field = dco.getField(wf.getIndex());
            value = dco.getValue(field.getIndex());
            if (value != null) {
                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                    DcObject o = (DcObject) value;
                    value = new Reference(o.toString(),
                                          o.getValue(DcObject._ID).toString(),
                                          field.getReferenceIdx());
                } else if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                	picture = new Picture(false, getID() + "_" + field.getDatabaseFieldName());
                	value = picture;
                } else if (wf.isDuration()) {
                    Calendar cal = Calendar.getInstance();  
                    cal.clear();
                    cal.set(Calendar.SECOND, ((Long) value).intValue());
                    value = cal.getTime();                	
                } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                	references = new ArrayList<Reference>();
                	for (DcObject o : (Collection<DcObject>) value) {
                		references.add(new Reference(o.toString(),
                									 o.getValue(DcMapping._B_REFERENCED_ID).toString(),
                									 field.getReferenceIdx()));
                	}
                	
                    value = references;
                } else if (value instanceof Number) {
                    value = String.valueOf(value);
                }
            } else if (wf.getType() == Field._IMAGE && !coverFound) {
            	picture = new Picture(false, getID() + "_" + field.getDatabaseFieldName());
            	if (picture.isAlive()) {
            		cover = picture;
            		coverFound = true;
            	}
            }
            
            wf.setValueLowLevel(value);
            wf.setChanged(false);
        }
    }

    public List<Field> getFields() {
        return fields;
    }
    
    public String getID() {
        return ID;
    }

    public List<Field> getDetailFields() {
        return detailFields;
    }
    
    public List<Field> getTechnicalFields() {
        return technicalFields;
    }    
    
    public List<Field> getOverviewFields() {
        List<Field> fields = new ArrayList<Field>();
        for (Field field : overviewFields)
            if (field.getValue() != null)
                fields.add(field);
        
        return fields;
    }
    
    public List<Field> getOverviewFieldsSpan() {
        return overviewFieldsSpan;
    }

    public Module getModule() {
        return module;
    }
    
    public List<Item> getChildren() {
        return children;
    }
    
    private void loadChildren() {
        children.clear();
        
        if (module.isCanHaveChildren()) {
            
            Connector conn = DcConfig.getInstance().getConnector();
            DcObject dco = conn.getItem(moduleIdx, ID);
            
            if (dco != null) {
                dco.loadChildren(dco.getModule().getMinimalFields(null));
                
                for (DcObject child : dco.getChildren()) {
                    
                    Item item = new Item(child);
                    
                    if (child.getModuleIdx() == DcModules._MUSIC_TRACK) {
                        child.load(null);
                        
                        String track = child.getDisplayString(MusicTrack._F_TRACKNUMBER);
                        String title = child.getDisplayString(MusicTrack._A_TITLE);
                        String playlength = child.getDisplayString(MusicTrack._J_PLAYLENGTH);
                        
                        String trackTitle = track.length() > 0 ? track + " - " + title : title;
                        trackTitle += playlength.length() > 0 ? " (" + playlength + ")" : "";
                        
                        item.setTitle(trackTitle);
                    }

                    children.add(item);
                }
            }
        }
    }
    
    private void loadReferencingItems() {
        this.referencingItems.clear();
        
        if (DcModules.getActualReferencingModules(moduleIdx).size() > 0 &&
            moduleIdx != DcModules._CONTACTPERSON &&
            moduleIdx != DcModules._CONTAINER) {
            
            Connector conn = DcConfig.getInstance().getConnector();
            List<DcObject> references = conn.getReferencingItems(moduleIdx, ID);
            Collections.sort(references);
            
            for (DcObject dco : references) {
                this.referencingItems.add(new Reference(dco.toString(), dco.getID(), dco.getModuleIdx()));
            }
        }
    }
    
    @Override
    public String toString() {
        return title;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof Item) {
            return o == this || ((Item) o).getID().equalsIgnoreCase(ID);
        }
        
        return false;
    }
}