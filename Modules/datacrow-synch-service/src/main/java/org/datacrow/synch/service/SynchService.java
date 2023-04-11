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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;

import org.datacrow.core.log.DcLogger;

public class SynchService implements Runnable {
	
	private static DcLogger logger;

	protected final String name;
	protected final int port;
	protected final String ip;
	
    protected ServerSocket socket = null;
    protected boolean isStopped = false;
    protected Thread runningThread = null;
    
    private LinkedBlockingDeque<SynchServiceSession> sessions = new  LinkedBlockingDeque<SynchServiceSession>();
    
	public SynchService(String name, String ip, int port) {
		this.name = name;
		this.ip = ip;
		this.port = port;
		
		startService();
	}
	
	private void startService() {
        Thread st = new Thread(this);
        st.start();

        
        try {
        	st.join();
        } catch (InterruptedException e) {
            logger.error(e, e);
        }

        logger.info("Service has been stopped");
        shutdown();
	}
	
    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public void shutdown(){
        this.isStopped = true;
        
        try {
        	for (SynchServiceSession session : sessions) {
        		session.closeSession();
        	}
        	
        	if (this.socket != null)
        	    this.socket.close();
        	
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.socket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port " + port, e);
        }
    }
	
	@Override
	public void run() {
    	
        synchronized(this) {
            this.runningThread = Thread.currentThread();
        }
        
        openServerSocket();
        
        while(!isStopped()){
            Socket clientSocket = null;
            
            try {
                clientSocket = this.socket.accept();
                clientSocket.setKeepAlive(true);
                
                logger.info("A client has connected (" + clientSocket.getInetAddress() + ")");
                
            } catch (IOException e) {
            	
                if (clientSocket != null) {
                    try {
                    	clientSocket.close();
                    } catch (Exception e2) {
                        logger.debug("Error closing client socket after Exception was thrown: " + e, e2);
                    }
                }
                
                if (isStopped()) {
                    logger.info("Server Stopped.");
                    return;
                } else {
                	throw new RuntimeException("Error accepting client connection", e);
                }
            }
            
            SynchServiceSession session = new SynchServiceSession(clientSocket);
            sessions.add(session);
        }
        
        logger.info("Server Stopped.");
    }
}
