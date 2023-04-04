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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;

import org.datacrow.core.DcConfig;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.server.Connector;
import org.datacrow.core.server.DcServerConnection;
import org.datacrow.core.server.requests.ClientRequest;
import org.datacrow.core.server.response.ServerResponse;
import org.datacrow.core.server.serialization.SerializationHelper;

public class ClientRequestHandler {
	
    private transient static DcLogger logger = DcLogManager.getInstance().getLogger(ClientRequestHandler.class);

	private ClientRequest cr;
    
	public ClientRequestHandler(ClientRequest cr) {
		this.cr = cr;
	}
	
	/** 
	 * One method to handle them all.
	 * 
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
    public synchronized ServerResponse process() throws IOException, ClassNotFoundException {
	    
        ServerResponse response = null;
        DcServerConnection connection = null;
        
        ObjectOutputStream os;
        ObjectInputStream is;
        
        try {
            Connector conn = DcConfig.getInstance().getConnector();
            connection = conn.getServerConnection();
            connection.setAvailable(false);
            
            os = connection.getOutputStream();
            is = connection.getInputStream();
            
            String json = SerializationHelper.getInstance().serialize(cr);
            os.writeObject(json);
            os.flush();
            
            response = SerializationHelper.getInstance().deserializeServerResponse(is);
            
            logger.debug("Client has received: " + response);
        } catch (SocketException se) {
            try {
                wait(1000);
            } catch (InterruptedException ie) {
                logger.debug("Failed to wait for 1 second after connection error", ie);
            }
            
            if (connection != null)
            	connection.disconnect();
            
        } catch (Exception e) {
            logger.error("Failed to connect to server", e);
        }
        
        if (connection != null)
            connection.setAvailable(true);
        
        return response;
	}
}
