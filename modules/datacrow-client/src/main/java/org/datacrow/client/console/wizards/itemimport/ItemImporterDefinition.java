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

package org.datacrow.client.console.wizards.itemimport;

import java.io.File;
import java.util.Collection;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.migration.itemimport.ItemImporter;
import org.datacrow.core.migration.itemimport.ItemImporterFieldMappings;
import org.datacrow.core.migration.itemimport.ItemImporters;
import org.datacrow.core.migration.itemimport.ItemImporters.ImporterType;
import org.datacrow.core.modules.DcModules;

public class ItemImporterDefinition {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ItemImporterDefinition.class.getName());
    
    private int moduleIdx;
    private File file;
    
    private ItemImporterFieldMappings mappings;
    
    private ItemImporter template;
    private ImporterType type;
    
    public int getModule() {
		return moduleIdx;
	}

	public void setModule(int moduleIdx) {
		this.moduleIdx = moduleIdx;
	}
	
	public void setType(ImporterType type) {
		this.type = type;
		// we'll just get an importer - for the current module. They're all the same in the end
		try {
			this.template = ItemImporters.getInstance().getImporter(type, DcModules.getCurrent().getIndex());
		} catch (Exception e) {
			logger.error(e, e);
		}
	}
	
	public Collection<String> getSettingKeys() {
		return template.getSettingKeys();
	}
	
	public String[] getSupportedFileTypes() {
		return template.getSupportedFileTypes();
	}	
	
	public ImporterType getType() {
		return type;
	}

	public ItemImporterDefinition() {}
	
	public ItemImporterFieldMappings getMappings() {
		return mappings;
	}
	
    public void setFile(File file) {
        this.file = file;
        
        try {
        	ItemImporter importer = ItemImporters.getInstance().getImporter(type, moduleIdx); 
        	importer.setFile(file);
        	mappings = importer.getSourceMappings();
		} catch (Exception e) {
			logger.error(e, e);
		}
    }

    public File getFile() {
        return file;
    }
}
