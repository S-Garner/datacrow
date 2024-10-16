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

package org.datacrow.core.migration.itemexport;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.datacrow.core.DcConfig;
import org.datacrow.core.clients.IItemExporterClient;
import org.datacrow.core.migration.ItemMigrater;
import org.datacrow.core.modules.DcModule;

public abstract class ItemExporter extends ItemMigrater {
    
    protected Collection<String> items;
    protected BufferedOutputStream bos;

    protected IItemExporterClient client;
    protected ItemExporterSettings settings;
    
    protected int[] fields;
    
    protected boolean success = true;
    
    public ItemExporter(
    		int moduleIdx, 
    		String key, 
    		int mode, 
    		boolean processChildren) throws Exception {
    	
        super(DcConfig.getInstance().getConnector().getUser(), moduleIdx, key, mode, processChildren);
    }

    @Override
    public void start() throws Exception {
        client.notifyTaskStarted(items.size());
        success = true;
        super.start();
    }

    public void setClient(IItemExporterClient client) {
        this.client = client;
    }
    
    public void setItems(List<String> items) {
        this.items = items;
    }
    
    public List<Integer> getFields(DcModule m) {
		List<Integer> l = new LinkedList<Integer>();
		
		for (int fieldIdx : m.getFieldIndices()) {
			// for other modules; simply add all fields
			if (fields == null || m.getIndex() != moduleIdx) {
				l.add(fieldIdx);
			} else {
				// for the main modules; add fields as selected by the user in the same order as the XSD
				for (int fld : fields) {
					if (fieldIdx == fld)
						l.add(Integer.valueOf(fieldIdx));
				}
			}
		}
		
		return l;
    }

    public void setFields(int[] fields) {
        this.fields = fields;
    }

    public void setSettings(ItemExporterSettings properties) {
        this.settings = properties;
    }

    @Override
    protected void initialize() throws Exception {
        bos = new BufferedOutputStream(new FileOutputStream(file));
    }    
    
    public boolean isSuccessfull() {
        return success;
    }
    
    /**
     * The file type.
     * @return File extension.
     */
    public abstract String getFileType();
}
