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
import org.datacrow.core.objects.Picture;
import org.datacrow.core.utilities.Base64;
import org.datacrow.core.utilities.CoreUtilities;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class PictureAdapter implements JsonDeserializer<Picture>, JsonSerializer<Picture> {
    
    public JsonElement serialize(
            Picture src, 
            Type typeOfSrc, 
            JsonSerializationContext context) {
        
        JsonObject jdco = new JsonObject();
        
        jdco.addProperty("edited", src.isEdited());
        jdco.addProperty("deleted", src.isDeleted());
        jdco.addProperty("changed", src.isChanged());
        
        if (src.isFilled(Picture._A_OBJECTID))
            jdco.addProperty("objectid", (String) src.getValue(Picture._A_OBJECTID));
        
        if (src.isFilled(Picture._C_FILENAME))
            jdco.addProperty("filename", (String) src.getValue(Picture._C_FILENAME));
        
        if (src.isFilled(Picture._G_EXTERNAL_FILENAME))
            jdco.addProperty("externalfilename", (String) src.getValue(Picture._G_EXTERNAL_FILENAME));
        
        if (src.isFilled(Picture._B_FIELD))
            jdco.addProperty("field", (String) src.getValue(Picture._B_FIELD));
        
        if (src.isFilled(Picture._E_HEIGHT))
            jdco.addProperty("height", (Long) src.getValue(Picture._E_HEIGHT));
        
        if (src.isFilled(Picture._F_WIDTH))
            jdco.addProperty("width", (Long) src.getValue(Picture._F_WIDTH));
        
        if (!CoreUtilities.isEmpty(src.getUrl()))
            jdco.addProperty("url", src.getUrl());
        
        if (!CoreUtilities.isEmpty(src.getThumbnailUrl()))
            jdco.addProperty("thumbnailurl", src.getThumbnailUrl());
        
        DcImageIcon icon = (DcImageIcon) src.getValue(Picture._D_IMAGE);
        
        if (icon != null) {
            jdco.addProperty("imageBytes", new String(Base64.encode(icon.getCurrentBytes())));  
        }
        
        return jdco;
    }

    public Picture deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject jo = json.getAsJsonObject();
        Picture pic = new Picture();
        
        pic.isEdited(jo.get("edited").getAsBoolean());
        pic.isDeleted(jo.get("deleted").getAsBoolean());
        
        boolean edited = jo.get("edited").getAsBoolean();
        
        if (jo.has("url"))
            pic.setUrl(jo.get("url").getAsString());
        
        if (jo.has("thumbnailurl"))
            pic.setThumbnailUrl(jo.get("thumbnailurl").getAsString());
        
        if (jo.has("objectid"))
            pic.setValue(Picture._A_OBJECTID, jo.get("objectid").getAsString());
        
        if (jo.has("field"))
            pic.setValue(Picture._B_FIELD, jo.get("field").getAsString());
        
        if (jo.has("filename"))
            pic.setValue(Picture._C_FILENAME, jo.get("filename").getAsString());
        
        if (jo.has("height"))
            pic.setValue(Picture._E_HEIGHT, jo.get("height").getAsLong());
        
        if (jo.has("width"))
            pic.setValue(Picture._F_WIDTH, jo.get("width").getAsLong());
        
        if (jo.has("externalfilename"))
            pic.setValue(Picture._G_EXTERNAL_FILENAME, jo.get("externalfilename").getAsString());
        
        if (jo.has("imageBytes")) {
            String base64 = jo.get("imageBytes").getAsString();
            DcImageIcon icon = new DcImageIcon(Base64.decode(base64.toCharArray()));
            pic.setValue(Picture._D_IMAGE, icon);
        }
        
        for (int fieldIdx : pic.getFieldIndices()) {
            pic.setChanged(fieldIdx, edited);
        }
        
        return pic;
    }
}
