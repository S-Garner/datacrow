package org.datacrow.onlinesearch.discogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.services.FilterField;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.Region;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.settings.Setting;

public class DiscogsServer implements IServer {
    
    private static final long serialVersionUID = 6451130355747891181L;

    private Collection<Region> regions = new ArrayList<Region>();
    private Collection<SearchMode> modes = new ArrayList<SearchMode>();

    public DiscogsServer() {
        regions.add(new Region("en", "English", "https://www.discogs.com/"));
    }

    @Override
    public int getModule() {
        return DcModules._MUSIC_ALBUM;
    }

    @Override
    public Collection<Setting> getSettings() {
        return null;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public boolean isFullModeOnly() {
        return false;
    }
    
    @Override
    public Collection<FilterField> getFilterFields() {
        return new ArrayList<>();
    }
    
    @Override
    public String getName() {
        return "Discogs";
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
        return "http://www.discogs.com";
    }
    
    @Override
    public SearchTask getSearchTask(
            IOnlineSearchClient listener,
            SearchMode mode,
            Region region,
            String query,
            Map<String, Object> additionalFilters,
            DcObject client) {
        
        DiscogsSearch task = new DiscogsSearch(listener, this, mode, query, additionalFilters);
        task.setClient(client);
        return task;
    }
    
    @Override
    public String toString() {
        return getName();
    }

    @Override
    public long getWaitTimeBetweenRequest() {
        return 1000l;
    }
}