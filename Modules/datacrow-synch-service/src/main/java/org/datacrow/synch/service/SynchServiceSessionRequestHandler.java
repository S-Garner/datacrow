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

package org.datacrow.synch.service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.datacrow.core.DcConfig;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.server.Connector;
import org.datacrow.core.server.requests.ClientRequestLogin;
import org.datacrow.core.server.requests.ClientRequestUser;
import org.datacrow.core.server.requests.IClientRequest;
import org.datacrow.core.server.response.IServerResponse;
import org.datacrow.core.server.response.ServerLoginResponse;
import org.datacrow.core.server.serialization.SerializationHelper;
import org.datacrow.server.LocalServerConnector;
import org.datacrow.server.security.SecurityCenter;

public class SynchServiceSessionRequestHandler extends Thread {
		
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(SynchServiceSessionRequestHandler.class.getName());
	
	protected Socket socket;
	protected boolean canceled = false;
	
	protected Connector context;
	protected IClientRequest cr;
	
	protected final SynchServiceSession session;
	
	public SynchServiceSessionRequestHandler(SynchServiceSession session) {
		this.session = session;

		// copy the current context and reset the user
		this.context = DcConfig.getInstance().getConnector().clone();
		this.context.setUser(null);
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
		
		ObjectInputStream is = null;
		ObjectOutputStream os = null;
		
		try {
	        os = new ObjectOutputStream(socket.getOutputStream());
	        is = new ObjectInputStream(socket.getInputStream());

            context = new LocalServerConnector();
            
            while (!socket.isClosed()) {
                try {
                    cr = SerializationHelper.getInstance().deserializeClientRequest(is);
                    
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
	
	private IServerResponse processLoginRequest(ClientRequestLogin lr) {
		SecuredUser su = context.login(lr.getUsername(), lr.getPassword());
		return new ServerLoginResponse(su);
	}	
	
	/**
	 * Processes an request. The type of the request is checked before type casting.
	 * @throws Exception
	 */
	private void processRequest(ObjectOutputStream os) throws Exception {
        try {
            IServerResponse sr = null;
	        switch (cr.getType()) {
	        case SynchServiceClientRequest._REQUEST_LOGIN:
	        	sr = processLoginRequest((ClientRequestLogin) cr);
	        	break;	        
            case SynchServiceClientRequest._REQUEST_MODULES:
//                sr = processModulesRequest((ClientRequestModules) cr);
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
}
