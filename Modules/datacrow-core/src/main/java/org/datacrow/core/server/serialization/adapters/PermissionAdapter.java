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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.objects.helpers.Permission;
import org.datacrow.core.objects.helpers.User;
import org.datacrow.core.utilities.CoreUtilities;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class PermissionAdapter implements JsonDeserializer<Permission>, JsonSerializer<Permission> {
    
	private transient static Logger logger = DcLogManager.getLogger(PermissionAdapter.class.getName());
	
    public JsonElement serialize(
    		Permission src, 
            Type typeOfSrc, 
            JsonSerializationContext context) {
        
        JsonObject jdco = new JsonObject();
        
        jdco.addProperty("id", (String) src.getValue(Permission._ID));
        jdco.addProperty("plugin", (String) src.getValue(Permission._A_PLUGIN));
        jdco.addProperty("field", (Number) src.getValue(Permission._B_FIELD));
        jdco.addProperty("module", (Number) src.getValue(Permission._C_MODULE));
        jdco.addProperty("view", (Boolean) src.getValue(Permission._D_VIEW));
        jdco.addProperty("edit", (Boolean) src.getValue(Permission._E_EDIT));
        
        if (src.isFilled(Permission._SYS_CREATED))
        	jdco.addProperty("created",  new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format((Date) src.getValue(Permission._SYS_CREATED)));
        
        if (src.isFilled(Permission._SYS_MODIFIED))
        	jdco.addProperty("modified", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format((Date) src.getValue(Permission._SYS_MODIFIED)));
        
        jdco.addProperty("changed", src.isChanged(Permission._D_VIEW) || src.isChanged(Permission._E_EDIT));
        
        String user = 
        		src.getValue(Permission._F_USER) instanceof User ?
   				((User)src.getValue(Permission._F_USER)).getID() : (String) src.getValue(Permission._F_USER);
        
        jdco.addProperty("user", user);
        
        return jdco;
    }

    public Permission deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject src = json.getAsJsonObject();
        Permission permission = new Permission();
        permission.setNew(false);
        
        boolean changed = src.get("changed").getAsBoolean();
        
        if (!CoreUtilities.isEmpty(src.get("id")))
        	permission.setValue(Permission._ID, src.get("id").getAsString());
        
        if (!CoreUtilities.isEmpty(src.get("plugin")))
        	permission.setValue(Permission._A_PLUGIN, src.get("plugin").getAsString());
        
        if (!CoreUtilities.isEmpty(src.get("field")))
        	permission.setValue(Permission._B_FIELD, src.get("field").getAsLong());
        
        if (!CoreUtilities.isEmpty(src.get("module")))
        	permission.setValue(Permission._C_MODULE, src.get("module").getAsLong());
        
        if (!CoreUtilities.isEmpty(src.get("view")))
        	permission.setValue(Permission._D_VIEW, src.get("view").getAsBoolean());
        
        if (!CoreUtilities.isEmpty(src.get("edit")))
        	permission.setValue(Permission._E_EDIT, src.get("edit").getAsBoolean());
        
        if (!CoreUtilities.isEmpty(src.get("user")))
        	permission.setValue(Permission._F_USER, src.get("user").getAsString());
        
        try {
        	if (!CoreUtilities.isEmpty(src.get("created")))
        		permission.setValue(Permission._SYS_CREATED, 
        				new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(src.get("created").getAsString()));
        	
        	if (!CoreUtilities.isEmpty(src.get("modified")))
        		permission.setValue(Permission._SYS_MODIFIED, 
        				new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(src.get("modified").getAsString()));
        } catch (ParseException pe) {
        	logger.error(pe, pe);
        }
        
        permission.markAsUnchanged();
        
        if (changed) {
        	permission.setChanged(Permission._D_VIEW, changed);
        	permission.setChanged(Permission._E_EDIT, changed);
        }

        return permission;
    }
}
