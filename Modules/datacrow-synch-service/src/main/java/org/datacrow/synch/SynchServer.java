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

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.DcStarter;
import org.datacrow.core.IStarterClient;
import org.datacrow.core.clients.IClient;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.modules.upgrade.ModuleUpgrade;
import org.datacrow.core.modules.upgrade.ModuleUpgradeResult;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.server.Connector;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.server.LocalServerConnector;
import org.datacrow.server.db.DatabaseInvalidException;
import org.datacrow.server.db.DatabaseManager;
import org.datacrow.server.security.SecurityCenter;

public class SynchServer implements Runnable, IStarterClient, IClient {

	private static Logger logger;

    private static SynchServer instance;

    private final LinkedBlockingDeque<SynchServerSession> sessions = 
    		new  LinkedBlockingDeque<SynchServerSession>();
    
    private int port;
	
    private ServerSocket socket = null;
    private boolean isStopped = false;
    
	public SynchServer(int port) {
		this.port = port;
	}
	
	public static void main(String[] args) {

		Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
            	if (DcConfig.getInstance().getConnector() != null)
            		DcConfig.getInstance().getConnector().shutdown(true);
            	
            	System.out.println("\r\nSynch server has stopped");
            }
        });
        
        System.setProperty("java.awt.headless", "true");
	    
        String installationDir = "";
        String dataDir = "";
        String db = "dc";
        
        int port = 9000;
        
        String username = "sa";
        String password = null;
        
        String ip = null;
        
        boolean determiningInstallDir = false;
        boolean determiningUserDir = false;
        
        DcConfig dcc = DcConfig.getInstance();
        for (String arg : args) {
            if (arg.toLowerCase().startsWith("-dir:")) {
                installationDir = arg.substring(5, arg.length());
                determiningInstallDir = true;
                determiningUserDir = false;
            } else if (arg.toLowerCase().startsWith("-ip:")) {
                ip = arg.substring(4, arg.length());
            } else if (arg.toLowerCase().startsWith("-userdir:")) {
                dataDir = arg.substring("-userdir:".length(), arg.length());
                determiningUserDir = true;
                determiningInstallDir = false;
            } else if (arg.toLowerCase().startsWith("-port:")) {
                String s = arg.substring("-port:".length());
                try {
                    port = Integer.parseInt(s);
                } catch (NumberFormatException nfe) {
                    logger.error("Incorrect port number " + port, nfe);
                }
            } else if (arg.toLowerCase().startsWith("-db:")) {
                db = arg.substring("-db:".length());
            } else if (arg.toLowerCase().startsWith("-debug")) {
                dcc.setDebug(true);
            } else if (arg.toLowerCase().startsWith("-credentials:")) {
                String credentials = arg.substring("-credentials:".length());
                int index = credentials.indexOf("/");
                username = index > -1 ? credentials.substring(0, index) : credentials;
                password = index > -1 ? credentials.substring(index + 1) : "";
            } else if (determiningInstallDir && !arg.startsWith("-Dorg.")) { // exclude other parameters from being added to the path
                installationDir += " " + arg;
            } else if (determiningUserDir && !arg.startsWith("-Dorg.")) { // exclude other parameters from being added to the path
                dataDir += " " + arg;                    
            } else if (!arg.startsWith("-Dorg.")) { 
            	printParameterHelp();
                System.exit(0);
            }
        }
        
        if (installationDir.length() == 0) {
            installationDir = FileSystems.getDefault().getPath(".").toAbsolutePath().getParent().toString();
            installationDir = !installationDir.endsWith("/") && !installationDir.endsWith("\\") ? installationDir + File.separatorChar : installationDir;
        }
        
        File file = new File(installationDir, "datacrow.credentials");
        if (file.exists()) {
            try {
                String credentials = new String(CoreUtilities.readFile(file));
                
                int index = credentials.indexOf("/");
                username = index > -1 ? credentials.substring(0, index) : credentials;
                password = index > -1 ? credentials.substring(index + 1) : "";
                
            } catch (IOException ioe) {
                System.out.println("The " + file + " could not be read");
            }
        }
        
        if (CoreUtilities.isEmpty(ip)) {
            System.out.println("The IP address (-ip:<IP address>) is a required parameters.\r\n");
            printParameterHelp();
        } else if (CoreUtilities.isEmpty(dataDir)) {
            System.out.println("The user dir (-userdir:<directory>) is a required parameters.\r\n");
            printParameterHelp();
        } else {
            
            dataDir = !dataDir.endsWith("/") && !dataDir.endsWith("\\") ? dataDir + File.separatorChar : dataDir;
            
    	    dcc.setOperatingMode(DcConfig._OPERATING_MODE_SERVER);
    	    dcc.setInstallationDir(installationDir);
    	    dcc.setDataDir(dataDir);
    	    
    	    instance = new SynchServer(port);
    	    
            if (instance.initialize(username, password, db)) {
                
                Connector connector = dcc.getConnector();
                if (connector != null) {
                    connector.setApplicationServerPort(port);
                    connector.setServerAddress(ip);
                }
                
                if (logger != null) {
                    logger.info("Synch server has been started, ready for client connections.");
                    logger.info("Synch clients can connect to IP address " + connector.getServerAddress() + 
                            " on port " + connector.getApplicationServerPort() + " and on image port " +
                            connector.getImageServerPort());

                    logger.info("Listening for CTRL-C for synch server shutdown.");
                    
                    instance.startServer();
                }
            }
        }
    }
	
    @Override
    public void configureLog4j(boolean debug) {
        DcLogManager.configureLog4j(debug ? Level.DEBUG: Level.INFO, true);
    }
	
	private void startServer() {
        Thread st = new Thread(instance);
        st.start();

        try {
        	st.join();
        } catch (InterruptedException e) {
            logger.error(e, e);
        }

        logger.info("Server has been stopped");
        instance.shutdown();
	}
	
	public static SynchServer getInstance() {
	    return instance;
	}
	
	private static void printParameterHelp() {
        System.out.println("The following parameters can be used:");
        System.out.println("");
        System.out.println("-dir:<installdir>");
        System.out.println("Specifies the installation directory.");
        System.out.println("Example: java -jar datacrow-server.jar -dir:d:/datacrow");
        System.out.println("");
        System.out.println("-credentials:username/password");
        System.out.println("Specify the login credentials to start the Data Crow server (default user is SA with a blank password");
        System.out.println("Example (username and password): java -jar datacrow-server.jar -credentials:SA/12345");                
        System.out.println("Example (username without a password): java -jar datacrow-server.jar -credentials:SA");
        System.out.println("Note that it is also possible to supply the credentials in the datacrow.credentials file. Create the file and supply the credentials there: username/password");
        System.out.println("");
        System.out.println("-userdir:<userdir>");
        System.out.println("Specifies the user directory. Start the name with a dot (.) to make the path relative to the installation folder.");
        System.out.println("Example: java -jar datacrow-server.jar -userdir:d:/datacrow-data");
        System.out.println("");                    
        System.out.println("-db:<databasename>");
        System.out.println("Forces Data Crow to use an alternative database.");
        System.out.println("Example: java -jar datacrow-server.jar -db:testdb");
        System.out.println("");
        System.out.println("-port:<port number>");
        System.out.println("Specifies the port to be used by the application server.");
        System.out.println("Example: java -jar datacrow-server.jar -port:9000");
        System.out.println("");
        System.out.println("-debug");
        System.out.println("Debug mode for additional system event information.");
        System.out.println("Example: java -jar datacrow-server.jar -debug");   
        System.out.println("");            
        System.out.println("-ip:<server IP address>");
        System.out.println("Specifies the IP address used by the server. The server will use this IP address to point to resources such as images.");
        System.out.println("Example: java -jar datacrow-server.jar -ip:192.168.178.10");
        System.out.println("make sure to use an external IP address if users will be connecting not attached to your network.");          
	}
	
	public boolean initialize(String username, String password, String db) {
	    
	    boolean initialized = false;
	    
        try {
            
            DcStarter ds = new DcStarter(this);
            initialized = ds.initialize();
            if (initialized) {

                DcSettings.set(DcRepository.Settings.stConnectionString, "dc");
                if (!CoreUtilities.isEmpty(db))
                    DcSettings.set(DcRepository.Settings.stConnectionString, db);
                
                logger.info(new Date() + " Starting Data Crow Server.");
                
                ModuleUpgradeResult mur = new ModuleUpgrade().upgrade();
                DcModules.load();
                
    			try {
    			    DatabaseManager.getInstance().doDatabaseHealthCheck();
    			} catch (DatabaseInvalidException die) {
    				System.out.println(die.getMessage());
    			    System.exit(0);
    			}
    
                SecurityCenter.getInstance().initialize();
                
        	    LocalServerConnector connector = new LocalServerConnector();
        	    SecuredUser su = connector.login(username, password);
        	    
        	    if (su == null) {
        	        logger.error("The user could not login, please check the credentials.");
        	        initialized = false;
        	    } else {
            	    connector.initialize();
                    
                    DcConfig dcc = DcConfig.getInstance();
                    dcc.setConnector(connector);
                    
                    DatabaseManager.getInstance().initialize();
                    DcModules.loadDefaultModuleData();
        	    }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        	logger.error("Synch server could not be started: " + t, t);
            try {
                DcSettings.set(DcRepository.Settings.stGracefulShutdown, Boolean.FALSE);
                DcSettings.save();
            } catch (Exception e) {
            	logger.error("An error occured while saving settings: " + e, e);
            }
        }
        return initialized;
	}
    
    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void shutdown(){
        this.isStopped = true;
        
        try {
        	for (SynchServerSession session : sessions) {
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
            
            SynchServerSession session = new SynchServerSession(clientSocket);
            sessions.add(session);
        }
        
        logger.info("Server Stopped.");
    }

    @Override
    public void notifyLog4jConfigured() {
        logger = DcLogManager.getLogger(SynchServer.class.getName());
    }

    @Override
    public void notifyFatalError(String msg) {
        if (logger != null)
            logger.error(msg);
        else
            System.out.println(msg);
        
        System.exit(0);
    }

    @Override
    public void notifyWarning(String msg) {
        if (logger != null)
            logger.warn(msg);
        else
            System.out.println(msg);
    }

    @Override
    public void notifyError(String msg) {
        logger.error(msg);
    }

    @Override
    public void requestDataDirSetup(String target) {
    	System.out.println("The data folder does not exist or is invalid. Please specify a correct, "
    			+ "initialized data folder; -userdir:<userdir>.");
    	
    	System.exit(0);
    }

    @Override
    public void notify(String msg) {
        if (logger != null)
            logger.info(msg);
        else
            System.out.println(msg);   
    }

    @Override
    public void notifyError(Throwable t) {
        if (logger != null)
            logger.error(t, t);
        else
            t.printStackTrace();
    }

    @Override
    public void notifyTaskCompleted(boolean success, String taskID) {}

    @Override
    public void notifyTaskStarted(int taskSize) {}

    @Override
    public void notifyProcessed() {}

    @Override
    public boolean isCancelled() {
        return false;
    }
}
