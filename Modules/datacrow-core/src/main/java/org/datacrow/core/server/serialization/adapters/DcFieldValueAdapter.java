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

package org.datacrow.core.server.serialization.adapters;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.Picture;
import org.datacrow.core.server.serialization.helpers.DcFieldValue;
import org.datacrow.core.utilities.CoreUtilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DcFieldValueAdapter implements JsonDeserializer<DcFieldValue>, JsonSerializer<DcFieldValue> {

    private transient static DcLogger logger = DcLogManager.getInstance().getLogger(DcFieldValueAdapter.class.getName());
    
    @SuppressWarnings("unchecked")
    public JsonElement serialize(
            DcFieldValue src, 
            Type typeOfSrc, 
            JsonSerializationContext context) {
        
        JsonObject jdco = new JsonObject();
        
        try {
            jdco.addProperty("fieldindex", src.getFieldIndex());
            jdco.addProperty("moduleindex", src.getModuleIndex());
            jdco.addProperty("changed", src.isChanged());
            
            DcField field = DcModules.get(src.getModuleIndex()).getField(src.getFieldIndex());
            
            Object value = src.getValue();
            JsonElement je = null;
            if (value instanceof Picture)
                je = context.serialize(value, Picture.class);
            else if (value instanceof DcObject) {
                je = context.serialize(value, DcObject.class);
            } else if (value instanceof Number) {
                je = context.serialize(value.toString());
            } else if (value instanceof Date) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                je = context.serialize(formatter.format((Date) value));
            } else if (
                field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION &&
                value instanceof Collection) {
    
                JsonArray references = new JsonArray();
                JsonObject ref;
                
                for (DcMapping mapping : (Collection<DcMapping>) value) {
                    ref = (JsonObject) context.serialize(mapping.getReferencedObject(), DcObject.class);
                    
                    if (mapping.isFilled(DcMapping._A_PARENT_ID))
                        ref.addProperty("referenceParentId", (String) mapping.getValue(DcMapping._A_PARENT_ID));
                    
                    references.add(ref);
                }
                
                je = context.serialize(references);
            } else {
                je = context.serialize(value);
            }
            
            jdco.add("fieldvalue", je);
        } catch (Exception e) {
            logger.error("An error occurred during serialization of value [" + src.getValue() + "] "
                    + " for module [" + src.getModuleIndex() + "], Field: [" + src.getFieldIndex() + "]", e);
        }
        
        return jdco;
    }
    
    public DcFieldValue deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        int fieldIndex = jsonObject.get("fieldindex").getAsInt();
        int moduleIndex = jsonObject.get("moduleindex").getAsInt();
        boolean changed = jsonObject.get("changed").getAsBoolean();
        
        DcField field = DcModules.get(moduleIndex).getField(fieldIndex);
        Object result = null;
        
        JsonElement e = jsonObject.get("fieldvalue");
        if (e != null) { 
            if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                JsonObject jo = jsonObject.getAsJsonObject("fieldvalue");
                result = jo != null ? context.deserialize(jo, DcObject.class) : null;
            } else if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                JsonObject jo = jsonObject.getAsJsonObject("fieldvalue");
                if (jo != null) result = context.deserialize(jsonObject.getAsJsonObject("fieldvalue"), Picture.class);
            } else if (field.getValueType() == DcRepository.ValueTypes._BIGINTEGER ||
                       field.getValueType() == DcRepository.ValueTypes._LONG) {
            	String s = e.getAsString();
        		result = CoreUtilities.isEmpty(s) ? null : Long.valueOf(s);
            } else if (field.getValueType() == DcRepository.ValueTypes._DOUBLE) {
            	String s = e.getAsString();
        		result = CoreUtilities.isEmpty(s) ? null : Double.valueOf(s);
            } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                JsonArray array = (JsonArray) e;
                Iterator<?> iter = array.iterator();
                
                JsonObject joRef;
                DcObject ref;
                Collection<DcMapping> mappings = new ArrayList<DcMapping>();
                DcMapping mapping;
                String parentId;
                while (iter.hasNext()) {
                    joRef = (JsonObject) iter.next();
                    
                    parentId = null;
                    
                    if (joRef.has("referenceParentId")) {
                        parentId = joRef.get("referenceParentId").getAsString();
                        joRef.remove("referenceParentId");
                    }
                    
                    ref = context.deserialize(joRef, DcObject.class);
                    
                    mapping = new DcMapping(DcModules.getMappingModIdx(moduleIndex, ref.getModuleIdx(), fieldIndex));
                    mapping.setReference(ref);
                    
                    mapping.setValue(DcMapping._A_PARENT_ID, parentId);
                    mapping.setValue(DcMapping._B_REFERENCED_ID, ref.getID());
                    mappings.add(mapping);
                }
                result = mappings;
            } else if (field.getValueType() == DcRepository.ValueTypes._STRING) {
                result = e.getAsString();
            } else if (field.getValueType() == DcRepository.ValueTypes._BOOLEAN) {
                result = jsonObject.get("fieldvalue").getAsBoolean();
            } else if (field.getValueType() == DcRepository.ValueTypes._DATE ||
                       field.getValueType() == DcRepository.ValueTypes._DATETIME) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            	String s = e.getAsString();
                try {
                    result =  CoreUtilities.isEmpty(s) ? null :
                    	formatter.parse(jsonObject.get("fieldvalue").getAsString());
                } catch (Exception exp) {
                    logger.debug("Could not parse date from [" + jsonObject.get("fieldvalue").getAsString() + "]", exp);
                }
            } else {
                result = jsonObject.get("fieldvalue").getAsString();
            }
        }
            
        return new DcFieldValue(moduleIndex, fieldIndex, result, changed);
    }
}
