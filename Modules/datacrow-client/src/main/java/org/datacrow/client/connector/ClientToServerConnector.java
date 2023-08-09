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

package org.datacrow.client.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacrow.client.console.GUI;
import org.datacrow.client.console.windows.security.LoginDialog;
import org.datacrow.client.tabs.Tabs;
import org.datacrow.core.DcConfig;
import org.datacrow.core.attachments.Attachment;
import org.datacrow.core.console.IMasterView;
import org.datacrow.core.console.IView;
import org.datacrow.core.data.DataFilter;
import org.datacrow.core.data.DataFilterEntry;
import org.datacrow.core.data.DataFilters;
import org.datacrow.core.data.DcIconCache;
import org.datacrow.core.data.DcResultSet;
import org.datacrow.core.data.Operator;
import org.datacrow.core.drivemanager.DriveManager;
import org.datacrow.core.enhancers.IValueEnhancer;
import org.datacrow.core.enhancers.ValueEnhancers;
import org.datacrow.core.filerenamer.FilePatterns;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.modules.security.PermissionModule;
import org.datacrow.core.modules.security.UserModule;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcSimpleValue;
import org.datacrow.core.objects.Loan;
import org.datacrow.core.objects.Picture;
import org.datacrow.core.objects.ValidationException;
import org.datacrow.core.objects.helpers.User;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.server.Connector;
import org.datacrow.core.server.DcServerConnection;
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
import org.datacrow.core.server.requests.ClientRequestReferencingItems;
import org.datacrow.core.server.requests.ClientRequestRemoveReferenceTo;
import org.datacrow.core.server.requests.ClientRequestSimpleValues;
import org.datacrow.core.server.requests.ClientRequestUser;
import org.datacrow.core.server.requests.ClientRequestValueEnhancers;
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
import org.datacrow.core.server.response.ServerResponse;
import org.datacrow.core.server.response.ServerSQLResponse;
import org.datacrow.core.server.response.ServerSimpleValuesResponse;
import org.datacrow.core.server.response.ServerValueEnhancersRequestResponse;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.settings.Setting;
import org.datacrow.core.settings.Settings;
import org.datacrow.core.utilities.SystemMonitor;
import org.datacrow.core.wf.tasks.DcTask;

public class ClientToServerConnector extends Connector {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ClientToServerConnector.class.getName());
	
	private static final ClientToServerConnector si = new ClientToServerConnector();
	
	private Collection<DcServerConnection> connections = new ArrayList<DcServerConnection>();
	
	private SecuredUser su;
	
	public static ClientToServerConnector getInstance() {
		return si;
	}
	
	private ClientToServerConnector() {
		super();
	}
	
	@Override
	public DcServerConnection getServerConnection() throws Exception {
		
		cleanup();
	    
        for (DcServerConnection connection : connections) {
            try {
                if (connection.isActive() && connection.isAvailable())
                    return connection;
            } catch (Exception e) {
                logger.error("An unexpected error occurred while checking existing connections. Creating a new connection", e);
                if (connection != null) connections.remove(connection);
            }
        }
	    
	    DcServerConnection connection = new DcServerConnection(getServerAddress(), getApplicationServerPort());
	    connections.add(connection);
	    return connection;
	}
	
	@Override
	public void initialize() {
	    try {
            // the following are required for the login operation to be able to succeed:
            DcModules.register(new PermissionModule());
            DcModules.register(new UserModule());
	        
            login(getUsername(), getPassword());
            
            DcModules.load();
            
            DataFilters.load();
            FilePatterns.load();
            ValueEnhancers.initialize();
            
            loadApplicationSettings();
            
            SystemMonitor monitor = new SystemMonitor();
            monitor.start();
            
        } catch (Exception e) {
            logger.error(e, e);
        }
	}
	
	/**
	 * Remove all disconnected connections
	 */
	private void cleanup() {
		Collection<DcServerConnection> remove = new ArrayList<DcServerConnection>();
	    for (DcServerConnection connection : connections) {
	        if (!connection.isActive()) {
	        	connection.disconnect();
	        	remove.add(connection);
	        }
	    }

	    connections.removeAll(remove);
	}
	
	/**
	 * Handles the actual client request to the server. Requests can vary between login, data and task execution requests.
	 * Each time a request is made to the server a thread is started on the server. 
	 * This connector will wait for the response to come through.
	 *  
	 * @param cr the client request, containing all the information to process the request on the server
	 * @return the response from the server
	 */
	private ServerResponse processClientRequest(ClientRequest cr) {
	    ServerResponse sr = null;

		try {
			ClientRequestHandler handler = new ClientRequestHandler(cr);
			sr = handler.process();
			
			if (sr instanceof ServerErrorResponse) {
				ServerErrorResponse ser = (ServerErrorResponse) sr;
				logger.error("The server has encountered an error while processing the request: " + 
						ser.getMessage(), ser.getErrorMessage());
				
				GUI.getInstance().displayErrorMessage(ser.getErrorMessage());
				
				// to avoid ClassCastExceptions.
				sr = null;
			}

		} catch (IOException e) {
			logger.error("Error while sending the request " + cr + " to " + serverAddress + ":" + applicationServerPort +
					". Most likely the server is down. Please check with your server administrator.", e);
		} catch (ClassNotFoundException e) {
			logger.error("Error while sending the request " + cr + " to " + serverAddress + ":" + applicationServerPort + 
					". Most likely the server and client version are in conflict.", e);
		}
		
		return sr;
	}
	
	private void loadApplicationSettings() {
        try {
            ClientRequestApplicationSettings cras = new ClientRequestApplicationSettings(su);
            ServerApplicationSettingsRequestResponse response = (ServerApplicationSettingsRequestResponse) processClientRequest(cras);

            if (response == null) return;
            
            Settings settings = response.getSettings();
            for (Setting setting : settings.getSettings()) {
                if (DcSettings.getSetting(setting.getKey()).isReadonly())
                    DcSettings.set(setting.getKey(), setting.getValue());
            }
         } catch (Exception e) {
             logger.error("Unable to retrieve the settings from the server", e);
         }
	}
	
	@Override
	public SecuredUser getUser() {
		return su;
	}
	
	@Override
    public void deleteModule(int moduleIdx) {
        logger.error("User requests to delete module while this is not allowed.");
    }

	@Override
	public SecuredUser login(String username, String password) {
        boolean success = false;
        int retry = 0;
        
        GUI.getInstance().showSplashScreen(false);
        while (!success && retry < 3) {
                               
            LoginDialog dlg = new LoginDialog();
            GUI.getInstance().openDialogNativeModal(dlg);
            if (dlg.isCanceled()) break;
            
            ClientRequest cr = new ClientRequestLogin(dlg.getLoginName(), dlg.getPassword());
            ServerLoginResponse response = (ServerLoginResponse) processClientRequest(cr);
            
            // register this user as the logged in user
            if (response != null)
                su = response.getUser();
            
            success = su != null;
            retry++;
        }
        
        if (!success) {
            System.exit(0);
        } else {
            GUI.getInstance().showSplashScreen(true);
		}
		return su;
	}
	
	@Override
	public List<DcSimpleValue> getSimpleValues(int module, boolean includeIcons) {
        ClientRequestSimpleValues cr = new ClientRequestSimpleValues(getUser(), module, includeIcons);
        ServerSimpleValuesResponse response = (ServerSimpleValuesResponse) processClientRequest(cr);
        return response != null ? response.getValues() : null;
	}
	
	@Override
	public Collection<Picture> getPictures(String parentID) {
		ClientRequestItems cr = new ClientRequestItems(getUser());
        DataFilter df = new DataFilter(DcModules._PICTURE);
        df.addEntry(new DataFilterEntry(DcModules._PICTURE, Picture._A_OBJECTID, Operator.EQUAL_TO, parentID));
		cr.setDataFilter(df);
	      
		ServerItemsRequestResponse response = (ServerItemsRequestResponse) processClientRequest(cr);
		
		Collection<Picture> pictures = null;
		if (response != null && response.getItems() != null) {
		    pictures = new ArrayList<Picture>();
		    for (DcObject dco : response.getItems()) {
		        pictures.add((Picture) dco);
		    }
		}
		
		return pictures;
	}
	
	@Override
    public Map<DcField, Collection<IValueEnhancer>> getValueEnhancers() {
        ClientRequestValueEnhancers crve = new ClientRequestValueEnhancers(DcConfig.getInstance().getConnector().getUser());
        ServerValueEnhancersRequestResponse response = (ServerValueEnhancersRequestResponse) processClientRequest(crve);
        return response != null ? response.getEnhancers() : null;
    }

    @Override
    public void removeReferencesTo(int moduleIdx, String ID) {
    	ClientRequestRemoveReferenceTo cr = new ClientRequestRemoveReferenceTo(su, moduleIdx, ID);
    	processClientRequest(cr);
    }
	
    @Override
	public Collection<DcObject> getReferences(int mappingModuleIdx, String parentKey, boolean full) {
		ClientRequestItems cr = new ClientRequestItems(getUser());
		
		DataFilter df = new DataFilter(mappingModuleIdx);
		df.addEntry(new DataFilterEntry(mappingModuleIdx, DcMapping._A_PARENT_ID, Operator.EQUAL_TO, parentKey));
		
		cr.setDataFilter(df);
		cr.setFields(full ? null : DcModules.get(mappingModuleIdx).getMinimalFields(null));
	      
		ServerItemsRequestResponse response = (ServerItemsRequestResponse) processClientRequest(cr);
		return response != null ? response.getItems() : null;
	}
	
    @Override
	public Map<String, Integer> getChildrenKeys(String parentKey, int childModuleIdx) {
    	ClientRequestItemKeys cr = new ClientRequestItemKeys(getUser());
        DataFilter df = new DataFilter(childModuleIdx);
        DcModule module = DcModules.get(childModuleIdx);
        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, childModuleIdx, module.getParentReferenceFieldIndex(), Operator.EQUAL_TO, parentKey));
        cr.setDataFilter(df);
    	
        ServerItemKeysRequestResponse response = (ServerItemKeysRequestResponse) processClientRequest(cr);
		return response != null ? response.getItems() : null;
    }
	
    @Override
	public Collection<DcObject> getChildren(String parentKey, int childModuleIdx, int[] fields) {
    	ClientRequestItems cr = new ClientRequestItems(getUser());
    	cr.setFields(fields);
    	
        DataFilter df = new DataFilter(childModuleIdx);
        DcModule module = DcModules.get(childModuleIdx);
        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, childModuleIdx, module.getParentReferenceFieldIndex(), Operator.EQUAL_TO, parentKey));
        cr.setDataFilter(df);
    	
		ServerItemsRequestResponse response = (ServerItemsRequestResponse) processClientRequest(cr);
		return response != null ? response.getItems() : null;
    }
    
    @Override
	public Loan getCurrentLoan(String parentKey) {
    	ClientRequestItems cr = new ClientRequestItems(getUser());
        DataFilter df = new DataFilter(DcModules._LOAN);
        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._B_ENDDATE, Operator.IS_EMPTY, null));
        df.addEntry(new DataFilterEntry(DataFilterEntry._AND, DcModules._LOAN, Loan._D_OBJECTID, Operator.EQUAL_TO, parentKey));
        df.setResultLimit(1);
        cr.setDataFilter(df);
        
		ServerItemsRequestResponse response = (ServerItemsRequestResponse) processClientRequest(cr);
		Loan loan = null;
		if (response != null) {
			List<DcObject> items = response.getItems();
			loan = items.size() > 0 ? (Loan) items.get(0) : null;
		}
        
		return loan == null ? new Loan() : loan;
    }
    
    @Override
	public List<DcObject> getLoans(String parentKey) {
    	ClientRequestItems cr = new ClientRequestItems(getUser());
        DataFilter df = new DataFilter(DcModules._LOAN);
        df.addEntry(new DataFilterEntry(DcModules._LOAN, Loan._D_OBJECTID, Operator.EQUAL_TO, parentKey));
        cr.setDataFilter(df);
		ServerItemsRequestResponse response = (ServerItemsRequestResponse) processClientRequest(cr);
		return response != null ? response.getItems() : null;
    }
	
	@Override
	public DcObject getItem(int moduleIdx, String key) {
		return getItem(moduleIdx, key, null);
	}
	
	@Override
	public DcObject getItem(int moduleIdx, String key, int[] fields) {
		ClientRequestItem cr = new ClientRequestItem(getUser(), ClientRequestItem._SEARCHTYPE_BY_ID, moduleIdx, key);
		cr.setFields(fields);
		
		ServerItemRequestResponse response = (ServerItemRequestResponse) processClientRequest(cr);
		return response != null ? response.getItem() : null;
    }
	
    @Override
    public List<DcObject> getItems(int moduleIdx, int[] fields) {
        return getItems(new DataFilter(moduleIdx), fields);
    }
	
	@Override
	public DcObject getItemByKeyword(int module, String keyword) {
		ClientRequestItem cr = new ClientRequestItem(
				getUser(), 
				ClientRequestItem._SEARCHTYPE_BY_KEYWORD, 
				module, 
				keyword);
		ServerItemRequestResponse response = (ServerItemRequestResponse) processClientRequest(cr);
		return response != null ? response.getItem() : null;
	}

	@Override
	public DcObject getItemByExternalID(int module, String keyType, String keyword) {
		ClientRequestItem cr = new ClientRequestItem(
				getUser(), 
				ClientRequestItem._SEARCHTYPE_BY_EXTERNAL_ID, 
				module, 
				keyword);
		
		cr.setExternalKeyType(keyType);
		
		ServerItemRequestResponse response = (ServerItemRequestResponse) processClientRequest(cr);
		return response != null ? response.getItem() : null;
	}
	
	@Override
	public DcObject getItemByUniqueFields(DcObject dco) {
		ClientRequestItem cr = new ClientRequestItem(
				getUser(), 
				ClientRequestItem._SEARCHTYPE_BY_UNIQUE_FIELDS, 
				dco.getModule().getIndex(), 
				dco);
		ServerItemRequestResponse response = (ServerItemRequestResponse) processClientRequest(cr);
		return response != null ? response.getItem() : null;
	}
	
	@Override
	public DcObject getItemByDisplayValue(int moduleIdx, String displayValue) {
		ClientRequestItem cr = new ClientRequestItem(
				getUser(), 
				ClientRequestItem._SEARCHTYPE_BY_DISPLAY_VALUE, 
				moduleIdx, 
				displayValue);
		ServerItemRequestResponse response = (ServerItemRequestResponse) processClientRequest(cr);
		return response != null ? response.getItem() : null;
	}
	
    @Override
    public HashMap<Integer, Settings> getModuleSettings() {
        ClientRequestModuleSettings crms = new ClientRequestModuleSettings(su);
        ServerModulesSettingsResponse response = (ServerModulesSettingsResponse) processClientRequest(crms);
        return response != null ? response.getSettings() : null;
        
    }	
	
	@Override
	public Map<String, Integer> getKeys(DataFilter df) {
    	ClientRequestItemKeys cr = new ClientRequestItemKeys(getUser());
        cr.setDataFilter(df);
        ServerItemKeysRequestResponse response = (ServerItemKeysRequestResponse) processClientRequest(cr);
		return response != null ? response.getItems() : null;
	}
	
	@Override
	public List<DcObject> getItems(DataFilter df) {
		return getItems(df, null);
	}

	@Override
	public List<DcObject> getItems(DataFilter df, int[] fields) {
    	ClientRequestItems cr = new ClientRequestItems(getUser());
    	cr.setFields(fields);
        cr.setDataFilter(df);
        ServerItemsRequestResponse response = (ServerItemsRequestResponse) processClientRequest(cr);
		return response != null ? response.getItems() : null;
	}
	
	@Override
	public void createUser(User user, String password) {
    	ClientRequestUser cr = new ClientRequestUser(ClientRequestUser._ACTIONTYPE_CREATE, getUser(), user, password);
        processClientRequest(cr);
	}

	@Override
	public void changePassword(User user, String password) {
    	ClientRequestUser cr = new ClientRequestUser(ClientRequestUser._ACTIONTYPE_CHANGEPASSWORD, getUser(), user, password);
        processClientRequest(cr);
	}
	
	@Override
	public void updateUser(User user) {
    	ClientRequestUser cr = new ClientRequestUser(ClientRequestUser._ACTIONTYPE_UPDATE, getUser(), user, null);
        processClientRequest(cr);
	}
	
	@Override
	public void dropUser(User user) {
    	ClientRequestUser cr = new ClientRequestUser(ClientRequestUser._ACTIONTYPE_DROP, getUser(), user, null);
        processClientRequest(cr);
	}
	
	@Override
	public Collection<Attachment> getAttachmentsList(String objectID) {
		ClientRequestAttachmentsList cr = new ClientRequestAttachmentsList(su, objectID);
		ServerAttachmentsListResponse response = (ServerAttachmentsListResponse) processClientRequest(cr);
		return response.getAttachments();
	}

	@Override
	public void deleteAttachments(String ObjectID) {
		ClientRequestAttachmentsDelete cr = new ClientRequestAttachmentsDelete(su, ObjectID);
		processClientRequest(cr);	
	}
	
	@Override
	public void deleteAttachment(Attachment attachment) {
		ClientRequestAttachmentAction cr = new ClientRequestAttachmentAction(
				su, 
				ClientRequestAttachmentAction._ACTION_DELETE_ATTACHMENT,
				attachment);
		processClientRequest(cr);
	}

	@Override
	public void saveAttachment(Attachment attachment) {
		ClientRequestAttachmentAction cr = new ClientRequestAttachmentAction(
				su, 
				ClientRequestAttachmentAction._ACTION_SAVE_ATTACHMENT,
				attachment);
		processClientRequest(cr);		
	}

	@Override
	public void loadAttachment(Attachment attachment) {
		ClientRequestAttachmentAction cr = new ClientRequestAttachmentAction(
				su, 
				ClientRequestAttachmentAction._ACTION_LOAD_ATTACHMENT,
				attachment);
		ServerAttachmentActionResponse response = (ServerAttachmentActionResponse) processClientRequest(cr);
		attachment.setData(response.getAttachment().getData());
	}
	
    @Override
    public DcResultSet executeSQL(String sql) {
        ClientRequestExecuteSQL csr = new ClientRequestExecuteSQL(su, sql);
        ServerResponse response = processClientRequest(csr);
        
        DcResultSet result = null;
        if (response != null) {
            ServerSQLResponse ssr = (ServerSQLResponse) response;
            result = ssr.getResult();
        }
        
        return result == null ? new DcResultSet() : result;
    }
    
	@Override
	public boolean deleteItem(DcObject dco) throws ValidationException {
	    ClientRequestItemAction cr = new ClientRequestItemAction(
	            getUser(), ClientRequestItemAction._ACTION_DELETE, dco);
	    ServerResponse response = processClientRequest(cr);
	    
	    boolean success = false;
        if (response != null) {
            ServerActionResponse sar = (ServerActionResponse) response;
            success = sar.isSuccess();
        }
	    
		return success;
	}

	@Override
	public boolean saveItem(DcObject dco) throws ValidationException {
        ClientRequestItemAction cr = new ClientRequestItemAction(
                getUser(), ClientRequestItemAction._ACTION_SAVE, dco);
        
        // make sure to load the bytes as the image inside the ImageIcon will not be available on the server.
        dco.loadImageData();
        
        ServerResponse response = processClientRequest(cr);
        
        boolean success = false;
        if (response != null) {
            ServerActionResponse sar = (ServerActionResponse) response;
            success = sar.isSuccess();
        }
        
        return success;	
	}

	@Override
	public void close() {
	}

	@Override
	public int getCount(int module, int field, Object value) {
		return 0;
	}

	@Override
	public List<DcObject> getReferencingItems(int moduleIdx, String ID) {
		ClientRequestReferencingItems cri = new ClientRequestReferencingItems(su, moduleIdx, ID);
        ServerItemsRequestResponse response = (ServerItemsRequestResponse) processClientRequest(cri);
        return response != null ? response.getItems() : null;
	}

	@Override
	public boolean checkUniqueness(DcObject dco, boolean exitingItem) {
		return true;
	}

	@Override
	public void executeTask(DcTask task) {
	    // We start the thread instead of sending it to the server.
	    // The started task will use the connector again to save / delete the items (where applicable) 
	    // and will call the server within the thread. At completion the necessary requests are handled.
        Thread thread = new Thread(task);
        thread.start();
	}

    @Override
    public ServerModulesRequestResponse getModules() {
    	ClientRequestModules crm = new ClientRequestModules(su);
        ServerModulesRequestResponse response = (ServerModulesRequestResponse) processClientRequest(crm);
        return response;
    }

    @Override
    public void shutdown(boolean checkForChanges) {
        DcConfig dcc = DcConfig.getInstance();
        dcc.getClientSettings().setUiScaling();
        dcc.getClientSettings().save();
        dcc.getConnector().close();

        DriveManager.getInstance().stopScanners();
        DriveManager.getInstance().stopDrivePoller();
        DriveManager.getInstance().stopFileSynchronizer();
        
        logger.info(DcResources.getText("msgApplicationStops"));
        
        if (checkForChanges) {
            boolean unsavedChanges = false;
            for (IMasterView mv : GUI.getInstance().getViews()) {
                mv.saveSettings();
                for (IView view : mv.getViews()) {
                    if (!view.isChangesSaved())
                        unsavedChanges = true;
                }
            }
            
            if (unsavedChanges && !GUI.getInstance().displayQuestion("msgCancelExitAndSave"))
                return;
        }
        
        Tabs.getInstance().save();
        
        DcIconCache.getInstance().deleteIcons();
        
        DataFilters.save();
        FilePatterns.save();
        DcSettings.save();
        DcModules.save();
        
        GUI.getInstance().getMainFrame().setVisible(false);

        System.exit(0);
    }
    
    @Override
    public void setUser(SecuredUser su) {
    	this.su = su;
    }
    
    @Override
    public Connector clone() {
    	ClientToServerConnector ctsc = new ClientToServerConnector();
    	ctsc.setApplicationServerPort(getApplicationServerPort());
    	ctsc.setImageServerPort(getImageServerPort());
    	ctsc.setPassword(getPassword());
    	ctsc.setUser(getUser());
    	ctsc.setUsername(getUsername());
    	return ctsc;
    }
}
