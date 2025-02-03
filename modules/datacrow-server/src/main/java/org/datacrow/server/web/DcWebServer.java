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

package org.datacrow.server.web;

import java.io.File;

import org.datacrow.core.DcConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

public class DcWebServer {
    
    private static final String context = "/";
    
	private final Server server;
	
	private boolean isRunning;

	/**
	 * Creates a new instance.
	 */
	public DcWebServer(int port, String ip) throws Exception {
        this.server = new Server();
        
        @SuppressWarnings("resource")
		ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        connector.setHost(ip);
        connector.setIdleTimeout(30000);
        server.addConnector(connector);
        
        String baseDir = DcConfig.getInstance().getWebDir();
        File contextDir = new File(baseDir, context);
        
        WebAppContext wac = new WebAppContext(contextDir.toString(), context);
        wac.setResourceBase(contextDir.toString());
        wac.setConfigurationDiscovered(true);
        wac.setParentLoaderPriority(true);
        
        server.setHandler(wac);
        server.setStopAtShutdown(true);
	}
	
	/**
	 * Indicates if the server is currently up and running.
	 */
	public boolean isRunning() {
        return isRunning;
    }
	
	public void setup() {}
	
    /**
     * Stops the server.
     * @throws Exception
     */
	public void stop() throws Exception {
	    server.stop();
        isRunning = false;
	}
	
	/**
	 * Starts the Web Server. The port is configurable.
	 */
	public void start() throws Exception {
        server.start();
        isRunning = true;
	} 
}
