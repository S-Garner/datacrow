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

package org.datacrow.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.Version;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.server.upgrade.SystemUpgrade;
import org.datacrow.server.upgrade.SystemUpgradeException;

/**
 * Manages the Data Crow database. Is responsible for executing queries and 
 * database maintenance (upgrading).
 * 
 * @author Robert Jan van der Waals
 */
public class DcDatabase {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DcDatabase.class.getName());
    
    private QueryQueue queue;
    private Version originalVersion;

    private final Conversions conversions = new Conversions();
    
    public DcDatabase() {}
    
    protected Conversions getConversions() {
        return conversions;
    }
    
    /**
     * The version from before the upgrade.
     */
    protected Version getOriginalVersion() {
        return originalVersion;
    }

    /**
     * Retrieves the current version of the database. In case the database does not have
     * a version number assigned an undetermined version number is returned.
     * @param connection
     */
    protected Version getVersion(Connection connection) {
        int major = 0;
        int minor = 0;
        int build = 0;
        int patch = 0;

        try {
            
            if (connection != null) {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM VERSION");
                
                while (rs.next()) {
                    major = rs.getInt("major");
                    minor = rs.getInt("minor");
                    build = rs.getInt("build");
                    patch = rs.getInt("patch");
                }
                
                rs.close();
                stmt.close();
            }
                
        } catch (SQLException se) {}
        
        return new Version(major, minor, build, patch);
    }
    
    @SuppressWarnings("resource")
	public boolean isNew() {
    	return getVersion(DatabaseManager.getInstance().getAdminConnection()).equals(new Version(0, 0, 0, 0));
    }
    
    private void updateVersion(Connection connection) throws DatabaseVersionException {
    	try {
	        Version v = DcConfig.getInstance().getVersion();
	        Statement stmt = connection.createStatement();
	        stmt.execute("DELETE FROM VERSION");
	        stmt.execute("INSERT INTO VERSION(MAJOR, MINOR, BUILD, PATCH) VALUES (" + 
	                     v.getMajor() + "," + v.getMinor() + "," + v.getBuild() + "," + v.getPatch() + ")");
	        stmt.close();
    	} catch (Exception e) {
    		throw new DatabaseVersionException("The version could not be updated", e);
    	}
    }
    
    /**
     * Initializes the database. Upgrades are performed automatically; missing columns are added
     * and missing tables are created.
     * 
     * @param connection
     * @throws Exception
     */
    @SuppressWarnings("resource")
	protected void initialize() throws SystemUpgradeException, 
    								   DatabaseInvalidException,
    								   DatabaseInitializationException,
    								   DatabaseVersionException	{
    	
        Connection connection = DatabaseManager.getInstance().getAdminConnection();
        
        if (connection == null)
        	throw new DatabaseInvalidException("The database is invalid. Data Crow "
        			+ "could not connect to the database.");
        
        if (!isNew()) {
        	new SystemUpgrade(false).start();
        	connection = DatabaseManager.getInstance().getAdminConnection();
        }
        
        startQueryQueue();
        initialize(connection);
        setProperties(connection);
        checkIndexes(connection);
        
        if (!isNew())
            new SystemUpgrade(true).start();
        
        originalVersion = getVersion(connection);
        updateVersion(connection);

        // Set the database privileges for the current user. This avoids errors for upgraded modules and such. 
        DatabaseManager.getInstance().setPriviliges(DcConfig.getInstance().getConnector().getUser().getUser());
        DatabaseManager.getInstance().setPriviliges("DC_ADMIN", true);
        
        
    }
    
    /**
     * Removes unused columns.
     */
	protected void cleanup() {
        
        // As we might still need the info during the upgrade..... 
        // we'll clean this all up after the database has been upgraded!
        if (DcConfig.getInstance().getVersion().isNewer(DatabaseManager.getInstance().getOriginalVersion()))
            return;
        
        String sql;
        ResultSet rs = null;
        Statement stmt = null;
        ResultSetMetaData md;
        String columnName;
        boolean remove;
        
        @SuppressWarnings("resource")
		Connection c = DatabaseManager.getInstance().getAdminConnection();
        
        try {
        	stmt = c.createStatement();
        
	        for (DcModule module : DcModules.getAllModules()) {
	            
	            if (module.isAbstract()) continue;
	            
	            try {
	                sql = "select top 1 * from " + module.getTableName();
	                
	                rs = stmt.executeQuery(sql);
	                md = rs.getMetaData();
	
	                for (int i = 1; i < md.getColumnCount() + 1; i++) {
	                	columnName = md.getColumnName(i);
	                	remove = false;
	                	
	                    if (!columnName.startsWith("KEEP_ME_")) { 
	                        for (DcField field : module.getFields()) {
	                            if (columnName.equalsIgnoreCase(field.getDatabaseFieldName())) 
	                                remove = field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ? true : false; 
	                        }
	                        
	                        // the column is not used.. remove!
	                        if (remove) {
	                            logger.info("Removing column " + columnName + " for module " + module.getName() + " as it is no longer in use");
	                            stmt.execute("alter table " + module.getTableName() + " drop column " + columnName);
	                        }
	                    }
	                }
	            } catch (Exception e) {
	                logger.error("Error while trying to cleanup unused columns", e);
	            } finally {
	            	try { if (rs != null) rs.close(); } catch (Exception e) {logger.error("Could not close database resource");}
	            }
	        }
        } catch (SQLException e) {
            logger.error("Error while trying to cleanup unused columns", e);
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception e) {logger.error("Could not close database resource");}
        }
    }

    /**
     * Returns the current count of queries waiting in the queue.
     */
    protected int getQueueSize() {
    	return queue.getQueueSize();
    }
    
    private void startQueryQueue() {
        queue = new QueryQueue();
        Thread queryQueue = new Thread(queue, "queryQueue");
        queryQueue.setPriority(Thread.NORM_PRIORITY);
        queryQueue.setDaemon(true);
        queryQueue.start();
    }

    /**
     * Returns the name of the database.
     */
    protected String getName() {
        return DcSettings.getString(DcRepository.Settings.stConnectionString);
    }

    /**
     * Adds a query to the query queue of this database.
     * @param query
     */
    protected void queue(Query query) {
        queue.addQuery(query);
    }

    protected void createCheckpoint(Connection connection) {
        try {
            Statement stmt = connection.createStatement();
            stmt.execute("CHECKPOINT");
            stmt.close();
        } catch (Exception e) {
            logger.error("Checkpoint could not be created.", e);
        }
    }
    
    /**
     * Applies the default settings on the database.
     * @param connection
     */
    protected void setProperties(Connection connection) {
        try {
            Statement stmt = connection.createStatement();
            stmt.execute("SET FILES SCRIPT FORMAT COMPRESSED");
            stmt.execute("SET FILES LOG TRUE");
            stmt.execute("SET FILES WRITE DELAY FALSE");
            stmt.close();
        } catch (Exception e) {
            logger.error(DcResources.getText("msgUnableToChangeDbSettings"), e);
        }
    }

    private void initialize(Connection connection) throws DatabaseInitializationException {
    	try {
	        Statement stmt = connection.createStatement();
	
	        initializeSystemTable(stmt);
	        String testQuery;
	        ResultSet rs = null;
	        for (DcModule module : DcModules.getAllModules()) {
	            if (!module.isAbstract()) {
	                testQuery = "select * from " + module.getTableName();
	                try {
	                    stmt.setMaxRows(1);
	                    rs = stmt.executeQuery(testQuery);
	                    initializeColumns(connection, rs.getMetaData(), module);
	                    logger.debug(DcResources.getText("msgTableFound", module.getTableName()));
	                } catch (SQLException e) {
	                    logger.info((DcResources.getText("msgTableNotFound", module.getTableName())));
	                    createTable(module);
	                } finally {
	                    try {
	                        if (rs != null) rs.close();
	                    } catch (Exception e) {
	                        logger.debug("Failed to close ResultSet", e);
	                    }
	                }
	            }
	        }
	        stmt.close();
    	} catch (Exception e) {
    		throw new DatabaseInitializationException("Database initiliazation has failed", e);
    	}
    }
    
    private void initializeSystemTable(Statement stmt) {
        try {
            stmt.execute("SELECT * FROM VERSION");
        } catch (Exception e) {
            try {
                stmt.execute("CREATE TABLE VERSION (Major " + DcRepository.Database._FIELDBIGINT + "," +
                                                   "Minor " + DcRepository.Database._FIELDBIGINT + "," +
                                                   "Build " + DcRepository.Database._FIELDBIGINT + "," +
                                                   "Patch " + DcRepository.Database._FIELDBIGINT + ")");
            } catch (SQLException se) {
                logger.error("Could not create the version table!", se);
            }
        }
    }
    
    private void checkIndexes(Connection conn) {
        Statement stmt = null;
        
		try {
			stmt = conn.createStatement();

			for (DcModule m : DcModules.getAllModules()) {
				if (m.getType() == DcModule._TYPE_MAPPING_MODULE) {
	                stmt.execute("CREATE INDEX IF NOT EXISTS " + m.getTableName() + "_REFERENCEID_IDX ON " + m.getTableName() + " (" +
	                        m.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName() + ")");
				}
				
				if (m.isChildModule() && !m.isAbstract()) {
	                stmt.execute("CREATE INDEX IF NOT EXISTS " + m.getTableName() + "_PARENTID_IDX ON " + m.getTableName() + " (" +
	                        m.getField(m.getParentReferenceFieldIndex()).getDatabaseFieldName() + ")");
				}
			}
			
		} catch (Exception e) {
			logger.error("An error occured while checking the indexes.", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {};
		}
    }
    
    @SuppressWarnings("resource")
	private void initializeColumns(Connection connection, ResultSetMetaData metaData, DcModule module) throws SQLException {
        String tablename = module.getTableName();
        
        String column;
        String type;
        boolean found;
        boolean convert;
        int dbSize;
        int dbType;
        for (DcField field : module.getFields()) {
            
            column = field.getDatabaseFieldName();
            type = field.getDataBaseFieldType();
            
            found = false;
            convert = false;
            
            if (!field.isUiOnly()) {
                for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                    if (metaData.getColumnName(i).equalsIgnoreCase(column)) {
                        found = true;
                        dbSize = metaData.getColumnDisplaySize(i);
                        dbType = metaData.getColumnType(i);
                        
                        convert = false;
                        if (    dbType == Types.BIGINT && 
                               (field.getValueType() == DcRepository.ValueTypes._DCPARENTREFERENCE ||
                                field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE ||
                                field.getIndex() == DcObject._ID))
                            convert = true;
                        else if (dbSize < field.getMaximumLength() && 
                               (field.getValueType() == DcRepository.ValueTypes._STRING))
                            convert = true;
                        
                        if (convert) {
                            logger.info(DcResources.getText("msgTableUpgradeIncorrectColumn", new String[] {tablename, field.getLabel()}));
                            executeQuery(connection, "alter table " + tablename + " alter column " + column + " " + type);
                        }
                    }
                }
            } 
            
            if (!field.isUiOnly() && !found) {
                logger.info(DcResources.getText("msgTableUpgradeMissingColumn", new String[] {tablename, field.getLabel()}));
                executeQuery(DatabaseManager.getInstance().getAdminConnection(), "alter table " + tablename + " add column " + column + " " + type);
            }
        }
    }

    @SuppressWarnings("resource")
	private void executeQuery(Connection connection, String sql) {
        try {
            executeQuery(connection.prepareStatement(sql));
        } catch (Exception e) {
            logger.error("Error while executing query " + sql, e);
        }
    }
    
    private void executeQuery(PreparedStatement ps) {
        try {
            ps.execute();
            logger.info(ps);
            ps.close();
        } catch (Exception e) {
            logger.error("Error while executing query " + ps, e);
        }
    }

    private void createTable(DcModule module) {
        try {
            Query query = new CreateQuery(module.getIndex());
            query.run();
            module.setNew(true);
        } catch (Exception e) {
            logger.error("An error occurred while inserting demo data", e);
        }
    }
}
