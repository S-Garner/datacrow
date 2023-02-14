package org.datacrow.synch;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.logging.log4j.Logger;
import org.datacrow.core.DcConfig;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.server.requests.ClientRequestLogin;
import org.datacrow.core.server.requests.ClientRequestModules;
import org.datacrow.core.server.response.IServerResponse;
import org.datacrow.core.server.serialization.SerializationHelper;
import org.datacrow.server.DcServerSessionRequestHandler;
import org.datacrow.server.LocalServerConnector;
import org.datacrow.synch.request.SynchClientRequest;
import org.datacrow.synch.response.SynchServerLoginResponse;
import org.datacrow.synch.response.SynchServerModuleResponse;

public class SynchServerSessionRequestHandler extends DcServerSessionRequestHandler {

	private static Logger logger = DcLogManager.getLogger(DcServerSessionRequestHandler.class.getName());
	
	private Socket socket;
	private boolean canceled = false;
	private LocalServerConnector conn;
	
	public SynchServerSessionRequestHandler(SynchServerSession session) {
		super(session);
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

	        // this is the connector we'll use on the server.
            // we'll use the actual logged on user credentials for the actions to be performed.
            conn = new LocalServerConnector();
            DcConfig.getInstance().setConnector(conn);
            
            while (!socket.isClosed()) {
                try {
                    
                    cr = SerializationHelper.getInstance().deserializeClientRequest(is);
                    
                    if (!(cr instanceof SynchServerLoginResponse))
                        conn.setUser(session.getUser(cr));
                    
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
	
	/**
	 * Processes an request. The type of the request is checked before type casting.
	 * @throws Exception
	 */
	private void processRequest(ObjectOutputStream os) throws Exception {
        try {
            IServerResponse sr = null;
	        switch (cr.getType()) {
	        case SynchClientRequest._REQUEST_LOGIN:
	        	sr = processLoginRequest((ClientRequestLogin) cr);
	        	break;
	        case SynchClientRequest._REQUEST_MODULES:
	        	sr = processModulesRequest((ClientRequestModules) cr);
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
	
    private IServerResponse processModulesRequest(ClientRequestModules crm) throws Exception {
        return new SynchServerModuleResponse();
    }
	
	protected IServerResponse processLoginRequest(ClientRequestLogin lr) {
		SecuredUser su = conn.login(lr.getUsername(), lr.getPassword());
		return new SynchServerLoginResponse(su);
	}
}
