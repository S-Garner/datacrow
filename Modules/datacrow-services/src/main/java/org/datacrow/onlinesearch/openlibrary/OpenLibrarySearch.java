package org.datacrow.onlinesearch.openlibrary;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.datacrow.core.DcRepository;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcMediaObject;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.OnlineSearchUserError;
import org.datacrow.core.services.OnlineServiceError;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.plugin.IServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

public class OpenLibrarySearch extends SearchTask {
    
    protected final Gson gson;
	
    public OpenLibrarySearch(
            IOnlineSearchClient listener, 
            IServer server, 
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, null, mode, query, additionalFilters);
        
        setMaximum(50);
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    @Override
    protected DcObject getItem(Object key, boolean full) throws Exception {
        OpenLibrarySearchResult olsr = (OpenLibrarySearchResult) key;
        DcObject dco = olsr.getDco();
        
        String address = "";
//        
//        waitBetweenRequest();
//        
//        HttpConnection conn = new HttpConnection(new URL(address), userAgent);
//        String json = conn.getString(StandardCharsets.UTF_8);
//        conn.close();
//
//        Map<?, ?> item = gson.fromJson(json, Map.class);
//        
//        DcObject dco = parseItem(item, aosr);
//        
//        Map<?, ?> metadata = (Map<?, ?>) item.get("metadata");
//
//        if (metadata != null) {
//	        setYear(dco, metadata);
//	        setExtendedDescription(dco, metadata);
//	        setLanguage(dco, metadata);
//        }
        
        dco.addExternalReference(DcRepository.ExternalReferences._OPENLIBRARY, 
				"work-" + olsr.getWork() + "#edition-" + olsr.getEdition());        		
        		
        setServiceInfo(dco);
        dco.setValue(DcObject._SYS_SERVICEURL, address);
        
        return dco;
    }
    
    @Override
    protected DcObject getItem(URL url) throws Exception {
        return null;
    }
    
    @SuppressWarnings("unchecked")
	@Override
    protected Collection<Object> getItemKeys() throws OnlineSearchUserError, OnlineServiceError {
        Collection<Object> result = new ArrayList<>();
        
        waitBetweenRequest();
        
        try {
            String query = "https://openlibrary.org/search.json?q=" + getQuery();
            HttpConnection conn = new HttpConnection(new URL(query), userAgent);
            String json = conn.getString(StandardCharsets.UTF_8);
            conn.close();
            
            Map<?, ?> m = gson.fromJson(json, Map.class);
            
            ArrayList<LinkedTreeMap<?, ?>> items = (ArrayList<LinkedTreeMap<?, ?>>) m.get("docs");
            
    		OpenLibrarySearchResult aosr;
            DcObject dco;
            int count = 0;
            for (LinkedTreeMap<?, ?> src : items) {
            	dco = DcModules.get(getServer().getModule()).getItem();
            	
            	dco.setValue(DcMediaObject._A_TITLE, src.get("title"));
            	
            	aosr = new OpenLibrarySearchResult(dco);
            	//aosr.setId((String) src.get("identifier"));
            	
                count++;
                
                result.add(aosr);
                
                if (count == getMaximum()) break;                
            }
        } catch (Exception e) {
            throw new OnlineServiceError(e);
        }
        
        return result;
    }
}