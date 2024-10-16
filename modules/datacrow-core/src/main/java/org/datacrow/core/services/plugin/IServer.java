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

package org.datacrow.core.services.plugin;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.services.FilterField;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.Region;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.settings.Setting;

/**
 * This interface should be implemented for new (and custom) online services.
 * The IServer class holds all information for a specific server such as its supported
 * search modes and regions and the external location (URL).
 *  
 * @author Robert Jan van der Waals
 */
public interface IServer extends Serializable {

    /**
     * The module to which this server belongs.
     * @see DcModules
     * @see DcModule
     */
    int getModule();
    
    /**
     * The regions belonging to this server. 
     * @return Collection of regions or an empty collection. 
     */
    java.util.Collection<Region> getRegions();
    
    /**
     * The search modes belonging to this server.
     * @return Collection of search modes or an empty collection.
     */
    java.util.Collection<SearchMode> getSearchModes();
    
    Collection<FilterField> getFilterFields();
    
    /**
     * Name of the server (must be unique)
     * @return Unique name of the server
     */
    String getName();
    
    long getWaitTimeBetweenRequest();
    
    /**
     * Is the service available?
     * @return
     */
    boolean isEnabled();
    
    /**
     * Specific settings for this server. 
     * Returns null of there are no settings available.
     */
    Collection<Setting> getSettings();
    
    /**
     * The URL of the main server.
     */
    String getUrl();
    
    /**
     * Retrieves an instance of the search task. This task will be used to perform
     * the actual search.
     * @see SearchTask
     * @see SearchMode
     * @see Region
     * @param listener The class which requested the search. This class will be informed of 
     * errors and events.
     * @param mode The selected search mode.
     * @param region The selected region.
     * @param query The query as specified by the user.
     */
    SearchTask getSearchTask(IOnlineSearchClient listener, 
                             SearchMode mode, 
                             Region region,
                             String query,
                             Map<String, Object> additionalFilters,
                             DcObject client);    
}