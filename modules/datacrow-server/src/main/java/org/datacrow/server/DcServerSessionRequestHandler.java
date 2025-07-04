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

package org.datacrow.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.datacrow.core.attachments.Attachment;
import org.datacrow.core.data.DcResultSet;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcSimpleValue;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.server.requests.ClientRequest;
import org.datacrow.core.server.requests.ClientRequestApplicationSettings;
import org.datacrow.core.server.requests.ClientRequestAttachmentAction;
import org.datacrow.core.server.requests.ClientRequestAttachmentsDelete;
import org.datacrow.core.server.requests.ClientRequestAttachmentsList;
import org.datacrow.core.server.requests.ClientRequestDeleteChildren;
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
import org.datacrow.core.server.requests.ClientRequestUser;
import org.datacrow.core.server.requests.ClientRequestValueEnhancers;
import org.datacrow.core.server.requests.IClientRequest;
import org.datacrow.core.server.response.DefaultServerResponse;
import org.datacrow.core.server.response.IServerResponse;
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
import org.datacrow.core.server.serialization.SerializationHelper;
import org.datacrow.server.security.SecurityCenter;

public class DcServerSessionRequestHandler extends Thread {
		
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DcServerSessionRequestHandler.class.getName());
	
	protected Socket socket;
	protected boolean canceled = false;
	
	protected LocalServerConnector context;
	protected IClientRequest cr;
	
	protected final DcServerSession session;
	
	public DcServerSessionRequestHandler(DcServerSession session) {
		this.session = session;
	} 
	
	protected void cancel() {
		canceled = true;
	}
	
	protected boolean isCanceled() {
		return canceled;
	}
	
	@Override
    public void run() {
		if (isCanceled()) return;
        
		this.socket = session.getSocket();
		
		try {
			socket.setReceiveBufferSize(128000);
			socket.setSendBufferSize(128000);
        
	        // set a socket timeout
	        // socket.setSoTimeout(2000);
	        // ping every 2 hours
	        socket.setKeepAlive(true);
		} catch (Exception e) {
			logger.warn("Failed to set socket properties", e);
		}
		
        ObjectInputStream is = null;
		ObjectOutputStream os = null;
		
		try {
	        os = new ObjectOutputStream(socket.getOutputStream());
	        is = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
	        
            context = new LocalServerConnector();
            
            while (!socket.isClosed()) {
                try {
                    cr = SerializationHelper.getInstance().deserializeClientRequest(is);
                    
                    // check if we can login with the user of the request
					if (	!(cr instanceof ClientRequestLogin) && 
							!(cr instanceof ClientRequestUser)) {
						
						SecuredUser su = SecurityCenter.getInstance().login(
								cr.getClientKey(),
								cr.getUsername(),
								cr.getPassword());

						context.setUser(su);
					}    
                    
                    processRequest(os);
                } catch (IOException e) {
                    logger.info("Client session has been ended (" + socket.getInetAddress() + ")");
                    socket.close();
                } catch (ClassNotFoundException e) {
                    logger.error(e, e);
                    socket.close();
                }
            }
		} catch (Exception e) {
		    logger.error("Error while processing request " + cr + " for client " + (cr != null ? cr.getClientKey() : " null"), e);
		} finally {
        	try {
        		if (cr != null) cr.close();
        	} catch (Exception e) {
        	    logger.debug("An error occured while closing resources", e);
        	}
        }
    }
	
	/**
	 * Processes an request. The type of the request is checked before type casting.
	 * @throws Exception
	 */
	private void processRequest(ObjectOutputStream os) throws Exception {
        try {
            IServerResponse sr = null;
	        switch (cr.getType()) {
	        case ClientRequest._REQUEST_ITEMS:
	        	sr = processItemsRequest((ClientRequestItems) cr);
	        	break;
	        case ClientRequest._REQUEST_ITEM:
	        	sr = processItemRequest((ClientRequestItem) cr);
	        	break;
	        case ClientRequest._REQUEST_ITEM_ACTION:
                sr = processItemActionRequest((ClientRequestItemAction) cr);
                break;
	        case ClientRequest._REQUEST_LOGIN:
	        	sr = processLoginRequest((ClientRequestLogin) cr);
	        	break;
	        case ClientRequest._REQUEST_ITEM_KEYS:
                sr = processItemKeysRequest((ClientRequestItemKeys) cr);
                break;
            case ClientRequest._REQUEST_EXECUTE_SQL:
                sr = processSQLRequest((ClientRequestExecuteSQL) cr);
                break;
            case ClientRequest._REQUEST_REFERENCING_ITEMS:
                sr = processReferencingItemsRequest((ClientRequestReferencingItems) cr);
                break;
            case ClientRequest._REQUEST_SIMPLE_VALUES:
                sr = processSimpleValuesRequest((ClientRequestSimpleValues) cr);
                break;
            case ClientRequest._REQUEST_MODULES:
                sr = processModulesRequest((ClientRequestModules) cr);
                break;
            case ClientRequest._REQUEST_APPLICATION_SETTINGS:
                sr = processApplicationSettingsRequest((ClientRequestApplicationSettings) cr);
                break;
            case ClientRequest._REQUEST_VALUE_ENHANCERS_SETTINGS:
                sr = processValueEnhancersRequest((ClientRequestValueEnhancers) cr);
                break;
            case ClientRequest._REQUEST_USER_MGT:
                sr = processUserManagementAction((ClientRequestUser) cr);
                break;
            case ClientRequest._REQUEST_MODULE_SETTINGS:
                sr = processModuleSettingsRequest((ClientRequestModuleSettings) cr);
                break;
            case ClientRequest._REQUEST_REMOVE_REFERENCES_TO:
                sr = processRemoveReferenceToRequest((ClientRequestRemoveReferenceTo) cr);
                break;
            case ClientRequest._REQUEST_ATTACHMENT_ACTION:
                sr = processAttachmentActionRequest((ClientRequestAttachmentAction) cr);
                break;
            case ClientRequest._REQUEST_ATTACHMENTS_LIST:
                sr = processListAttachmentsRequest((ClientRequestAttachmentsList) cr);
                break;
            case ClientRequest._REQUEST_ATTACHMENTS_DELETE:
                sr = processDeleteAttachmentsRequest((ClientRequestAttachmentsDelete) cr);
                break;
            case ClientRequest._REQUEST_PICTURE_ACTION:
                sr = processPictureActionRequest((ClientRequestPictureAction) cr);
                break;
            case ClientRequest._REQUEST_PICTURES_LIST:
                sr = processListPicturesRequest((ClientRequestPicturesList) cr);
                break;
            case ClientRequest._REQUEST_PICTURES_DELETE:
                sr = processDeletePicturesRequest((ClientRequestPicturesDelete) cr);
                break;
            case ClientRequest._REQUEST_PICTURE_ORDER:
                sr = processPictureOrderRequest((ClientRequestSavePictureOrder) cr);
                break;
            case ClientRequest._REQUEST_DELETE_CHILDREN:
                sr = processDeleteChildrenRequest((ClientRequestDeleteChildren) cr);
                break;
            default:
                logger.error("No handler found for " + cr);
	        }
	        
	        if (sr != null) {
	            String json = SerializationHelper.getInstance().serialize(sr);
	            os.writeObject(json);
	            os.flush();
		        
		        logger.debug("Send object to client");
	        } else {
	        	logger.error("Could not complete the request. The request type was unknown to the server. " + cr);
	        }
        } catch (IOException ioe) {
        	logger.error("Communication error between server and client", ioe);
        } 
	}
	
    /** 
     * Retrieves items directly from the DataFilter.
     * @throws Exception
     */
    private ServerResponse processUserManagementAction(ClientRequestUser cr) {
        if (cr.getActionType() == ClientRequestUser._ACTIONTYPE_CHANGEPASSWORD) {
            SecurityCenter.getInstance().changePassword(cr.getUser(), cr.getNewPassword());
        } else {
            logger.error("Client Request User action type not supported");
        }
        return new DefaultServerResponse();
    }
	
	/** 
	 * Retrieves items directly from the DataFilter.
	 * @throws Exception
	 */
	private ServerResponse processItemsRequest(ClientRequestItems cr) {
    	List<DcObject> items = context.getItems(cr.getDataFilter(), cr.getFields());
        ServerItemsRequestResponse sr = new ServerItemsRequestResponse(items);
	    return sr;
	}
	
   private IServerResponse processItemKeysRequest(ClientRequestItemKeys cr) {
        Map<String, Integer> items = context.getKeys(cr.getDataFilter());
        ServerItemKeysRequestResponse sr = new ServerItemKeysRequestResponse(items);
        return sr;
    }
	
	private IServerResponse processLoginRequest(ClientRequestLogin lr) {
		SecuredUser su = context.login(lr.getUsername(), lr.getPassword());
		return new ServerLoginResponse(su);
	}
	
	private IServerResponse processSQLRequest(ClientRequestExecuteSQL csr) throws Exception {
	    DcResultSet result = context.executeSQL(csr.getSQL());
        return new ServerSQLResponse(result);
    }
	
    private IServerResponse processReferencingItemsRequest(ClientRequestReferencingItems crri) throws Exception {
        List<DcObject> values = context.getReferencingItems(crri.getModuleIdx(), crri.getID());
        return new ServerItemsRequestResponse(values);
    }
	
    private IServerResponse processSimpleValuesRequest(ClientRequestSimpleValues crsv) throws Exception {
        List<DcSimpleValue> values = context.getSimpleValues(crsv.getModule(), crsv.isIncludeIcons());
        return new ServerSimpleValuesResponse(values);
    }
    
    private ServerResponse processModulesRequest(ClientRequestModules crm) throws Exception {
        return new ServerModulesRequestResponse();
    }
    
    private ServerResponse processApplicationSettingsRequest(ClientRequestApplicationSettings cras) throws Exception {
        return new ServerApplicationSettingsRequestResponse();
    }
    
    private ServerResponse processValueEnhancersRequest(ClientRequestValueEnhancers cras) throws Exception {
        return new ServerValueEnhancersRequestResponse();
    }
    
    private DefaultServerResponse processDeletePicturesRequest(ClientRequestPicturesDelete cr) {
    	context.deleteAttachments(cr.getObjectID());
    	return new DefaultServerResponse();
    }
    
    private DefaultServerResponse processPictureOrderRequest(ClientRequestSavePictureOrder crspo) {
    	context.savePictureOrder(crspo.getObjectID(), crspo.getFiles());
    	return new DefaultServerResponse();
    }
    
    private DefaultServerResponse processDeleteChildrenRequest(ClientRequestDeleteChildren crdc) {
    	context.deleteChildren(crdc.getModuleIdx(), crdc.getParentID());
    	return new DefaultServerResponse();
    }    
    
    private ServerPicturesListResponse processListPicturesRequest(ClientRequestPicturesList cr) {
    	Collection<Picture> pictures = context.getPictures(cr.getObjectID());
    	return new ServerPicturesListResponse(pictures);
    }    

    private ServerResponse processPictureActionRequest(ClientRequestPictureAction cr) {
    	
    	ServerResponse response = new DefaultServerResponse();
    	
    	switch (cr.getActionType()) {
	    	case ClientRequestPictureAction._ACTION_DELETE_PICTURE:
	    		context.deletePicture(cr.getPicture());
	    		response = new ServerPictureActionResponse(cr.getPicture());
	    		break;
	    	case ClientRequestPictureAction._ACTION_SAVE_PICTURE:
	    		boolean saved = context.savePicture(cr.getPicture());
	    		response = new ServerPictureSaveActionResponse(saved);
	    		break;
    	}
    	
    	return response; 
    }      
    
    private DefaultServerResponse processDeleteAttachmentsRequest(ClientRequestAttachmentsDelete cr) {
    	context.deleteAttachments(cr.getObjectID());
    	return new DefaultServerResponse();
    }
    
    private ServerAttachmentsListResponse processListAttachmentsRequest(ClientRequestAttachmentsList cr) {
    	Collection<Attachment> attachments = context.getAttachmentsList(cr.getObjectID());
    	return new ServerAttachmentsListResponse(attachments);
    }    

    private ServerAttachmentActionResponse processAttachmentActionRequest(ClientRequestAttachmentAction cr) {
    	
    	switch (cr.getActionType()) {
	    	case ClientRequestAttachmentAction._ACTION_DELETE_ATTACHMENT:
	    		context.deleteAttachment(cr.getAttachment());
	    		break;
	    	case ClientRequestAttachmentAction._ACTION_SAVE_ATTACHMENT:
	    		context.saveAttachment(cr.getAttachment());
	    		break;
	    	case ClientRequestAttachmentAction._ACTION_LOAD_ATTACHMENT:
	    		context.loadAttachment(cr.getAttachment());
	    		break;
    	}
    	
    	return new ServerAttachmentActionResponse(cr.getAttachment());
    }    
    
    private DefaultServerResponse processRemoveReferenceToRequest(ClientRequestRemoveReferenceTo cr) {
    	context.removeReferencesTo(cr.getModuleIdx(), cr.getId());
        return new DefaultServerResponse();
    } 
    
    private ServerModulesSettingsResponse processModuleSettingsRequest(ClientRequestModuleSettings crms) {
        return new ServerModulesSettingsResponse();
    }     
	   
    private ServerResponse processItemActionRequest(ClientRequestItemAction cr) {
        DcObject dco = cr.getItem();
        
        ServerResponse sr;
        boolean success = false;
        Throwable t = null;
        
        try {
	        if (cr.getAction() == ClientRequestItemAction._ACTION_DELETE) {
	            success = context.deleteItem(dco);
	        } else if (cr.getAction() == ClientRequestItemAction._ACTION_SAVE) {
	            success = context.saveItem(dco);
	        }
        } catch (Exception e) {
            logger.error("Error while executing Item Action", e);
            t = e;
        }
        
        if (!success) {
        	sr = new ServerErrorResponse(t, t.getMessage());
        } else {
        	sr = new ServerActionResponse(success);
        }
        
        return sr;
    }
	
	private IServerResponse processItemRequest(ClientRequestItem cr) {
		DcObject result = null;
		int[] fields = cr.getFields();
		Object value = cr.getValue();
		
		int moduleIdx = cr.getModule();
		
		if (cr.getSearchType() == ClientRequestItem._SEARCHTYPE_BY_ID) {
			result = context.getItem(moduleIdx, (String) value, fields);
		} else if (cr.getSearchType() == ClientRequestItem._SEARCHTYPE_BY_EXTERNAL_ID) {
			result = context.getItemByExternalID(moduleIdx, cr.getExternalKeyType(), (String) value);	
		} else if (cr.getSearchType() == ClientRequestItem._SEARCHTYPE_BY_KEYWORD) {
			result = context.getItemByKeyword(moduleIdx, (String) cr.getValue());	
		} else if (cr.getSearchType() == ClientRequestItem._SEARCHTYPE_BY_UNIQUE_FIELDS) {
			result = context.getItemByUniqueFields((DcObject) cr.getValue());	
        } else if (cr.getSearchType() == ClientRequestItem._SEARCHTYPE_BY_DISPLAY_VALUE) {
            result = context.getItemByDisplayValue(cr.getModule(), (String) cr.getValue());  
        }
		
        ServerItemRequestResponse sr = new ServerItemRequestResponse(result);
	    return sr;
	}
}
