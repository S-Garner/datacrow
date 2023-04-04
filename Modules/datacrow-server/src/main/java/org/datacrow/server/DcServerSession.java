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

import java.net.Socket;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;

public class DcServerSession {
	
	private transient static DcLogger logger = DcLogManager.getInstance().getLogger(DcServerSession.class.getName());
	
	protected Socket socket;
	protected DcServerSessionRequestHandler ct;
	
	public DcServerSession(Socket socket) {
		this.socket = socket;
		
		long time = System.currentTimeMillis();
		logger.debug("Client session started: " + time);
		
		ct = new DcServerSessionRequestHandler(this);
		ct.start();
	}
	
	public DcServerSession() {} 
	
	public boolean isAlive() {
		return ct.isAlive();
	}
	
	public void closeSession() {
		try {
			ct.cancel();
		} catch (Exception e) {
			logger.error(e, e);
		}
	}
	
	public String getName() {
		return socket.toString();
	}
	
	public Socket getSocket() {
		return socket;
	}
}
