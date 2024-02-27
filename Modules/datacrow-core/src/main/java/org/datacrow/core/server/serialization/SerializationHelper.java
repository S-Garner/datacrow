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

package org.datacrow.core.server.serialization;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.datacrow.core.enhancers.IValueEnhancer;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcSimpleValue;
import org.datacrow.core.server.requests.ClientRequest;
import org.datacrow.core.server.requests.ClientRequestApplicationSettings;
import org.datacrow.core.server.requests.ClientRequestAttachmentAction;
import org.datacrow.core.server.requests.ClientRequestAttachmentsDelete;
import org.datacrow.core.server.requests.ClientRequestAttachmentsList;
import org.datacrow.core.server.requests.ClientRequestExecuteSQL;
import org.datacrow.core.server.requests.ClientRequestItem;
import org.datacrow.core.server.requests.ClientRequestItemAction;
import org.datacrow.core.server.requests.ClientRequestItemKeys;
import org.datacrow.core.server.requests.ClientRequestItems;
import org.datacrow.core.server.requests.ClientRequestLogin;
import org.datacrow.core.server.requests.ClientRequestModuleSettings;
import org.datacrow.core.server.requests.ClientRequestModules;
import org.datacrow.core.server.requests.ClientRequestPictureAction;
import org.datacrow.core.server.requests.ClientRequestPicturesDelete;
import org.datacrow.core.server.requests.ClientRequestPicturesList;
import org.datacrow.core.server.requests.ClientRequestReferencingItems;
import org.datacrow.core.server.requests.ClientRequestRemoveReferenceTo;
import org.datacrow.core.server.requests.ClientRequestSavePictureOrder;
import org.datacrow.core.server.requests.ClientRequestSimpleValues;
import org.datacrow.core.server.requests.ClientRequestValueEnhancers;
import org.datacrow.core.server.response.DefaultServerResponse;
import org.datacrow.core.server.response.ServerActionResponse;
import org.datacrow.core.server.response.ServerApplicationSettingsRequestResponse;
import org.datacrow.core.server.response.ServerAttachmentActionResponse;
import org.datacrow.core.server.response.ServerAttachmentsListResponse;
import org.datacrow.core.server.response.ServerErrorResponse;
import org.datacrow.core.server.response.ServerItemKeysRequestResponse;
import org.datacrow.core.server.response.ServerItemRequestResponse;
import org.datacrow.core.server.response.ServerItemsRequestResponse;
import org.datacrow.core.server.response.ServerLoginResponse;
import org.datacrow.core.server.response.ServerModulesRequestResponse;
import org.datacrow.core.server.response.ServerModulesSettingsResponse;
import org.datacrow.core.server.response.ServerPictureActionResponse;
import org.datacrow.core.server.response.ServerPictureSaveActionResponse;
import org.datacrow.core.server.response.ServerPicturesListResponse;
import org.datacrow.core.server.response.ServerResponse;
import org.datacrow.core.server.response.ServerSQLResponse;
import org.datacrow.core.server.response.ServerSimpleValuesResponse;
import org.datacrow.core.server.response.ServerValueEnhancersRequestResponse;
import org.datacrow.core.server.serialization.adapters.DcColorAdapter;
import org.datacrow.core.server.serialization.adapters.DcFieldValueAdapter;
import org.datacrow.core.server.serialization.adapters.DcFontAdapter;
import org.datacrow.core.server.serialization.adapters.DcObjectAdapter;
import org.datacrow.core.server.serialization.adapters.DcSimpleValueAdapter;
import org.datacrow.core.server.serialization.adapters.FileAdapter;
import org.datacrow.core.server.serialization.adapters.InterfaceAdapter;
import org.datacrow.core.server.serialization.adapters.SettingsAdapter;
import org.datacrow.core.server.serialization.helpers.DcFieldValue;
import org.datacrow.core.settings.DcModuleSettings;
import org.datacrow.core.settings.Settings;
import org.datacrow.core.settings.objects.DcColor;
import org.datacrow.core.settings.objects.DcFont;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SerializationHelper {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(SerializationHelper.class.getName());

    private static SerializationHelper instance;
    
    // GSON objects are thread-safe
    private final Gson gson;
    private final Gson gsonSimple;
    
    static {
        instance = new SerializationHelper();
    }
    
    public static SerializationHelper getInstance() {
        return instance;
    }
    
    public Gson getSimpleGson() {
        return gsonSimple;
    }
    
    public Gson getGson() {
        return gson;
    }    
    
    private SerializationHelper() {
        
        gson = new GsonBuilder()
                .disableHtmlEscaping()
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
    
    public ServerResponse deserializeServerResponse(ObjectInputStream is) throws IOException, ClassNotFoundException {
        String json = getJson(is);
        Gson gson = getGson();
        
        int type = ServerResponse._RESPONSE_DEFAULT;
        type = gson.fromJson(json, ServerResponse.class).getType();
        ServerResponse sr = getGson().fromJson(json, ServerResponse.class);
        
        if (type == ServerResponse._RESPONSE_ACTION)
            sr = gson.fromJson(json, ServerActionResponse.class);
        else if (type == ServerResponse._RESPONSE_APPLICATION_SETTINGS)
            sr = gson.fromJson(json, ServerApplicationSettingsRequestResponse.class);
        else if (type == ServerResponse._RESPONSE_DEFAULT)
            sr = gson.fromJson(json, DefaultServerResponse.class);
        else if (type == ServerResponse._RESPONSE_ERROR)
            sr = gson.fromJson(json, ServerErrorResponse.class);
        else if (type == ServerResponse._RESPONSE_ITEM_KEYS)
            sr = gson.fromJson(json, ServerItemKeysRequestResponse.class);
        else if (type == ServerResponse._RESPONSE_ITEM_REQUEST)
            sr = gson.fromJson(json, ServerItemRequestResponse.class);
        else if (type == ServerResponse._RESPONSE_ITEMS_REQUEST)
            sr = gson.fromJson(json, ServerItemsRequestResponse.class);
        else if (type == ServerResponse._RESPONSE_LOGIN)
            sr = gson.fromJson(json, ServerLoginResponse.class);
        else if (type == ServerResponse._RESPONSE_MODULES)
            sr = gson.fromJson(json, ServerModulesRequestResponse.class);
        else if (type == ServerResponse._RESPONSE_SIMPLE_VALUES)
            sr = gson.fromJson(json, ServerSimpleValuesResponse.class);
        else if (type == ServerResponse._RESPONSE_SQL)
            sr = gson.fromJson(json, ServerSQLResponse.class);
        else if (type == ServerResponse._RESPONSE_VALUE_ENHANCERS)
            sr = gson.fromJson(json, ServerValueEnhancersRequestResponse.class);
        else if (type == ServerResponse._RESPONSE_MODULE_SETTINGS)
            sr = gson.fromJson(json, ServerModulesSettingsResponse.class);
        else if (type == ServerResponse._RESPONSE_ATTACHMENT_ACTION)
            sr = gson.fromJson(json, ServerAttachmentActionResponse.class);
        else if (type == ServerResponse._RESPONSE_ATTACHMENTS_LIST)
            sr = gson.fromJson(json, ServerAttachmentsListResponse.class);
        else if (type == ServerResponse._RESPONSE_PICTURE_ACTION)
            sr = gson.fromJson(json, ServerPictureActionResponse.class);
        else if (type == ServerResponse._RESPONSE_PICTURES_LIST)
            sr = gson.fromJson(json, ServerPicturesListResponse.class);
        else if (type == ServerResponse._RESPONSE_PICTURE_SAVE_ACTION)
            sr = gson.fromJson(json, ServerPictureSaveActionResponse.class);
        else
            logger.fatal("No server response implementation found for type [" + type + "]");
        
        return sr;
    }
    
    public ClientRequest deserializeClientRequest(ObjectInputStream is) throws IOException, ClassNotFoundException {
        String json = getJson(is);
        Gson gson = getGson();
        
        ClientRequest cr = gson.fromJson(json, ClientRequest.class);
        int type = cr.getType();
        
        if (type == ClientRequest._REQUEST_APPLICATION_SETTINGS)
            cr = gson.fromJson(json, ClientRequestApplicationSettings.class);
        else if (type == ClientRequest._REQUEST_EXECUTE_SQL)
            cr = gson.fromJson(json, ClientRequestExecuteSQL.class);
        else if (type == ClientRequest._REQUEST_ITEM)
            cr = gson.fromJson(json, ClientRequestItem.class);  
        else if (type == ClientRequest._REQUEST_ITEM_ACTION)
            cr = gson.fromJson(json, ClientRequestItemAction.class);  
        else if (type == ClientRequest._REQUEST_ITEM_KEYS)
            cr = gson.fromJson(json, ClientRequestItemKeys.class);
        else if (type == ClientRequest._REQUEST_ITEMS)
            cr = gson.fromJson(json, ClientRequestItems.class);  
        else if (type == ClientRequest._REQUEST_LOGIN)
            cr = gson.fromJson(json, ClientRequestLogin.class); 
        else if (type == ClientRequest._REQUEST_MODULES)
            cr = gson.fromJson(json, ClientRequestModules.class);
        else if (type == ClientRequest._REQUEST_REFERENCING_ITEMS)
            cr = gson.fromJson(json, ClientRequestReferencingItems.class);         
        else if (type == ClientRequest._REQUEST_SIMPLE_VALUES)
            cr = gson.fromJson(json, ClientRequestSimpleValues.class); 
        else if (type == ClientRequest._REQUEST_VALUE_ENHANCERS_SETTINGS)
            cr = gson.fromJson(json, ClientRequestValueEnhancers.class); 
        else if (type == ClientRequest._REQUEST_MODULE_SETTINGS)
            cr = gson.fromJson(json, ClientRequestModuleSettings.class);
        else if (type == ClientRequest._REQUEST_REMOVE_REFERENCES_TO)
            cr = gson.fromJson(json, ClientRequestRemoveReferenceTo.class);
        else if (type == ClientRequest._REQUEST_ATTACHMENT_ACTION)
            cr = gson.fromJson(json, ClientRequestAttachmentAction.class);
        else if (type == ClientRequest._REQUEST_ATTACHMENTS_DELETE)
            cr = gson.fromJson(json, ClientRequestAttachmentsDelete.class);
        else if (type == ClientRequest._REQUEST_ATTACHMENTS_LIST)
            cr = gson.fromJson(json, ClientRequestAttachmentsList.class);
        else if (type == ClientRequest._REQUEST_PICTURE_ACTION)
            cr = gson.fromJson(json, ClientRequestPictureAction.class);
        else if (type == ClientRequest._REQUEST_PICTURES_DELETE)
            cr = gson.fromJson(json, ClientRequestPicturesDelete.class);
        else if (type == ClientRequest._REQUEST_PICTURES_LIST)
            cr = gson.fromJson(json, ClientRequestPicturesList.class);
        else if (type == ClientRequest._REQUEST_PICTURE_ORDER)
            cr = gson.fromJson(json, ClientRequestSavePictureOrder.class);
        else
            logger.fatal("No client request implementation found for type [" + type + "]");
            
        return cr;
    }
}
