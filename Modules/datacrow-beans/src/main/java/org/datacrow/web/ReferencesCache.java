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

package org.datacrow.web;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacrow.core.DcConfig;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcSimpleValue;
import org.datacrow.core.server.Connector;
import org.datacrow.web.model.Reference;

public class ReferencesCache {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ReferencesCache.class.getName());
    
    private Map<Integer, List<Reference>> references = new HashMap<Integer, List<Reference>>();
    private Map<Integer, Date> referencesDateStored = new HashMap<Integer, Date>();
    private Map<Integer, Boolean> referencesForceRefresh = new HashMap<Integer, Boolean>();
    
    private static ReferencesCache me;
    
    private final File iconDir;
    
    static {
        me = new ReferencesCache();
    }
    
    public static ReferencesCache getInstance() {
        return me;
    }
    
    public void forceRefresh(int moduleIdx) {
        referencesForceRefresh.put(Integer.valueOf(moduleIdx), Boolean.TRUE);
    }
    
    private ReferencesCache() {
        iconDir = new File(DcConfig.getInstance().getWebDir(), "datacrow/icons/");
        iconDir.mkdir();
    }
    
    private boolean refresh(int moduleIdx) {
        // refresh each ten minutes (approximately)
        return referencesForceRefresh.get(Integer.valueOf(moduleIdx)) ||
              (referencesDateStored.get(Integer.valueOf(moduleIdx)).getTime() - new Date().getTime()) > 60 * 10 * 1000;
    }
    
    public List<Reference> getReferences(int moduleIdx) {
        if (references.containsKey(Integer.valueOf(moduleIdx)) && !refresh(Integer.valueOf(moduleIdx))) {
            return references.get(Integer.valueOf(moduleIdx));
        } else {
            ArrayList<Reference> values = new ArrayList<Reference>();
            Connector conn = DcConfig.getInstance().getConnector();
            for (DcSimpleValue sv : conn.getSimpleValues(moduleIdx, true)) {
                if (sv.getName() != null && sv.getName().length() > 0) 
                    values.add(new Reference(sv.getName(), sv.getID(), moduleIdx));
                else
                    logger.debug("Could not load reference: empty label. ID: " + sv.getID() + " module " + moduleIdx);
            }
            
            references.put(Integer.valueOf(moduleIdx), values);
            referencesDateStored.put(Integer.valueOf(moduleIdx), new Date());
            referencesForceRefresh.put(Integer.valueOf(moduleIdx), Boolean.FALSE);
            
            return values;
        }
    }
}
