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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.server.serialization.SerializationHelper;
import org.datacrow.core.settings.DcModuleSettings;
import org.datacrow.core.settings.Setting;
import org.datacrow.core.settings.Settings;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SettingsAdapter implements JsonDeserializer<Settings>, JsonSerializer<Settings> {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(SettingsAdapter.class.getName());
    
    private static final String _CLASSNAME = "CLASSNAME";
    
    public JsonElement serialize(
            Settings src, 
            Type typeOfSrc, 
            JsonSerializationContext context) {
        
        JsonObject jsonObject = new JsonObject();
        
        jsonObject.addProperty(_CLASSNAME, src.getClass().getName());
       
        if (src instanceof DcModuleSettings) {
            jsonObject.addProperty("moduleIdx", ((DcModuleSettings) src).getModuleIdx());
        }

        List<JsonElement> jvalues = new ArrayList<JsonElement>();
        
        JsonObject jsetting;
        for (Setting setting : src.getSettings()) {
            jsetting = new JsonObject();
            jsetting.addProperty(setting.getKey(), setting.getValueAsString());
            jvalues.add(jsetting);
        }
        
        JsonElement tree = SerializationHelper.getInstance().getSimpleGson().toJsonTree(jvalues);
        jsonObject.add("settings", tree);
        
        return jsonObject;
    }
    
    public Settings deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        String className = jsonObject.get(_CLASSNAME).getAsString();
        Class<?> clazz = getObjectClass(className);

        Settings settings;
        if (jsonObject.has("moduleIdx")) {
            int moduleIdx = jsonObject.get("moduleIdx").getAsInt();    
            settings = new DcModuleSettings(DcModules.get(moduleIdx));
            
        } else {
            try {
                settings = (Settings) clazz.getConstructors()[0].newInstance(new Object[] {});
            } catch (Exception e) {
                logger.error("Could not instantiate (and therefore not deserialize) " + className, e);
                throw new JsonParseException("Could not instantiate (and therefore not deserialize) " + className, e);
            }
        }

        Set<Map.Entry<String, JsonElement>> entrySet;
        for (JsonElement jsetting : jsonObject.getAsJsonArray("settings")) {
            entrySet = jsetting.getAsJsonObject().entrySet();
            for(Map.Entry<String, JsonElement> entry : entrySet) {
                settings.setString(entry.getKey(), entry.getValue().getAsString());
            }
        }
        
        return settings;
        
    }
    
    public Class<?> getObjectClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e.getMessage());
        }
    }    
}
