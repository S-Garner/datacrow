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

package org.datacrow.server.upgrade;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.Version;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcAssociate;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcMediaObject;
import org.datacrow.core.objects.DcProperty;
import org.datacrow.core.objects.helpers.MusicTrack;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.settings.objects.DcLookAndFeel;
import org.datacrow.core.utilities.Base64;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.Directory;
import org.datacrow.core.utilities.IImageConverterListener;
import org.datacrow.core.utilities.definitions.DcFieldDefinition;
import org.datacrow.core.utilities.definitions.DcFieldDefinitions;
import org.datacrow.server.db.DatabaseManager;

/**
 * Upgrade steps for the various versions.
 * 
 * Converts the current database before the actual module tables are created / updated.
 * This means that the code here defies workflow logic and is strictly to be used for
 * table conversions and migration out of the scope of the normal module upgrade code.
 * 
 * The automatic database correction script runs after this manual upgrade.
 * 
 * @author Robert Jan van der Waals
 */
public class SystemUpgrade {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(SystemUpgrade.class.getName());
    
    private final boolean dbInitialized;
    
    public SystemUpgrade(boolean dbInitialized) {
        this.dbInitialized = dbInitialized;
    }
    
    public void start() throws SystemUpgradeException {
        try {
            Version v = DatabaseManager.getInstance().getVersion();

            // mark the system as upgraded / older in the settings
            if (dbInitialized && v.isOlder(DcConfig.getInstance().getVersion())) { 
            	DcSettings.set(DcRepository.Settings.stIsUpgraded, Boolean.TRUE);
            }
            
            Connector connector = DcConfig.getInstance().getConnector();
            
            if (!dbInitialized && v.isOlder(new Version(4, 0, 0, 0))) {
                connector.displayMessage("Make sure you have NOT installed Data Crow on top of an "
                        + "older version (3.x) of Data Crow; this will cause errors. This is just "
                        + "for information.");
                
                checkAudioTables();
            }
            
            if (dbInitialized && v.isOlder(new Version(5, 0, 0, 0))) {
            	moveAllImages();
            	cleanupImages();
            	
            	addIconFieldToPropertyForms();
            	removePersistencyColumns();
           	
            	correctFieldSettings();
            }
            
            if (!dbInitialized)
            	renameRecordLabel();
            
            if (dbInitialized && v.isOlder(new Version(4, 9, 2, 0))) {
            	// currently set as bytes but should be KB
            	long size = DcSettings.getLong(DcRepository.Settings.stHashMaxFileSizeKb); 
            	DcSettings.set(DcRepository.Settings.stHashMaxFileSizeKb, size / 1000);
            }
            
            if (dbInitialized && v.isOlder(new Version(4, 9, 1, 0)))
            	removeSelfReferencingItems();
            
            if (dbInitialized && v.isOlder(new Version(4, 0, 2, 0)))
                renumberMusicTracks();
            
            if (dbInitialized && v.isOlder(new Version(4, 1, 1, 0)))
                saveIcons();
            
            if (dbInitialized && v.isOlder(new Version(4, 0, 5, 0)))
                new File(DcConfig.getInstance().getModuleSettingsDir(), "record label.properties").delete();
            
            if (dbInitialized && v.isOlder(DcConfig.getInstance().getVersion())) {
                File installDirRes = new File(DcConfig.getInstance().getInstallationDir(), "resources/");
                File userDirRes = new File(DcConfig.getInstance().getResourcesDir());
                Directory dir = new Directory(installDirRes.toString(), false, new String[] {"properties"});
                File sourceFile;
                for (String installDirFile : dir.read()) {
                    sourceFile = new File(installDirFile);
                    CoreUtilities.copy(
                            sourceFile, 
                            new File(userDirRes, sourceFile.getName()), 
                            false);
                }
                
                // When this file is removed, the default from the JAR file will be used.
                new File(userDirRes, "English_resources.properties").delete();
                
                // Reload the resources
                new DcResources().initialize();
            }
            
            if (dbInitialized && v.isOlder(DcConfig.getInstance().getVersion())) {
            	File installDirReport = new File(DcConfig.getInstance().getInstallationDir(), "reports/");
                File userDirReport = new File(DcConfig.getInstance().getReportDir());
                Directory dir = new Directory(installDirReport.toString(), true, new String[] {"jasper"});
                File sourceFile;
                File targetFile;
                for (String installDirFile : dir.read()) {
                    sourceFile = new File(installDirFile);
                    targetFile = new File(
                    		new File(userDirReport, sourceFile.getParentFile().getName()), sourceFile.getName());
                    
                    CoreUtilities.copy(sourceFile, targetFile, false);
                }
            }
            
            if (dbInitialized && v.isOlder(new Version(4, 7, 0, 0))) {
            	DcSettings.set(DcRepository.Settings.stLookAndFeel, 
            			new DcLookAndFeel("FlatLaf Light", "com.formdev.flatlaf.FlatLightLaf", null, 1));
            }
            
            if (dbInitialized && v.isOlder(new Version(4, 8, 0, 0))) {
            	if (DcSettings.getString(DcRepository.Settings.stLanguage).equals("Polski"))
            		DcSettings.set(DcRepository.Settings.stDatabaseLanguage, "Polish");
            	
            	if (	DcSettings.getString(DcRepository.Settings.stLanguage).equals("Portuguese") ||
            			DcSettings.getString(DcRepository.Settings.stLanguage).equals("Brazilian_Portuguese"))
            		DcSettings.set(DcRepository.Settings.stDatabaseLanguage, "Portuguese");            	
            }

            if (dbInitialized && v.isOlder(new Version(4, 8, 0, 0)))
            	correctKeyValueSettings();
            

        } catch (Exception e) {
            String msg = e.toString() + ". Data conversion failed. " +
                "Please restore your latest Backup and retry. Contact the developer " +
                "if the error persists";
            throw new SystemUpgradeException(msg);
        }
    }
    
    private void moveAllImages() {
    	if (DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_SERVER) {
    		new ImageConverter(ImageConverter._UPGRADE_CONVERSION);
    	} else if (DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_STANDALONE) {
    		DcSettings.set(DcRepository.Settings.stImageConversionNeeded, Boolean.TRUE);
    		DcSettings.set(DcRepository.Settings.stImageUpgradeConversionNeeded, Boolean.TRUE);
    	}
    }
    
    private void addIconFieldToPropertyForms() {
    	// add the icon field to the form - for existing installations.
    	DcFieldDefinition def;
    	
    	for (DcModule m : DcModules.getAllModules()) {
    		if (m.getType() == DcModule._TYPE_PROPERTY_MODULE) {
    			def = m.getFieldDefinitions().get(DcProperty._B_ICON);
    			
    			if (CoreUtilities.isEmpty(def.getTab())) {
    				def.setTab("lblInformation");
    				def.setEnabled(true);
    			}
    		}
    	}    	
    }
    
    private void correctFieldSettings() {
    	
    	Collection<DcFieldDefinition> invalid;
    	DcFieldDefinitions definitions;
    	
    	for (DcModule m : DcModules.getAllModules()) {
    		invalid = new ArrayList<DcFieldDefinition>();
    		definitions = m.getFieldDefinitions();
    		
    		for (DcFieldDefinition def : definitions.getDefinitions()) {
    			if (m.getField(def.getIndex()) == null) {
    				invalid.add(def);
    			}
    		}
    		
    		for (DcFieldDefinition def : invalid) {
    			definitions.getDefinitions().remove(def);
    		}
    		
    		if (invalid.size() > 0) {
    			m.setSetting(DcRepository.ModuleSettings.stFieldDefinitions, definitions);
    			m.getSettings().save();
    		}
    	}
    }
    
    private void cleanupImages() {
		String imageDir = DcConfig.getInstance().getImageDir();
		
		Set<String> filenames = new HashSet<>();
		
		// add images located in the images folder (these will need to be moved)
		try (Stream<Path> streamFiles = Files.list(Paths.get(imageDir))) {
			filenames.addAll(streamFiles
		              .filter(file -> file.toString().endsWith("_small.jpg"))
		              .map(Path::toAbsolutePath)
		              .map(Path::toString)
		              .collect(Collectors.toSet()));
        } catch (Exception e) {
        	logger.error(e, e);
        }
		
		File file;
	    for (String filename : filenames) {
	    	file = new File(filename);
	    	if (!file.delete())
	    		file.deleteOnExit();
	    }		
    }
    
    private void removePersistencyColumns() {
        @SuppressWarnings("resource")
		Connection conn = DatabaseManager.getInstance().getAdminConnection();
        Connector connector = DcConfig.getInstance().getConnector();
        Statement stmt = null;
        
		try {
			stmt = conn.createStatement();

			String column;
			String sql;
			// remove the _persist fields from all tables
			for (DcModule m : DcModules.getAllModules()) {

				if (m.isAbstract() || m.getType() == DcModule._TYPE_TEMPLATE_MODULE ||
					m.getType() == DcModule._TYPE_MAPPING_MODULE ||
					m.getType() == DcModule._TYPE_EXTERNALREFERENCE_MODULE)
					continue;

				for (DcField field : m.getFields()) {

					if (field.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION)
						continue;

					column = CoreUtilities.toDatabaseName(field.getSystemName()) + "_persist";
					sql = "ALTER TABLE " + m.getTableName() + " DROP COLUMN " + column;
					stmt.execute(sql);
				}
			}
		} catch (Exception e) {
			logger.error("Upgrade failed; could not remove the persistency columns.", e);
			connector.displayError("Upgrade failed; could not remove the persistency columns.");
			System.exit(0);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {};
		}
    }
    
    private void removeSelfReferencingItems() {
    	
    	@SuppressWarnings("resource")
		Connection conn = DatabaseManager.getInstance().getAdminConnection();
        Connector connector = DcConfig.getInstance().getConnector();
        Statement stmt = null;
    	
    	String sql;
    	DcModule mappingMod;
    	
    	try {
    		
    		stmt = conn.createStatement();
    		
	    	for (DcModule module : DcModules.getAllModules()) {
	    	
	    		for (DcField field : module.getFields()) {
	    			
	    			// check if the field is a reference field which references itself
	    			if ((field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE ||
	    			     field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) &&
	    				 field.getReferenceIdx() == module.getIndex()) {
	    				
	    				if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
		    				sql = "UPDATE " + module.getTableName() + " SET " + 
		    						field.getDatabaseFieldName() + " = NULL WHERE " + field.getDatabaseFieldName() + " = ID";
	    				} else {
	    					mappingMod = DcModules.get(DcModules.getMappingModIdx(module.getIndex(), module.getIndex(), field.getIndex()));
	    					sql = "DELETE FROM " + mappingMod.getTableName() + 
	    						  " WHERE " + mappingMod.getField(DcMapping._B_REFERENCED_ID).getDatabaseFieldName() + " = " + 
	    							mappingMod.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName();
	    				}
	    				
    		            stmt.execute(sql);
	    			}
	    		}
	    	}
    	} catch (SQLException se) {
    		logger.error("Failed to establish a connection to the database. The upgrade has failed.", se);
            connector.displayError("Upgrade failed; could not correct the self referencing items.");
            System.exit(0);
    	} finally {
        	try { if (stmt != null) stmt.close(); } catch (Exception e) {};
    	}
    }
    
    private void renameRecordLabel() {
    	@SuppressWarnings("resource")
        Connection conn = DatabaseManager.getInstance().getAdminConnection();
        Connector connector = DcConfig.getInstance().getConnector();
        Statement stmt = null;
        
        try {
            stmt = conn.createStatement();
            
			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet tables = dbm.getTables(null, null, "TBL_6A4A14EC9A6D43F380B5222A101EC4A2", null);
			boolean exists = tables.next();
			tables.close();

			if (exists) {

	            String sql = "alter table TBL_6A4A14EC9A6D43F380B5222A101EC4A2 rename to RECORDLABEL";
	            stmt.execute(sql);
	            
	            sql = "alter table TBL_6A4A14EC9A6D43F380B5222A101EC4A2_EXTERNALREFERENCE rename to RECORDLABEL_EXTERNALREFERENCE";
	            stmt.execute(sql);
	
	            sql = "alter table TBL_6A4A14EC9A6D43F380B5222A101EC4A2_TEMPLATE rename to RECORDLABEL_TEMPLATE";
	            stmt.execute(sql);
	            
	            sql = "alter table X_TBL_6A4A14EC9A6D43F380B5222A101EC4A2_EXTERNALREFERENCES rename to X_RECORDLABEL_EXTERNALREFERENCES";
	            stmt.execute(sql);            
	
	            sql = "alter table X_TBL_6A4A14EC9A6D43F380B5222A101EC4A2_TAGS rename to X_RECORDLABEL_TAGS";
	            stmt.execute(sql);            
	
	            sql = "alter table RECORDLABEL alter column col_8f90365edbd844738c5b662cb597a084 rename to Description";
	            stmt.execute(sql);            
	            
	            sql = "alter table RECORDLABEL alter column col_ca4236f9dc5c42398aa6a070f2149655 rename to Webpage";
	            stmt.execute(sql);            
	
	            sql = "alter table RECORDLABEL alter column col_d3a623ec48584c8cb0f25d3b8432d6f0 rename to Name";
	            stmt.execute(sql);            
	            
	            sql = "alter table RECORDLABEL alter column col_f87162e98604407bb33987913faf36a9 rename to ContactInformation";
	            stmt.execute(sql);
	            
	            sql = "alter table RECORDLABEL_TEMPLATE alter column col_8f90365edbd844738c5b662cb597a084 rename to Description";
	            stmt.execute(sql);            
	            
	            sql = "alter table RECORDLABEL_TEMPLATE alter column col_ca4236f9dc5c42398aa6a070f2149655 rename to Webpage";
	            stmt.execute(sql);            
	
	            sql = "alter table RECORDLABEL_TEMPLATE alter column col_d3a623ec48584c8cb0f25d3b8432d6f0 rename to Name";
	            stmt.execute(sql);            
	            
	            sql = "alter table RECORDLABEL_TEMPLATE alter column col_f87162e98604407bb33987913faf36a9 rename to ContactInformation";
	            stmt.execute(sql);
	            
	            sql = "alter table MUSIC_ALBUM alter column COL_FF4C3A3A56C243B6887F1A15490628BE rename to RecordLabel";
	            stmt.execute(sql);
			}
        } catch (Exception e) {
            logger.error("Upgrade failed; could not rename the record label tables.", e);
            connector.displayError("Upgrade failed; could not rename the record label tables.");
            System.exit(0);
        } finally {
        	try { if (stmt != null) stmt.close(); } catch (Exception e) {};
        }
    }
    
    private void correctKeyValueSettings() {
    	boolean hasKey;
    	DcFieldDefinitions definitions;
    	
    	// We mark all items not having a unique field as unique, based on the required setting
    	for (DcModule module : DcModules.getModules()) {
    		
    		if (module.getType() != DcModule._TYPE_MAPPING_MODULE &&   
                module.getType() != DcModule._TYPE_TEMPLATE_MODULE) {
    		
	    		definitions =  
    				(DcFieldDefinitions) module.getSetting(DcRepository.ModuleSettings.stFieldDefinitions);
	    		
	    		hasKey = false;
	    		
	    		// check if the unique setting has been set
	    		for (DcFieldDefinition def : definitions.getDefinitions()) {
	    			hasKey |= def.isUnique();
	    		}
	    		
	    		boolean found = false;
	    		if (!hasKey) {
	    			
	    			// if not, set uniqueness based on the required setting
	        		for (DcFieldDefinition def : definitions.getDefinitions()) {
	        			if (def.isRequired()) {
	        				def.setUnique(def.isRequired());
	        				found = true;
	        			}
	        		}
	        		
	        		// no settings at all, we'll adjust to the defaults per module type
	        		if (!found) {
	        			DcFieldDefinition def = null;
	        			if (module.getType() == DcModule._TYPE_PROPERTY_MODULE)
	        				def =  definitions.get(DcProperty._A_NAME);
	        			else if (module.getType() == DcModule._TYPE_ASSOCIATE_MODULE)
	        				def =  definitions.get(DcAssociate._A_NAME);	        			
	        			else if (module.getType() == DcModule._TYPE_MEDIA_MODULE)
	        				def =  definitions.get(DcMediaObject._A_TITLE);	        			
	        			
	        			if (def != null) {
	        				def.setDescriptive(true);
	        				def.setRequired(true);
	        				def.setUnique(true);
	        				def.setEnabled(true);
	        			}
	        		}
	    		}
    		}
    	}
    }
    
    private void saveIcons() {
    	@SuppressWarnings("resource")
        Connection conn = DatabaseManager.getInstance().getAdminConnection();
        Connector connector = DcConfig.getInstance().getConnector();
        connector.displayMessage("Data Crow will now save all icons to the image folder");
        
        String ID;
        String icon;
        for (DcModule m : DcModules.getAllModules()) {
            DcField fld = m.getIconField();
            
            if (fld != null) {
            	
            	Statement stmt = null;
            	ResultSet rs = null;
            	
                try {
                    String sql = "SELECT ID, " + fld.getDatabaseFieldName() + " FROM " + 
                                 m.getTableName() + " WHERE " + fld.getDatabaseFieldName() + " IS NOT NULL AND LENGTH(" + fld.getDatabaseFieldName() + ") > 1";
                    
                    stmt = conn.createStatement();
                    rs = stmt.executeQuery(sql);
                    
                    while (rs.next()) {
                        ID = rs.getString(1);
                        icon = rs.getString(2);

                        File file = new File(DcConfig.getInstance().getImageDir(), "icon_" + ID + ".jpg");
                        CoreUtilities.writeToFile(Base64.decode(icon.toCharArray()), file);
                    }
                } catch (Exception e) {
                    logger.error("Could not save icons for module " + m, e);
                } finally {
                	try {
                		if (rs != null) rs.close();
                		if (stmt != null) stmt.close();
                	} catch (Exception e) {
                		logger.error(e);
                	}
                }
            }
        }
    }
    
    private void renumberMusicTracks() {
    	@SuppressWarnings("resource")
        Connection conn = DatabaseManager.getInstance().getAdminConnection();
        Connector connector = DcConfig.getInstance().getConnector();
        connector.displayMessage("Data Crow will now convert the Music Track numbers");
        
        try {
            Statement stmt = conn.createStatement();
            
            DcModule module = DcModules.get(DcModules._MUSIC_TRACK);
            String fld = module.getField(MusicTrack._F_TRACKNUMBER).getDatabaseFieldName();
            
            String sql = "SELECT ID, " + fld + " FROM " + module.getTableName() + " WHERE " + fld + " IS NOT NULL AND LENGTH(" + fld + ") = 1"; 
            ResultSet rs = stmt.executeQuery(sql);
            
            String ID; 
            String track;
            while (rs.next()) {
                ID = rs.getString(1);
                track = rs.getString(2);
                
                if (Character.isDigit(track.charAt(0))) {
                    sql = "UPDATE " + module.getTableName() + " SET " + fld + " = '0" + track + "' WHERE ID = '" + ID + "'";
                    stmt.execute(sql);
                }
            }
            
            rs.close();
            stmt.close();
            
            connector.displayMessage("The conversion of the Music Track numbers was successfull");
            
        } catch (Exception e) {
            logger.error("Could not update the track numbers.", e);
            connector.displayError("Could not update the track numbers. Error: " + e);
        }
    }
    
    @SuppressWarnings("resource")
	private void checkAudioTables() {
        Map<String, String> rename = new HashMap<String, String>();
        rename.put("x_musicalbum_artists", "x_music_album_artists");
        rename.put("x_musicalbum_container", "x_music_album_container");
        rename.put("x_musicalbum_countries", "x_music_album_countries");
        rename.put("x_musicalbum_externalreferences", "x_music_album_externalreferences");
        rename.put("x_musicalbum_genres", "x_music_album_genres");
        rename.put("x_musicalbum_languages", "x_music_album_languages");
        rename.put("x_musicalbum_tags", "x_music_album_tags");
        rename.put("x_musictrack_artists", "x_music_track_artists");
        rename.put("x_musictrack_countries", "x_music_track_countries");
        rename.put("x_musictrack_genres", "x_music_track_genres");
        rename.put("x_musictrack_languages", "x_music_track_languages");
        rename.put("musicalbum", "music_album");
        rename.put("musicalbum_externalreference", "music_album_externalreference");
        rename.put("musicalbum_state", "music_album_state");
        rename.put("musicalbum_storagemedium", "music_album_storagemedium");
        rename.put("musicalbum_template", "music_album_template");
        rename.put("musictrack", "music_track");
        rename.put("musictrack_state", "music_track_state");
        rename.put("musictrack_template", "music_track_template");

        Connection conn = DatabaseManager.getInstance().getAdminConnection();
        Connector connector = DcConfig.getInstance().getConnector();
        
        connector.displayMessage("Data Crow will now upgrade the database. The Music Album module and the Audio CD module will be merged.");
        
        try {
            Statement stmt = conn.createStatement();
            
            String sql;
            String tableTarget;
            for (String tableSrc : rename.keySet()) {
                tableTarget = rename.get(tableSrc);
                try {
                	sql = "ALTER TABLE " + tableSrc + " RENAME TO " + tableTarget;
                    stmt.execute(sql);
                    logger.info("Renamed " + tableSrc + " to " + tableTarget);
                } catch (Exception e1) {
                    
                    ResultSet rs = null;
                    try {
                        sql = "SELECT COUNT(*) FROM " + tableTarget;
                        rs = stmt.executeQuery(sql);
                        rs.next();
                        
                        int count = rs.getInt(1);
                        
                        if (count == 0) {
                            sql = "DROP TABLE " + tableTarget;
                            stmt.execute(sql);
                            sql = "ALTER TABLE " + tableSrc + " RENAME TO " + tableTarget;
                            stmt.execute(sql);
                        } else {
                            logger.error("Could not rename " + tableSrc + " to " + tableTarget + 
                                    ". The target already exists and is holding records.", e1);
                        }
                    } catch (Exception e2) {
                        logger.error("Could not rename " + tableSrc + " to " + tableTarget, e1);
                        logger.error("Could not correct the situation", e2);
                    } finally {
                        if (rs != null)
                            rs.close();
                    }
                }
            }
            
            stmt.close();
        } catch (Exception e) {
            logger.fatal("Upgrade failed; existing tables music album tables could not be converted.", e);
            connector.displayError("Upgrade failed; existing tables music album tables could not be converted.");
            System.exit(0);
        }
        
        try {
            Statement stmt = conn.createStatement();
            
            String sql;
            try {
                 sql = "alter table music_album_template add service varchar(255)";
                 stmt.execute(sql);
                 sql = "alter table music_album_template add serviceurl varchar(255)";
                 stmt.execute(sql);
            } catch (Exception e) {}
            
            try {
                sql = "alter table music_album add service varchar(255)";
                stmt.execute(sql);
                sql = "alter table music_album add serviceurl varchar(255)";
                stmt.execute(sql);
           } catch (Exception e) {}
            
            try {
                sql = "alter table music_album_template add service varchar(255)";
                stmt.execute(sql);
                sql = "alter table music_album_template add serviceurl varchar(255)";
                stmt.execute(sql);
            } catch (Exception e) {}
            
            try {
                sql = "alter table audiocd add service varchar(255)";
                stmt.execute(sql);
                sql = "alter table audiocd add serviceurl varchar(255)";
                stmt.execute(sql);
           } catch (Exception e) {}
            
            try {
                sql = "alter table audiocd_template add service varchar(255)";
                stmt.execute(sql);
                sql = "alter table audiocd_template add serviceurl varchar(255)";
                stmt.execute(sql);
            } catch (Exception e) {}
            
            try {
                sql = "alter table music_album add TAGS_PERSIST varchar(256)";
                stmt.execute(sql);
            } catch (Exception e) {}
            
            try {
                sql = "alter table audiocd add TAGS_PERSIST varchar(256)";
                stmt.execute(sql);
            } catch (Exception e) {}
            
            try {
                sql = "alter table audiocd_template add TAGS_PERSIST varchar(256)";
                stmt.execute(sql);
            } catch (Exception e) {}
            
            try {
                sql = "alter table music_album_template add TAGS_PERSIST varchar(256)";
                stmt.execute(sql);
            } catch (Exception e) {}
            
            sql = "insert into music_album_template (" +
                  "artists_persist, container_persist, countries_persist, " +
                  "created, description, ean, externalreferences_persist, genres_persist, id, " +
                  "languages_persist, modified, rating, service, serviceurl, state, " +
                  "tags_persist, title, userlongtext1, userinteger1, userinteger2, usershorttext1, usershorttext2, webpage, year, templatename, defaulttemplate) " +
                  "select " +
                  "artists_persist, container_persist, countries_persist, " +
                  "created, description, ean, externalreferences_persist, genres_persist, id, " +
                  "languages_persist, modified, rating, service, serviceurl, state, " +
                  "tags_persist, title, userlongtext1, userinteger1, userinteger2, usershorttext1, usershorttext2, webpage, year, templatename, defaulttemplate " +
                  "from audiocd_template";
            
            try {
            	stmt.execute(sql);
            	logger.info("Migrate the music album templates successfully!");
            } catch (Exception e) {}
            
            sql = "insert into music_track( "
                    + "albumid, artists_persist, countries_persist, created, description, genres_persist, id, languages_persist, lyric, modified, "
                    + "playlength, rating, title, track, userlongtext1, userinteger1, userinteger2, usershorttext1, usershorttext2, year) "
                    + "select "
                    + "albumid, artists_persist, countries_persist, created, description, genres_persist, id, languages_persist, lyric, modified, "
                    + "playlength, rating, title, track, userlongtext1, userinteger1, userinteger2, usershorttext1, usershorttext2, year "
                    + "from audiotrack";
            
            stmt.execute(sql);
            logger.info("Migrate the music tracks successfully!");
            
            sql = "insert into music_track_template ( "
                    + "albumid, artists_persist, countries_persist, created, description, genres_persist, id, languages_persist, lyric, modified, "
                    + "playlength, rating, title, track, userlongtext1, userinteger1, userinteger2, usershorttext1, usershorttext2, year, templatename, defaulttemplate) "
                    + "select "
                    + "albumid, artists_persist, countries_persist, created, description, genres_persist, id, languages_persist, lyric, modified, "
                    + "playlength, rating, title, track, userlongtext1, userinteger1, userinteger2, usershorttext1, usershorttext2, year, templatename, defaulttemplate "
                    + "from audiotrack_template";
            
            stmt.execute(sql);
            logger.info("Migrate the music track temmplates successfully!");
            
            sql = "update music_track_template set defaulttemplate = FALSE"; 
            
            stmt.execute(sql);
            
            sql = "update music_album_template set defaulttemplate = FALSE"; 
            
            stmt.execute(sql);
            
            String storageMediumId = CoreUtilities.getUniqueID();
            sql = "insert into music_album_storagemedium (ID, name, icon) "
            		+ "values ('" + storageMediumId + "' , 'Audio CD', 'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYA"
            				+ "AAAf8/9hAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAA3XAAAN1wFCKJt4AAAAB3RJTUUH1QQBE"
            				+ "TU1Kn0pSwAAAzBJREFUOMttk8tPXGUYxn/fd875OGfOQOdSGChlLg2LirULTWmiG9rEbTWa1pRSdd"
            				+ "MYhLUr/wCNWyi404jYpLps2KhFodi5QCS2LGAIgQQoMIMXmA5zrm5KYojv8sn7y7N4nkdw4oaGBy8"
            				+ "DHwJ9QO6FvAZMA1+Njozl//svTsBjyUTy9du3P9jv6Oh41bKsZoB6vV7b2tp8MjHxTaS6X50bHRkb5"
            				+ "H+cpx7NzX7neV6wtLQULiwshKVSKVxcXAzL5XJYrVZD3/eD2dmZqaHhwaljTjt27u8fEC+d77mRz+f"
            				+ "lwcEBvu8jhEAIgWEYSClpNBoim811JxKJ/YhtvlYslB5oQ8ODl5OJ5Me3+geuLi8vS9/3qdUOOawdUK"
            				+ "sdUK8/R9M1mpRC03TCMKS7u7szn38ceeXihXmtt/fSp3fufJR2HTdVr9dxXQddN2hubiHV1k5raxuJR"
            				+ "JJKdZcm1YTjuPi+T6ot5S4u/m7pQN/ZzrO5SqWCUgrXc4nazayurpIv/AYQ3rj+nsjlzrFSXiYRT+J5"
            				+ "HmfOdJ4G+iSQM01TKaUwTRMhIB6Pky88DiOWfdH3/dbvf7gfxGIxqtUKnufhOA6GYRhATj+OUylFEAT"
            				+ "ouo5t20gpgj//2t8A0DTNAUzHcfA8DyEEvu8LQEhgrdFoeEoplFKcOhWj0Wjw7jvXpVLqma7rq29de1"
            				+ "utr6+jVBOu6+J5HoeHhz6wpgPT29vbyXQ63QrQnmpnd2+Hnp4e8flnX5iAubGxwc8PfySXPYfrukQiEX"
            				+ "Z2d+rAQ62391KlvLpy7Urf1bimaei6jmVa7FX2mJ8v8uTJH1SqFdpaUwghkVLS1dXFvXuT/9SP6p9oxU"
            				+ "Jp8+ULPedjsVhHuivdIoRASg1dN4hGm4nHE9h2FCklR0dHZLNZisXC30+Xnt6/Ozr+pQZQLJQeWBHzTdu"
            				+ "Ons5kMqaUEsdxCIKAMAwBsCyLTCbD3Nwj55dfp2fujo7fBDTtuNPFQunbaHMkOzs7k0ul2pVpmrKlpQXL"
            				+ "sgDY3Nr0Jycnnq+UV74eHRm7+QILxYll6rcG+t+w7cj7uqZfEVJ0CSFEEASbnuf9ZBjG+Mk5/wu6lVFu"
            				+ "Spi0OgAAAABJRU5ErkJggg==')";
            
            stmt.execute(sql);
            logger.info("Created storage medium Audio CD");
            
            sql = "insert into music_album (" +
                    "storagemedium, artists_persist, container_persist, countries_persist, " +
                    "created, description, ean, externalreferences_persist, genres_persist, id, " +
                    "languages_persist, modified, rating, service, serviceurl, state, " +
                    "tags_persist, title, userlongtext1, userinteger1, userinteger2, usershorttext1, usershorttext2, webpage, year) " +
                    "select '" +
                    storageMediumId + "', artists_persist, container_persist, countries_persist, " +
                    "created, description, ean, externalreferences_persist, genres_persist, id, " +
                    "languages_persist, modified, rating, service, serviceurl, state, " +
                    "tags_persist, title, userlongtext1, userinteger1, userinteger2, usershorttext1, usershorttext2, webpage, year " +
                    "from audiocd";
            
            stmt.execute(sql);
            logger.info("Migrated the audio CD's successfully");
            
            sql = "select ID, NAME, ICON from audiocd_state";
            ResultSet rs1 = stmt.executeQuery(sql);
            ResultSet rs2;
            String stateName1;
            String stateID1;
            String stateIcon1;
            
            String stateID2;
            while (rs1.next()) {
                stateName1 = rs1.getString("NAME");
                stateID1 = rs1.getString("ID");
                stateIcon1 = rs1.getString("ICON");
                
                if (stateName1 == null)
                	continue;
                
                rs2 = stmt.executeQuery("select ID from music_album_state where UPPER(NAME) = '" + stateName1.replaceAll("'", "''").toUpperCase() + "'");
                if (rs2.next()) {
                    stateID2 = rs2.getString("ID");
                    sql = "update music_album set state = '" + stateID2 + "' where state = '" + stateID1 + "'";
                    stmt.execute(sql);
                } else {
                    sql = "insert into music_album_state (ID, NAME, ICON) values ('" + stateID1 + "', '" + stateName1.replaceAll("'", "''") + 
                            "','" + stateIcon1 + "')";
                    stmt.execute(sql);
                }
                rs2.close();
            }
            
            rs1.close();
            
            sql = "select distinct externalid, id, name, externalidtype from audiocd_externalreference";
            PreparedStatement ps = conn.prepareStatement("insert into music_album_externalreference (externalid, id, name, externalidtype) values (?, ?, ?, ?)");
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                try {
                    ps.setString(1, rs.getString("externalid"));
                    ps.setString(2, rs.getString("id"));
                    ps.setString(3, rs.getString("name"));
                    ps.setString(4, rs.getString("externalidtype"));
                    ps.execute();
                } catch (Exception e) {
                    logger.error("Error while inserting Audio CD external reference. Skipping.");
                }
            }
            
            try {
            	if (ps != null) ps.close();
            	if (rs != null) rs.close();
            } catch (SQLException se) {
            	logger.error(se);
            }
            
            logger.info("Migrated the music album external references successfully");

            Map<String, String> migrate = new HashMap<String, String>();
            migrate.put("x_audiocd_artists", "x_music_album_artists");
            migrate.put("x_audiocd_container", "x_music_album_container");
            migrate.put("x_audiocd_countries", "x_music_album_countries");
            migrate.put("x_audiocd_externalreferences", "x_music_album_externalreferences");
            migrate.put("x_audiocd_genres", "x_music_album_genres");
            migrate.put("x_audiocd_languages", "x_music_album_languages");
            migrate.put("x_audiocd_tags", "x_music_album_tags");
            migrate.put("x_audiotrack_artists", "x_music_track_artists");
            migrate.put("x_audiotrack_countries", "x_music_track_countries");
            migrate.put("x_audiotrack_languages", "x_music_track_languages");
            migrate.put("x_audiotrack_genres", "x_music_track_genres");
            
            String targetTable = null;
            for (String srcTable : migrate.keySet()) {
            	try {
	                targetTable = migrate.get(srcTable);
	                
	                sql = "select created, modified, objectid, referencedid from " + srcTable;
	                rs = stmt.executeQuery(sql);
	                ps = conn.prepareStatement("insert into " + targetTable + "(created, modified, objectid, referencedid) values (?, ?, ?, ?)");
	                
	                while (rs.next()) {
	                	try {
		                	ps.setDate(1, rs.getDate("created"));
		                	ps.setDate(2, rs.getDate("modified"));
		                	ps.setString(3, rs.getString("objectid"));
		                	ps.setString(4, rs.getString("referencedid"));
		                	ps.execute();
	                	} catch (Exception e) {
	                		logger.info("Skipping invalid reference for " + targetTable);
	                		logger.debug(e, e);
	                	}
	                }
	                
	                logger.info("Migrated the music album external references successfully");

            	} catch (Exception e) {
            		logger.error("migration of " + targetTable + " has failed", e);
            	} finally {
            		try { if (rs != null) rs.close(); } catch (Exception e) {logger.error("Could not close resource");}
            		try { if (ps != null) ps.close(); } catch (Exception e) {logger.error("Could not close resource");}
            	}
            }
            
            logger.info("The various music modules have been merged successfully.");
            logger.info("Starting cleanup of the old tables.");
            
            Collection<String> oldTables = new ArrayList<String>();
            oldTables.addAll(migrate.keySet());
            oldTables.add("audiocd");
            oldTables.add("audiocd_state");
            oldTables.add("audiocd_template");
            oldTables.add("audiotrack");
            oldTables.add("audiotrack_template");
            
            for (String oldTable : oldTables) {
            	try {
	                sql = "DROP TABLE " + oldTable;
	                stmt.execute(sql);
	                logger.info("Removed table " + oldTable);
            	} catch (Exception e) {}
            }
            
            logger.info("Cleanup has been completed successfully.");
            
            stmt.close();
            conn.close();
            
            connector.displayMessage("The process has finished. The database will now be restarted. This might take up to 5 minutes");
            
            logger.info("Restarting the database. This will take up to 5 minutes.");
            // closes the database
            DatabaseManager.getInstance().closeDatabases(true);
            DatabaseManager.getInstance().getAdminConnection().close();
            
            logger.info("Restart was successful.");
            
            connector.displayMessage("The upgrade was successfull.");
            
        } catch (Exception e) {
            logger.fatal("Upgrade failed; existing tables music album tables could not be converted.", e);
            connector.displayError("Upgrade failed; existing tables music album tables could not be converted.");
            System.exit(0);
        } 
    }
    
    private static class ImageConverter implements IImageConverterListener {
    	
    	private static final int _SIZE_CONVERSION = 0;
    	private static final int _UPGRADE_CONVERSION = 1;
    	
    	private int counter = 1;
    	private int total;
    	
    	protected ImageConverter(int type) {
    		
    		Thread converter;
    		
    		if (type == _SIZE_CONVERSION)
    			converter = new org.datacrow.server.upgrade.ImageSizeConverter(this);
    		else
    			converter = new org.datacrow.server.upgrade.ImageUpgradeConverter(this);
        	
        	try {
        		converter.start();
        		converter.join();
        	} catch (Exception e) {
        		DcConfig.getInstance().getConnector().displayError(e.getMessage());
        	}    		
    	}
    	
    	@Override
    	public void notifyImageProcessed() {
    		System.out.print("\r Processing [" + String.valueOf(counter++) +"/"+ String.valueOf(total) + "]");
    	}

    	@Override
    	public void notifyToBeProcessedImages(int count) {
    		DcConfig.getInstance().getConnector().displayMessage(DcResources.getText("msgConvertImages"));
    		DcConfig.getInstance().getConnector().displayMessage(DcResources.getText("msgPleaseWait"));
    		total = count;
    	}

    	@Override
    	public void notifyError(String s) {
    		DcConfig.getInstance().getConnector().displayError(s);
    		System.exit(0);
    	}

    	@Override
    	public void notifyFinished() {
    		System.out.println();
    		DcConfig.getInstance().getConnector().displayMessage(DcResources.getText("msgSuccessfullyConvertedAllImages"));
    	}
    }
}
