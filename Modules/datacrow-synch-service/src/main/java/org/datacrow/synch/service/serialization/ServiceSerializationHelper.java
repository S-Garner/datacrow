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

package org.datacrow.synch.service.serialization;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.datacrow.core.enhancers.IValueEnhancer;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcSimpleValue;
import org.datacrow.core.objects.Picture;
import org.datacrow.core.server.serialization.adapters.DcColorAdapter;
import org.datacrow.core.server.serialization.adapters.DcFieldValueAdapter;
import org.datacrow.core.server.serialization.adapters.DcFontAdapter;
import org.datacrow.core.server.serialization.adapters.DcObjectAdapter;
import org.datacrow.core.server.serialization.adapters.DcSimpleValueAdapter;
import org.datacrow.core.server.serialization.adapters.FileAdapter;
import org.datacrow.core.server.serialization.adapters.InterfaceAdapter;
import org.datacrow.core.server.serialization.adapters.PictureAdapter;
import org.datacrow.core.server.serialization.adapters.SettingsAdapter;
import org.datacrow.core.server.serialization.helpers.DcFieldValue;
import org.datacrow.core.settings.DcModuleSettings;
import org.datacrow.core.settings.Settings;
import org.datacrow.core.settings.objects.DcColor;
import org.datacrow.core.settings.objects.DcFont;
import org.datacrow.synch.service.request.ServiceLoginRequest;
import org.datacrow.synch.service.request.ServiceRequest;
import org.datacrow.synch.service.response.ServiceErrorResponse;
import org.datacrow.synch.service.response.ServiceLoginResponse;
import org.datacrow.synch.service.response.ServiceResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ServiceSerializationHelper {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ServiceSerializationHelper.class.getName());

    private static ServiceSerializationHelper instance;
    
    // GSON objects are thread-safe
    private final Gson gson;
    private final Gson gsonSimple;
    
    static {
        instance = new ServiceSerializationHelper();
    }
    
    public static ServiceSerializationHelper getInstance() {
        return instance;
    }
    
    public Gson getSimpleGson() {
        return gsonSimple;
    }
    
    public Gson getGson() {
        return gson;
    }    
    
    private ServiceSerializationHelper() {
        
        gson = new GsonBuilder()
                .disableHtmlEscaping()
                .registerTypeAdapter(Picture.class, new PictureAdapter())
                .registerTypeAdapter(DcObject.class, new DcObjectAdapter())
                .registerTypeAdapter(DcFieldValue.class, new DcFieldValueAdapter())
                .registerTypeAdapter(DcSimpleValue.class, new DcSimpleValueAdapter())
                .registerTypeAdapter(File.class, new FileAdapter())
                .registerTypeAdapter(DcColor.class, new DcColorAdapter())
                .registerTypeAdapter(DcFont.class, new DcFontAdapter())
                .registerTypeAdapter(IValueEnhancer.class, new InterfaceAdapter())
                .registerTypeAdapter(DcModule.class, new InterfaceAdapter())
                .registerTypeAdapter(Settings.class, new SettingsAdapter())
                .create();

        gsonSimple = new GsonBuilder()
                .disableHtmlEscaping()
                .registerTypeAdapter(Picture.class, new PictureAdapter())
                .registerTypeAdapter(DcObject.class, new DcObjectAdapter())
                .registerTypeAdapter(DcFieldValue.class, new DcFieldValueAdapter())
                .registerTypeAdapter(DcSimpleValue.class, new DcSimpleValueAdapter())
                .registerTypeAdapter(File.class, new FileAdapter())
                .registerTypeAdapter(DcColor.class, new DcColorAdapter())
                .registerTypeAdapter(DcFont.class, new DcFontAdapter())
                .registerTypeAdapter(DcModuleSettings.class, new SettingsAdapter())
                .registerTypeAdapter(Settings.class, new SettingsAdapter())
                .registerTypeAdapter(IValueEnhancer.class, new InterfaceAdapter())
                .create();
    }
    
    public String serialize(Object o) {
        String json = getGson().toJson(o);
        return json;
    }
    
    private static String getJson(ObjectInputStream is) throws IOException, ClassNotFoundException {
        String json = (String) is.readObject();
        return json;
    }
    
    public ServiceResponse deserializeServerResponse(ObjectInputStream is) throws IOException, ClassNotFoundException {
        String json = getJson(is);
        Gson gson = getGson();
        
        ServiceResponse reponse = gson.fromJson(json, ServiceResponse.class);
        
        switch (reponse.getType()) {
        case ERROR_REPONSE:
        	reponse = gson.fromJson(json, ServiceErrorResponse.class);
        	break;
        case LOGIN_REPONSE:        	
        	reponse = gson.fromJson(json, ServiceLoginResponse.class);
        default:
            logger.fatal("No server response implementation found for type [" + reponse.getType() + "]");
        }
        
        return reponse;
    }
    
    public ServiceRequest deserializeClientRequest(ObjectInputStream is) throws IOException, ClassNotFoundException {
        String json = getJson(is);
        Gson gson = getGson();
        
        ServiceRequest request = gson.fromJson(json, ServiceRequest.class);
        
        switch (request.getType()) {
        case LOGIN_REQUEST:
        	request = gson.fromJson(json, ServiceLoginRequest.class);
        	break;
        default:
        	logger.fatal("No client request implementation found for type [" + request.getType() + "]");
        }

        return request;
    }
}
