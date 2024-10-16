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

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.resources.DcResources;

public class SynchService {
	
	private static DcLogger logger = DcLogManager.getInstance().getLogger(SynchService.class.getName());
    private Task task;
    private ISynchServiceListener listener;
    
    // private final String name;
    private final int port;
    
	public SynchService(String name, int port) {
    	// this.name = name;
    	this.port = port;
	}
	
    public boolean start() {
        if (task == null || !task.isAlive()) {
            task = new Task();
            task.start();
            return true;
        }
        return false;
    }
    
    public void shutdown() {
    	if (task != null)
    		task.shutdown();
    }
    
    public void setListener(ISynchServiceListener listener) {
    	this.listener = listener;
    }
	
	private class Task extends Thread {
		
		private LinkedBlockingDeque<SynchServiceSession> sessions = new  LinkedBlockingDeque<SynchServiceSession>();
		
		protected boolean isStopped = false;
		
	    protected ServerSocket socket = null;
	    
	    protected Task() {}
	    
		@SuppressWarnings("resource")
		@Override
		public void run() {
	    	
	        openServerSocket();
	        
	        while(!isStopped()){
	            Socket clientSocket = null;
	            
	            try {
	            	listener.addMessage(DcResources.getText("msgSynchServiceWaiting"));
	            	
	                clientSocket = this.socket.accept();
	                clientSocket.setKeepAlive(true);
	                
	                listener.addMessage(
	                		DcResources.getText("msgSynchServiceClientConnected", 
	                		clientSocket.getInetAddress().getHostAddress()));
	                
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
	                	listener.addError(e.getMessage(), e);
	                	throw new RuntimeException("Error accepting client connection", e);
	                }
	            }
	            
	            SynchServiceSession session = new SynchServiceSession(clientSocket);
	            sessions.add(session);
	        }
	        
	        listener.addMessage(DcResources.getText("msgSynchServiceStopped"));
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
	}
}
