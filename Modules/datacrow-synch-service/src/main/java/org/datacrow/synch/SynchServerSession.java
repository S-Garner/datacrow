package org.datacrow.synch;

import java.net.Socket;

import org.apache.logging.log4j.Logger;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.server.DcServerSession;

public class SynchServerSession extends DcServerSession {
	
	private transient static Logger logger = 
			DcLogManager.getLogger(SynchServerSession.class.getName());

	public SynchServerSession(Socket socket) {
		this.socket = socket;
		
		long time = System.currentTimeMillis();
		logger.debug("Client session started: " + time);
		
		ct = new SynchServerSessionRequestHandler(this);
		ct.start();
	}
}
