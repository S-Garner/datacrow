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

package org.datacrow.core.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.NoSuchPaddingException;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;

public class DcServerConnection {

    private transient static DcLogger logger = DcLogManager.getInstance().getLogger(DcServerConnection.class.getName());
    
    private boolean isAvailable = true;
    
    private Socket socket;

    private ObjectInputStream is;
    private ObjectOutputStream os;
    
    public DcServerConnection(Connector conn) throws IOException, SocketException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException {
        socket = new Socket(conn.getServerAddress(), conn.getApplicationServerPort());
        socket.setKeepAlive(true);
        
        is = new ObjectInputStream(socket.getInputStream());
        os = new ObjectOutputStream(socket.getOutputStream());
    }
    
    public ObjectInputStream getInputStream() {
        return is;
    }
    
    public ObjectOutputStream getOutputStream() {
        return os;
    }
    
    public void disconnect() {
        try {
            is.close();
            os.close();
        } catch (Exception e) {
            logger.error("Error while closing connections", e);
        }
    }
    
    public void setAvailable(boolean b) {
        this.isAvailable = b;
    }
    
    public boolean isAvailable() {
        return isAvailable;
    }
    
    public boolean isActive() {
        return !socket.isClosed();
    }
}
