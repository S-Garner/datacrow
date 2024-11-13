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

package org.datacrow.core.synchronizers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.datacrow.core.DcRepository;
import org.datacrow.core.clients.ISynchronizerClient;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.services.OnlineSearchHelper;
import org.datacrow.core.utilities.CoreUtilities;

public abstract class Synchronizer implements Serializable {
    
	private static final long serialVersionUID = 1L;

    protected ISynchronizerClient client; 

    public static final int _ALL = 0;
    public static final int _SELECTED = 1;

    private final String title;
    protected final int module;
    
    public Synchronizer(String title, int module) {
        this.title = title;
        this.module = module;
    }
    
    public abstract Thread getTask();
    
    public abstract String getHelpText();
    
    public abstract boolean canParseFiles();
    public abstract boolean canUseOnlineServices();
    
    public  String getTitle() {
        return title;
    }

    public String getHelpIndex() {
        return "dc.tools.massupdate";
    }
    
    public abstract Synchronizer getInstance();
    
    public abstract boolean onlineUpdate(ISynchronizerClient client, DcObject dco);
    
    /**
     * Executed before the online update.
     * @param dco
     */
    protected boolean parseFiles(DcObject dco) {
        return false;
    }

    /**
     * Merges the data of the source and the target with regard of the settings.
     */
    public void merge(DcObject target, DcObject source) {
        merge(target, source, null);
    }

    /**
     * Merges the data of the source and the target with regard of the settings.
     * The online search helper is used to query additional data when needed.
     */
    protected void merge(DcObject target, DcObject source, OnlineSearchHelper osh) {
        if (source == null) return;
        
        // fetches the item using the service URL:
        DcObject queried = osh != null ? osh.query(source) : source;
        queried = queried == null ? source : queried;
        
        // External references need to be merged manually - not part of the field settings
        if (target.getField(DcObject._SYS_EXTERNAL_REFERENCES) != null)
            target.setValue(DcObject._SYS_EXTERNAL_REFERENCES, queried.getValue(DcObject._SYS_EXTERNAL_REFERENCES));
        
        for (int field : queried.getFieldIndices()) {
        	
        	if (field == DcObject._ID)
        		continue;
        	
            setValue(target, field, queried.getValue(field));
        }
        
        target.addNewPictures(source.getNewPictures());
    }
    
    public void synchronize(ISynchronizerClient client) {
        this.client = client;
        
        Thread thread = getTask();
        thread.start();
    }
    
    protected void setValue(DcObject dco, int field, Object value) {
    	
        // empty value, no need to update
        if (CoreUtilities.isEmpty(value))
            return;
        
        boolean overwrite = dco.getModule().getSettings().getBoolean(DcRepository.ModuleSettings.stOnlineSearchOverwrite);
        int[] fields = overwrite ?
                       dco.getModule().getSettings().getIntArray(DcRepository.ModuleSettings.stOnlineSearchFieldOverwriteSettings) :
                       dco.getModule().getFieldIndices();
            
       // if all fails, just update all..
       if (fields == null || fields.length == 0)
           fields = dco.getModule().getFieldIndices();
                       
        boolean allowed = false;
        for (int i = 0;i < fields.length; i++)
            allowed |= fields[i] == field;
        
        if (allowed) {
            if ((dco.isFilled(field) && overwrite) || !dco.isFilled(field)) {
                if (value instanceof Collection) {
                    dco.setValue(field, null);
                    for (Iterator<?> iter = ((Collection<?>) value).iterator(); iter.hasNext(); ) {
                        DcObject o = (DcObject) iter.next();
                        if (o instanceof DcMapping) {
                            Collection<?> c = (Collection<?>) dco.getValue(field);
                            c = c == null ? new ArrayList<DcMapping>() : c;
                            dco.createReference(field, ((DcMapping) o).getReferencedObject());
                        } else {
                        	dco.createReference(field, o);
                        }
                    }
                } else {
                    dco.setValue(field, value);    
                }                
            }
        }
    }
}