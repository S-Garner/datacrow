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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingDeque;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.DcStarter;
import org.datacrow.core.IStarterClient;
import org.datacrow.core.clients.IClient;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogSystem4j;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.modules.upgrade.ModuleUpgrade;
import org.datacrow.core.modules.upgrade.ModuleUpgradeResult;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.server.Connector;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.DataDirectoryCreator;
import org.datacrow.server.db.DatabaseInvalidException;
import org.datacrow.server.db.DatabaseManager;
import org.datacrow.server.security.SecurityCenter;
import org.datacrow.server.web.DcImageWebServer;
import org.datacrow.server.web.DcWebServer;
import org.datacrow.server.web.DcApiServer;

public class DcServer implements Runnable, IStarterClient, IClient {
	
	private static DcLogger logger;
	
	protected final int port;
	
    protected ServerSocket socket = null;
    protected boolean isStopped = false;
    protected Thread runningThread = null;
    
    private final LinkedBlockingDeque<DcServerSession> sessions = new  LinkedBlockingDeque<DcServerSession>();
    
    private static DcServer server;
    private static DcImageWebServer imgServer;
    private static DcApiServer apiServer;
    private static DcWebServer webServer;
    
    private static boolean enableApiServer = false;
    private static boolean enableWebServer = false;
	
	public DcServer(int port) {
		this.port = port;
	}
	
	public static void main(String[] args) {

		Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
            	if (DcConfig.getInstance().getConnector() != null)
            		DcConfig.getInstance().getConnector().shutdown(true);
            	
            	System.out.println("\r\nServer has stopped");
            }
        });
        
        System.setProperty("java.awt.headless", "true");
        
        DcLogManager.getInstance().setLogSystem(new DcLogSystem4j());
	    
        String installationDir = "";
        String dataDir = "";
        String db = "dc";
        
        int port = 9000;
        int webServerPort = -1;
        int imageServerPort = 8081;
        int apiServerPort = 8082;
        
        String username = "sa";
        String password = null;
        
        String serverIP = null;
        String imageIP = null;
        
        boolean determiningInstallDir = false;
        boolean determiningUserDir = false;
        
        DcConfig dcc = DcConfig.getInstance();
        for (String arg : args) {
            if (arg.toLowerCase().startsWith("-dir:")) {
                installationDir = arg.substring(5, arg.length());
                determiningInstallDir = true;
                determiningUserDir = false;
            } else if (arg.toLowerCase().startsWith("-ip:")) {
                serverIP = arg.substring(4, arg.length());
            } else if (arg.toLowerCase().startsWith("-imageip:")) {
            	imageIP = arg.substring(9, arg.length());
            } else if (arg.toLowerCase().startsWith("-userdir:")) {
                dataDir = arg.substring("-userdir:".length(), arg.length());
                determiningUserDir = true;
                determiningInstallDir = false;
                
            } else if (arg.toLowerCase().startsWith("-apiserverport:")) {
                String s = arg.substring("-apiserverport:".length());
                try {
                	apiServerPort = Integer.parseInt(s);
                } catch (NumberFormatException nfe) {
                    logger.error("Incorrect image port number " + port, nfe);
                }  
            } else if (arg.toLowerCase().startsWith("-imageserverport:")) {
                String s = arg.substring("-imageserverport:".length());
                try {
                    imageServerPort = Integer.parseInt(s);
                } catch (NumberFormatException nfe) {
                    logger.error("Incorrect image port number " + port, nfe);
                }  
            } else if (arg.toLowerCase().startsWith("-port:")) {
                String s = arg.substring("-port:".length());
                try {
                    port = Integer.parseInt(s);
                } catch (NumberFormatException nfe) {
                    logger.error("Incorrect port number " + port, nfe);
                }
            } else if (arg.toLowerCase().startsWith("-webserverport:")) {
                String s = arg.substring("-webserverport:".length());
                try {
                	webServerPort = Integer.parseInt(s);
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
        	@SuppressWarnings("resource")
			FileSystem fs = FileSystems.getDefault();
        	
            installationDir = fs.getPath(".").toAbsolutePath().getParent().toString();
            installationDir = !installationDir.endsWith("/") && !installationDir.endsWith("\\") ? installationDir + File.separatorChar : installationDir;
            
            try { if (fs != null) fs.close(); } catch (Exception e) {e.printStackTrace(); }
        }
        
        File file = new File(installationDir, "datacrow.credentials");
        if (file.exists()) {
            try {
                String credentials = new String(CoreUtilities.readFile(file));
                
                int index = credentials.indexOf("/");
                username = index > -1 ? credentials.substring(0, index) : credentials;
                password = index > -1 ? credentials.substring(index + 1) : "";
                
            } catch (IOException ioe) {
                System.out.println("File [" + file + "] could not be read");
            }
        }
        
        enableApiServer = apiServerPort > -1;
        enableWebServer = webServerPort > -1 && apiServerPort > -1;
	    
        if (CoreUtilities.isEmpty(serverIP)) {
            System.out.println("The IP address (-ip:<IP address>) is a required parameters. "
            		+ "It is used to generated URLs to server resources, such as images.\r\n");
            printParameterHelp();
        } else if (CoreUtilities.isEmpty(dataDir)) {
            System.out.println("The user dir (-userdir:<directory>) is a required parameters.\r\n");
            printParameterHelp();
        } else {
            
            dataDir = !dataDir.endsWith("/") && !dataDir.endsWith("\\") ? dataDir + File.separatorChar : dataDir;
            
    	    dcc.setOperatingMode(DcConfig._OPERATING_MODE_SERVER);
    	    dcc.setInstallationDir(installationDir);
    	    dcc.setDataDir(dataDir);
    	    
    	    imageIP = CoreUtilities.isEmpty(imageIP) ? serverIP : imageIP;
     	    
    	    server = new DcServer(port);
    	    
            if (server.initialize(username, password, db)) {
                
                Connector connector = dcc.getConnector();
                if (connector != null) {
                    connector.setApplicationServerPort(port);
                    connector.setImageServerPort(imageServerPort);
                    connector.setServerAddress(serverIP);
                    connector.setImageServerAddress(imageIP);
                }
                
                imgServer = new DcImageWebServer(imageServerPort, imageIP);
                
                if (enableApiServer) {
                    try {
                        apiServer = new DcApiServer(apiServerPort, serverIP);
                        apiServer.setup();
                    } catch (Exception e) {
                        logger.error("Web server could not be started", e);
                    }
                }
                
                if (enableWebServer) {
                    try {
                        webServer = new DcWebServer(webServerPort, serverIP, apiServerPort);
                        webServer.setup();
                    } catch (Exception e) {
                        logger.error("Web server could not be started", e);
                    }
                }
                
                // if the logger failed starting is unnecessary
                if (logger != null) {
                    logger.info("Server has been started, ready for client connections.");
                    logger.info("Thick clients can connect to IP address " + connector.getServerAddress() + 
                            " on port " + connector.getApplicationServerPort() + " and on image port " +
                            connector.getImageServerPort());

                    if (enableWebServer) {
                        logger.info("Web clients can connect via the following URL: http://" + connector.getServerAddress() + 
                                ":" + webServerPort + "");
                    } else {
                        logger.info("The web module has not been started.");
                    }
                    
                    logger.info("Listening for CTRL-C for server shutdown.");
                    
                    server.startServer();
                }
            }
        }
    }
	
    @Override
    public void intializeLogger(boolean debug) {
    	DcLogManager.getInstance().initialize(debug);
    }
	
	private void startServer() {
        Thread st = new Thread(server);
        st.start();

        if (enableApiServer) {
            try {
                apiServer.start();
            } catch (Exception e) {
                logger.error(e, e);
            }
        }        

        if (enableWebServer) {
            try {
                webServer.start();
            } catch (Exception e) {
                logger.error(e, e);
            }
        }        
        
        try {
            imgServer.start();
        } catch (Exception e) {
            logger.error(e, e);
        }
        
        try {
        	st.join();
        } catch (InterruptedException e) {
            logger.error(e, e);
        }

        logger.info("Server has been stopped");
        server.shutdown();
	}
	
	public static DcServer getInstance() {
	    return server;
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
        System.out.println("Note that it is also possible to supply the credentials in the datacrow.credentials file. Create the file and put the credentials within: username/password");
        System.out.println("");
        System.out.println("-userdir:<userdir>");
        System.out.println("Specifies the user directory. Start the name with a dot (.) to make the path relative to the installation folder.");
        System.out.println("Example: java -jar datacrow-server.jar -userdir:d:/datacrow-data");
        System.out.println("");                    
        System.out.println("-db:<databasename>");
        System.out.println("OPTIONAL: Forces Data Crow to use an alternative database.");
        System.out.println("Example: java -jar datacrow-server.jar -db:testdb");
        System.out.println("");
        System.out.println("-port:<port number>");
        System.out.println("Specifies the port to be used by the application server.");
        System.out.println("Example: java -jar datacrow-server.jar -port:9000");
        System.out.println("");
        System.out.println("-webserverport:<port number>");
        System.out.println("OPTIONAL: specifies the port to use for the web server. If not supplied, the web server module will NOT be started.");
        System.out.println("Example: -webserverport:8080");
        System.out.println("");
        System.out.println("-imageserverport:<port number>");
        System.out.println("DEFAULT: 8081 - if not specified");
        System.out.println("OPTIONAL: Specifies the port to be used by the image server.");
        System.out.println("Example: java -jar datacrow-server.jar -imageserverport:8081");
        System.out.println("");
        System.out.println("-apiserverport:<port number>");
        System.out.println("DEFAULT: 8082 - if not specified");
        System.out.println("OPTIONAL: Specifies the port to be used by the API server. The API is required for the web version to be operational.");
        System.out.println("Example: java -jar datacrow-server.jar -apiserverport:8082");
        System.out.println("");
        System.out.println("-debug");
        System.out.println("OPTIONAL: Debug mode for additional system event information.");
        System.out.println("Example: java -jar datacrow-server.jar -debug");   
        System.out.println("");            
        System.out.println("-ip:<server IP address>");
        System.out.println("Specifies the IP address used by the server. The server will use this IP address to point to resources such as images.");
        System.out.println("Example: java -jar datacrow-server.jar -ip:192.168.178.10");
        System.out.println("Make sure to use an external IP address if users will be connecting not attached to your network.");          
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
                DcModules.updateModuleSetting(mur);
                
    			try {
    			    DatabaseManager.getInstance().doDatabaseHealthCheck();
    			} catch (DatabaseInvalidException die) {
    				System.out.println(die.getMessage());
    			    System.exit(0);
    			}
    
                SecurityCenter.getInstance().initialize(username, password);
                
        	    LocalServerConnector connector = new LocalServerConnector();
        	    SecuredUser su = connector.login(username, password);
        	    
        	    if (su == null) {
        	        logger.error("The user could not login, please check the credentials.");
        	        initialized = false;
        	    } else {
            	    connector.initialize();
                    
                    DcConfig dcc = DcConfig.getInstance();
                    dcc.setConnector(connector);
                    
                    applyDatabaseSetting();
                    
                    DatabaseManager.getInstance().initialize();
                    DcModules.loadDefaultModuleData();
        	    }
            }
           
        } catch (Throwable t) {
            t.printStackTrace();
        	logger.error("Data Crow could not be started: " + t, t);
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
        	for (DcServerSession session : sessions) {
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
	
    private void applyDatabaseSetting() {
        try {
            File file = new File(DcConfig.getInstance().getDatabaseDir(), 
                                 DcSettings.getString(DcRepository.Settings.stConnectionString) + ".properties");
            if (file.exists()) {
                Properties properties = new Properties();
                
                FileInputStream fis = new FileInputStream(file);
                properties.load(fis);
                fis.close();
                
                properties.setProperty("readonly", "false");
                properties.setProperty("hsqldb.nio_data_file", "true");
                properties.setProperty("hsqldb.lock_file", "false");
                properties.setProperty("hsqldb.log_size", "10000");

                FileOutputStream fos = new FileOutputStream(file);
                properties.store(fos, "Default properties for the DC database of Data Crow.");
                fos.close();
            }
        } catch (Exception e) {
            logger.error("Could not set the default database properties.", e);
        }
    }   
	
	@SuppressWarnings("resource")
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
            
            DcServerSession session = new DcServerSession(clientSocket);
            sessions.add(session);
        }
        
        logger.info("Server Stopped.");
    }

    @Override
    public void notifyLoggerConfigured() {
        logger = DcLogManager.getInstance().getLogger(DcServer.class.getName());
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
    public boolean askQuestion(String msg) {
        return false;
    }

    @Override
    public void notifyError(String msg) {
        logger.error(msg);
    }

    @Override
    public void requestDataDirSetup(String target) {
        File f = new File(target, "database");
        if (!f.exists()) {
            DataDirectoryCreator ddc = new DataDirectoryCreator(new File(target), this);
            ddc.setMoveFiles(false);
            ddc.run();
            
            try {
                ddc.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
    public void notifyTaskCompleted(boolean success, String taskID) {
        if (success) {
            if (logger != null)
                logger.info("Creating and initializing the data folder SUCCESSFUL");
            else 
                System.out.println("Creating and initializing the data folder SUCCESSFUL");
        } else {
            if (logger != null)
                logger.error("Creating and initializing the data folder FAILED");
            else
                System.out.println("Creating and initializing the data folder FAILED");
        }
    }

    @Override
    public void notifyTaskStarted(int taskSize) {
        System.out.println("Creating and initializing the data folder.");
    }

    @Override
    public void notifyProcessed() {}

    @Override
    public boolean isCancelled() {
        return false;
    }
}
