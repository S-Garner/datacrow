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

package org.datacrow.synch;

import java.net.Socket;

import org.apache.logging.log4j.Logger;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.security.SecurityException;
import org.datacrow.core.server.requests.IClientRequest;
import org.datacrow.server.DcServerSession;
import org.datacrow.server.DcServerSessionRequestHandler;
import org.datacrow.server.security.SecurityCenter;

public class SynchServerSession extends DcServerSession {

	protected Socket socket;
	protected DcServerSessionRequestHandler ct;
	
	private transient static Logger logger = 
			DcLogManager.getLogger(SynchServerSession.class.getName());

	public SynchServerSession(Socket socket) {
		this.socket = socket;
		long time = System.currentTimeMillis();
		logger.debug("Client session started: " + time);
	}
	
	
	public boolean isAlive() {
		return ct.isAlive();
	}
	
	public String getName() {
		return socket.toString();
	}
	
	public SecuredUser getUser(IClientRequest cr) throws SecurityException {
		return SecurityCenter.getInstance().login(cr.getClientKey(), cr.getUsername(), cr.getPassword());
	}
	
	public Socket getSocket() {
		return socket;
	}	
}
