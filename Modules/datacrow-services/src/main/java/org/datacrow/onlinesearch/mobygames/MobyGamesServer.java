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

package org.datacrow.onlinesearch.mobygames;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.services.FilterField;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.Region;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.settings.Setting;


public class MobyGamesServer implements IServer {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(MobyGamesServer.class.getName());
    
    private static final long serialVersionUID = 1L;

    private Collection<Region> regions = new ArrayList<Region>();
    private Collection<SearchMode> modes = new ArrayList<SearchMode>();

    private final List<MobyGamesPlatform> platforms = new ArrayList<>();

    public MobyGamesServer() {
        regions.add(new Region("en", "English", "https://mobygames.com/"));
        platforms.add(new MobyGamesPlatform("", ""));
        
        InputStream is = null;
        
        try {
            Properties p = new Properties();
            is = DcResources.class.getResourceAsStream("MobyGamesPlatforms.properties");
            p.load(is);
            
            Enumeration<?> enums = p.propertyNames();
            while (enums.hasMoreElements()) {
                String key = enums.nextElement().toString();
                String value = p.getProperty(key);
                platforms.add(new MobyGamesPlatform(key, value));
            }
            
            platforms.sort(Comparator.comparing(MobyGamesPlatform::getDescription));
        
        } catch (Exception e) {
            logger.error(e);
        } finally {
        	try { if (is != null) is.close(); } catch (Exception e) {logger.error("Could not close moby platforms file input stream");}
        }
    }

    @Override
    public int getModule() {
        return DcModules._SOFTWARE;
    }

    @Override
    public long getWaitTimeBetweenRequest() {
        return 1100l;
    }
    
    @Override
    public Collection<Setting> getSettings() {
        Collection<Setting> settings = new ArrayList<>();
        settings.add(DcSettings.getSetting(DcRepository.Settings.stMobyGamesApiKey));
        return settings;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public String getName() {
        return "Mobygames.com";
    }

    @Override
    public Collection<Region> getRegions() {
        return regions;
    }

    @Override
    public Collection<SearchMode> getSearchModes() {
        return modes;
    }

    @Override
    public String getUrl() {
        return "https://mobygames.com/";
    }
    
    @Override
    public Collection<FilterField> getFilterFields() {
        Collection<FilterField> fields = new ArrayList<>();
        fields.add(new FilterField(DcResources.getText("lblPlatform"), platforms));
        return fields;
    }
    
    @Override
    public SearchTask getSearchTask(
            IOnlineSearchClient listener,
            SearchMode mode,
            Region region,
            String query,
            Map<String, Object> additionalFilters,
            DcObject client) {
            
        MobyGamesSearch task = new MobyGamesSearch(listener, this, mode, query, additionalFilters);
        task.setClient(client);
        return task;
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
