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

import javax.swing.KeyStroke;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class KeyStrokeAdapter implements JsonDeserializer<KeyStroke>, JsonSerializer<KeyStroke> {
    
    public JsonElement serialize(
            KeyStroke src, 
            Type typeOfSrc, 
            JsonSerializationContext context) {
        
        JsonObject jdco = new JsonObject();
        jdco.addProperty("keycode", src.getKeyCode());
        jdco.addProperty("modifiers", src.getModifiers());
        return jdco;
    }

    public KeyStroke deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        int keyCode = jsonObject.get("keycode").getAsInt();
        int modifiers = jsonObject.get("modifiers").getAsInt();
        return KeyStroke.getKeyStroke(keyCode, modifiers);
    }
}
