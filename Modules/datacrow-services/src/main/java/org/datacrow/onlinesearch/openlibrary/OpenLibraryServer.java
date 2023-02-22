package org.datacrow.onlinesearch.openlibrary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.services.FilterField;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.Region;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.settings.Setting;

public class OpenLibraryServer implements IServer {
    
    private static final long serialVersionUID = 6451130355747891181L;

    private Collection<Region> regions = new ArrayList<Region>();
    private Collection<SearchMode> modes = new ArrayList<SearchMode>();

    public OpenLibraryServer() {
        regions.add(new Region("en", "English", "https://www.openlibrary.org/"));
    }
    
    @Override
    public int getModule() {
        return DcModules._BOOK;
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
    public String getName() {
        return "Open Library";
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
        return "https://www.openlibrary.org";
    }
    
    @Override
    public SearchTask getSearchTask(
            IOnlineSearchClient listener,
            SearchMode mode,
            Region region,
            String query,
            Map<String, Object> additionalFilters,
            DcObject client) {
        
        OpenLibrarySearch task = new OpenLibrarySearch(listener, this, mode, query, additionalFilters);
        task.setClient(client);
        return task;
    }
    
    @Override
    public String toString() {
        return getName();
    }

    @Override
    public long getWaitTimeBetweenRequest() {
        return 500l;
    }
    
    @Override
    public Collection<FilterField> getFilterFields() {
        Collection<FilterField> fields = new ArrayList<>();
        fields.add(new FilterField(DcResources.getText("lblCollection"), null));
        return fields;
    }    
}