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

import org.datacrow.core.server.serialization.SerializationHelper;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class InterfaceAdapter implements JsonSerializer<Object>, JsonDeserializer<Object> {

    private static final String _CLASSNAME = "CLASSNAME";
    private static final String _DATA = "DATA";

    public JsonElement serialize(Object src, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(_CLASSNAME, src.getClass().getName());
        
        Gson gson = SerializationHelper.getInstance().getSimpleGson();
        String json = gson.toJson(src);
        
        jsonObject.add(_DATA, JsonParser.parseString(json).getAsJsonObject());
        return jsonObject;
    }
    
    public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {

        // get the class name
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String className = jsonObject.get(_CLASSNAME).getAsString();
        Class<?> clazz = getObjectClass(className);
        
        // get the data element
        JsonObject obj = jsonObject.get(_DATA).getAsJsonObject();
        
        // then, read with the simple reader (make sure we don't enter an eternal loop)
        return SerializationHelper.getInstance().getSimpleGson().fromJson(obj, clazz);
    }

    public Class<?> getObjectClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e.getMessage());
        }
    }
}
