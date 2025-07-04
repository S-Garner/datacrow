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

package org.datacrow.core.migration.itemimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.DcThread;
import org.datacrow.core.clients.IItemImporterClient;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.migration.itemimport.ItemImporters.ImporterType;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.CSVReader;

public class CsvImporter extends ItemImporter {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(CsvImporter.class.getName());
    
    public static final String _SEPERATOR = "seperator";
    public static final String _CHARACTER_SET = "character_set";
    
    public CsvImporter(
    		int moduleIdx, 
    		int mode) throws Exception {
    	
        super(DcConfig.getInstance().getConnector().getUser(), moduleIdx, "CSV", mode);
    }

    @Override
    public Collection<String> getSettingKeys() {
        Collection<String> settingKeys = super.getSettingKeys();
        settingKeys.add(DcRepository.Settings.stImportCharacterSet);
        settingKeys.add(DcRepository.Settings.stImportSeperator);
        settingKeys.add(DcRepository.Settings.stImportMatchAndMerge);
        return settingKeys;
    }
    
	@Override
	public ImporterType getType() {
		return ItemImporters.ImporterType.CSV;
	}    
    
    private String getCharacterSet() {
        String charSet = getSetting(_CHARACTER_SET);
        return charSet == null ? DcSettings.getString(DcRepository.Settings.stImportCharacterSet) : charSet;
    }
        
    private String getSeperator() {
        String sep = getSetting(_SEPERATOR);
        return sep == null ? DcSettings.getString(DcRepository.Settings.stImportSeperator) : sep; 
    }
    
    @Override
    protected void initialize() throws Exception {
        @SuppressWarnings("resource")
		InputStreamReader reader = new InputStreamReader(new FileInputStream(file), getCharacterSet());
        CSVReader csvReader = new CSVReader(reader, getSeperator());
        String[] headers = csvReader.readNext();
        
        Collection<String> fields = new ArrayList<String>();
        for (String field : headers)
            fields.add(field);

        try { if (csvReader != null) csvReader.close(); } catch (Exception e) {logger.error("Could not close resource");}
        try { if (reader != null) reader.close(); } catch (Exception e) {logger.error("Could not close resource");}
        
        mappings.setFields(moduleIdx, fields);
    }

    @Override
    public DcThread getTask() {
        return new Task(file, getModule(), mappings, client);
    }

    @Override
    public String[] getSupportedFileTypes() {
    	return new String[] {"csv", "txt", "data"};
    }

    @Override
    public String getName() {
        return DcResources.getText("lblXImport", "CSV");
    }
    
    private class Task extends DcThread {
        
        private File file;
        private ItemImporterFieldMappings mappings;
        private IItemImporterClient listener;
        private DcModule module;
        
        public Task(File file, DcModule module, ItemImporterFieldMappings mappings, IItemImporterClient listener) {
            super(null, "CVS Source Reader for " + file);
            this.file = file;
            this.module = module;
            this.listener = listener;
            this.mappings = mappings;
        }
        
        @Override
        public void run() {
            try {
                String characterSet = getCharacterSet();
                String seperator = getSeperator();
                seperator = seperator == null || seperator.equals("TAB") ? "\t" : seperator;
                
                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), characterSet);
                CSVReader csvReader = new CSVReader(reader, seperator);

                List<String[]> lines = csvReader.readAll();                        
                listener.notifyTaskStarted(lines.size() - 1);
                
                int counter = -1;
                for (String[] values : lines) {
                    counter++;
                    if (isCanceled()) break;
                    if (counter == 0) continue;
                    
                    DcObject dco = module.getItem();
                    for (int fieldIdx = 0; fieldIdx < values.length; fieldIdx++) {
                        
                        String value = values[fieldIdx];
                        DcField field = mappings.getTarget(fieldIdx);
                        
                        if (field == null) continue;
                        
                        if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                        	if (value.length() > 0) {
        	                	try {
                            		DcImageIcon icon = new DcImageIcon(new File(new URL(value).getFile()));
                                	Picture pic = new Picture(dco.getID(), icon);
                                	dco.addNewPicture(pic);
        	                	} catch (Exception e) {
        	                		listener.notifyError(e);
        	                	}                            	
                        	}
                        }                       	
                        
                        if ((   field.isUiOnly() && 
                                field.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION) ||  
                                field.getIndex() == DcObject._SYS_EXTERNAL_REFERENCES) continue;

                        setValue(dco, field.getIndex(), value, listener);
                    }
                    
                    try {
                        sleep(1000);
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                    
                    dco.setIDs();
                    listener.notifyProcessed(dco);
                }
                
                reader.close();
            } catch (Exception e) {
                listener.notify("Error while importing file " + file + ": " + e);
                logger.error("Error while importing file " + file, e);
            }  
            
            listener.notifyTaskCompleted(true, null);
            
            module = null;
            file = null;
            mappings = null;
            listener = null;
        }
    }
}
