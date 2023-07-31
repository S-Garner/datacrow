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
import java.util.ArrayList;
import java.util.Collection;

import org.datacrow.core.clients.IItemImporterClient;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.migration.ItemMigrater;
import org.datacrow.core.objects.DcObject;

public class ItemImporterHelper implements IItemImporterClient {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ItemImporterHelper.class.getName());
    
    private final Collection<DcObject> items = new ArrayList<DcObject>();
    private final File file;
    private final ItemImporter reader;
    
    public ItemImporterHelper(String type, int moduleIdx, File file) throws Exception {
        this.file = file;
        this.reader = ItemImporters.getInstance().getImporter(type, moduleIdx, ItemMigrater._MODE_NON_THREADED);
        this.reader.setClient(this);
    }

    public void setSetting(String key, String value) {
        reader.setSetting(key, value);
    }
    
    public void start() throws Exception {
        if (reader != null) {
            reader.setFile(file);
            reader.start();
        } else {
            logger.error("No source reader found for " + file);
        }
    }
    
    public Collection<DcObject> getItems() {
        return items;
    }
    
    @Override
    public void notifyProcessed(DcObject item) {
        items.add(item);
    }

    @Override
    public void notifyTaskStarted(int count) {}
    
    @Override
    public void notify(String message) {}
    
	@Override
	public void notifyWarning(String msg) {}

	@Override
	public void notifyError(Throwable e) {}

    @Override
    public boolean askQuestion(String msg) {
        return false;
    }
	
	@Override
	public void notifyTaskCompleted(boolean success, String taskID) {}

	@Override
	public void notifyProcessed() {}

	@Override
	public boolean isCancelled() {
		return false;
	}
}
