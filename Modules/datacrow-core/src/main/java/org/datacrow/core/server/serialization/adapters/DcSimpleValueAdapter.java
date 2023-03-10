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

import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcSimpleValue;
import org.datacrow.core.utilities.Base64;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DcSimpleValueAdapter implements JsonDeserializer<DcSimpleValue>, JsonSerializer<DcSimpleValue> {

    public JsonElement serialize(
            DcSimpleValue src, 
            Type typeOfSrc, 
            JsonSerializationContext context) {
        
        JsonObject jo = new JsonObject();
        
        jo.addProperty("name", src.getName());
        jo.addProperty("itemId", src.getID());
        
        if (src.getIcon() != null)
            jo.addProperty("icon", new String(Base64.encode(src.getIcon().getBytes())));
        
        return jo;
    }
    
    public DcSimpleValue deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        
        JsonObject jo = json.getAsJsonObject();
        
        String id = jo.get("itemId").getAsString();
        String name = jo.get("name").getAsString();

        DcSimpleValue sv = new DcSimpleValue(id, name);
        
        if (jo.has("icon")) {
            String icon = jo.get("icon").getAsString();
            sv.setIcon(new DcImageIcon(Base64.decode(icon.toCharArray())));
        }

        return sv;
    }
}
