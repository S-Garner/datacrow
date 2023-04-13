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
import org.datacrow.server.security.SecurityCenter;
import org.datacrow.synch.service.request.ServiceRequest;
import org.datacrow.synch.service.request.ServiceRequestType;
import org.datacrow.synch.service.response.ServiceLoginResponse;
import org.datacrow.synch.service.response.ServiceResponse;
import org.datacrow.synch.service.serialization.ServiceSerializationHelper;

public class SynchServiceSessionRequestHandler extends Thread {
		
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(SynchServiceSessionRequestHandler.class.getName());
	
	protected Socket socket;
	protected boolean canceled = false;
	
	protected Connector context;
	protected ServiceRequest request;
	
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

            while (!socket.isClosed()) {
                try {
                	request = ServiceSerializationHelper.getInstance().deserializeClientRequest(is);
					SecuredUser su = SecurityCenter.getInstance().login(
							request.getClientKey(),
							request.getUsername(),
							request.getPassword());
					context.setUser(su);
                    
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
		    logger.error("Error while processing request " + request + " for client " + (request != null ? request.getClientKey() : " null"), e);
		} finally {
        	try {
        		if (request != null) request.close();
        	} catch (Exception e) {
        	    logger.debug("An error occured while closing resources", e);
        	}
        }
    }
	
	private ServiceResponse processLoginRequest(ServiceRequest request) {
		SecuredUser su = context.login(request.getUsername(), request.getPassword());
		return new ServiceLoginResponse(su.getUser().getID());
	}	
	
	/**
	 * Processes an request. The type of the request is checked before type casting.
	 * @throws Exception
	 */
	private void processRequest(ObjectOutputStream os) throws Exception {
        try {
        	ServiceResponse response = null;
	        switch (request.getType()) {
	        case LOGIN_REQUEST:
	        	response = processLoginRequest(request);
	        	break;	        
            default:
                logger.error("No handler found for " + request);
	        }
	        
	        if (response != null) {
	            String json = ServiceSerializationHelper.getInstance().serialize(response);
	            os.writeObject(json);
	            os.flush();
		        
		        logger.debug("Send object to client");
	        } else {
	        	logger.error("Could not complete the request. The request type was unknown to the server. " + request);
	        }
        } catch (IOException ioe) {
        	logger.error("Communication error between server and client", ioe);
        } 
	}
}
