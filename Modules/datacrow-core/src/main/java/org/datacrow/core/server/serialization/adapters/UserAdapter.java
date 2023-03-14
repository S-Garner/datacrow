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

import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcValue;
import org.datacrow.core.objects.helpers.User;
import org.datacrow.core.server.serialization.helpers.DcFieldValue;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class UserAdapter implements JsonDeserializer<User>, JsonSerializer<User> {
    
    public JsonElement serialize(
    		User src, 
            Type typeOfSrc, 
            JsonSerializationContext context) {
        
        JsonObject jdco = new JsonObject();
        
        src.loadImageData();
        
        jdco.addProperty("moduleIdx", src.getModuleIdx());
        jdco.addProperty("isnew", src.isNew());
        
        JsonArray array = new JsonArray();
        
        DcValue v;
        for (DcField field :  src.getFields()) {
            v = src.getValueDef(field.getIndex());
            DcFieldValue value = new DcFieldValue(
                    field.getModule(),
                    field.getIndex(), 
                    v.getValue(), 
                    v.isChanged());
            
            if (field.getIndex() == User._E_PHOTO && !DcModules.isLoaded())
            	continue;
            
            array.add(context.serialize(value));
        }
        
        jdco.add("values", array);
        
        return jdco;      
    }

    public User deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        boolean isnew = jsonObject.get("isnew").getAsBoolean();

        User dco = new User();
        
        JsonArray values = jsonObject.getAsJsonArray("values");
        
        DcFieldValue fieldValue;
        for (JsonElement value : values) {
            fieldValue = context.deserialize(value, DcFieldValue.class);

            if (fieldValue.getFieldIndex() == User._E_PHOTO && !DcModules.isLoaded()) 
            	continue;
            
            dco.setValue(fieldValue.getFieldIndex(), fieldValue.getValue());
            dco.setChanged(fieldValue.getFieldIndex(), fieldValue.isChanged());
        }
        
        dco.setNew(isnew);
            
        return dco;
    }
}
