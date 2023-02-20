package org.datacrow.onlinesearch.archiveorg;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Software;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.OnlineSearchUserError;
import org.datacrow.core.services.OnlineServiceError;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.plugin.IServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

public class ArchiveOrgSearch extends SearchTask {
    
    // private static Logger logger = DcLogManager.getLogger(ArchiveOrgSearch.class.getName());

    private final String userAgent = "DataCrow/" + DcConfig.getInstance().getVersion().toString() +  " +https://datacrow.org";
    private final Gson gson;
	
	private final String address = 
			"https://archive.org/advancedsearch.php?fl[]=identifier&fl[]=avg_rating&fl[]=collection&fl[]=date&fl[]=description&fl[]=format&fl[]=language&" +
			"fl[]=mediatype&fl[]=name&fl[]=subject&fl[]=title&fl[]=type&fl[]=volume&fl[]=week&fl[]=year&rows=100&output=json";
			
    public ArchiveOrgSearch(
            IOnlineSearchClient listener, 
            IServer server, 
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, null, mode, query, additionalFilters);
        
        setMaximum(100);
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }
    
    @Override
    protected DcObject getItem(Object key, boolean full) throws Exception {
        ArchiveOrgSearchResult dsr = (ArchiveOrgSearchResult) key;
        DcObject dco =  dsr.getDco();
        
        String address = "https://archive.org/metadata/" + dsr.getId();
        
        HttpConnection conn = new HttpConnection(new URL(address), userAgent);
        String json = conn.getString(StandardCharsets.UTF_8);
        conn.close();

        dco.addExternalReference(DcRepository.ExternalReferences._ARCHIVEORG, String.valueOf(dsr.getId()));
        setServiceInfo(dco);
        dco.setValue(Software._SYS_SERVICEURL, address);
        
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
        
        try {
            String query = address + "&q=title:%22" + getQuery() + "%22%20AND%20mediatype:%22software%22";
            HttpConnection conn = new HttpConnection(new URL(query), userAgent);
            
            String json = conn.getString(StandardCharsets.UTF_8);
            conn.close();
            
            Map<?, ?> musicalbums = gson.fromJson(json, Map.class);
            
            ArrayList<LinkedTreeMap<?, ?>> results = 
            		(ArrayList<LinkedTreeMap<?, ?>>)  ((LinkedTreeMap<?, ?>) musicalbums.get("response")).get("docs");
            
            ArchiveOrgSearchResult aosr;
            DcObject dco;
            
            int count = 0;
            for (LinkedTreeMap<?, ?> src : results) {
            	dco = new Software();
            	
            	dco.setValue(Software._A_TITLE, src.get("title"));
            	dco.setValue(Software._B_DESCRIPTION, src.get("description"));
            	
            	aosr = new ArchiveOrgSearchResult(dco);
            	aosr.setId((String) src.get("identifier"));
            	
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
