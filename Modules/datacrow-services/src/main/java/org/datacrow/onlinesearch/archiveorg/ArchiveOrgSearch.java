package org.datacrow.onlinesearch.archiveorg;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.datacrow.core.objects.DcObject;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.OnlineSearchUserError;
import org.datacrow.core.services.OnlineServiceError;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.plugin.IServer;

public class ArchiveOrgSearch extends SearchTask {
    
//    private static Logger logger = DcLogManager.getLogger(ArchiveOrgSearch.class.getName());
//
//    private final String userAgent = "DataCrow/" + DcConfig.getInstance().getVersion().toString() +  " +https://datacrow.org";
//    private final String address = "https://api.discogs.com/database";
//
//    private final Gson gson;
	
	private final String address = "https://archive.org/advancedsearch.php?fl[]=identifier&rows=100&output=json";
	// &q=dune+software
	
	// https://archive.org/metadata/dune-fifteen
    
    public ArchiveOrgSearch(
            IOnlineSearchClient listener, 
            IServer server, 
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, null, mode, query, additionalFilters);
        
        setMaximum(100);
        
//        GsonBuilder gsonBuilder = new GsonBuilder();
//        gson = gsonBuilder.create();
    }
    
    @Override
    protected DcObject getItem(Object key, boolean full) throws Exception {
        ArchiveOrgSearchResult dsr = (ArchiveOrgSearchResult) key;
        DcObject dco =  dsr.getDco();
        
//        waitBetweenRequest();
//        
//        HttpConnection conn = new HttpConnection(new URL(dsr.getDetailsUrl() + "?key=" + consumerKey + "&secret=" + consumerSecret), userAgent);
//        String json = conn.getString(StandardCharsets.UTF_8);
//        logUsageInformation(conn);
//        conn.close();
//        
//        Map<?, ?> src = gson.fromJson(json, Map.class);
//
//        dco.setValue(MusicAlbum._N_WEBPAGE, src.get("uri"));
//        
//        Double id = (Double) src.get("id");
//        dco.addExternalReference(DcRepository.ExternalReferences._DISCOGS, String.valueOf(id.intValue()));
//
//        setArtists(dco, src);
//        setRating(dco, src);
//        addTracks(dco, src);
//        
//        setServiceInfo(dco);
//        dco.setValue(Software._SYS_SERVICEURL, dsr.getDetailsUrl());
        
        return dco;
    }
    
    @Override
    protected DcObject getItem(URL url) throws Exception {
        return null;
    }

    @Override
    protected Collection<Object> getItemKeys() throws OnlineSearchUserError, OnlineServiceError {
        
        Collection<Object> result = new ArrayList<>();
        
/*        try {
            String query = address + "/search?title=" + getQuery() + "&type=release&"  +  "key=" + consumerKey + "&secret=" + consumerSecret;
            HttpConnection conn = new HttpConnection(new URL(query), userAgent);
            //logUsageInformation(conn);
            
            String json = conn.getString(StandardCharsets.UTF_8);
            conn.close();
            
            Map<?, ?> musicalbums = gson.fromJson(json, Map.class);
            ArrayList<LinkedTreeMap<?, ?>> albums = (ArrayList<LinkedTreeMap<?, ?>>) musicalbums.get("results");
            
            ArchiveOrgSearchResult aosr;
            MusicAlbum musicalbum;
            
            int count = 0;
            for (LinkedTreeMap<?, ?> src : albums) {
                musicalbum = new MusicAlbum();
                
                musicalbum.setValue(MusicAlbum._A_TITLE, src.get("title"));
                musicalbum.setValue(MusicAlbum._C_YEAR, src.get("year"));
                
                setCountry(musicalbum, src);
                setRecordLabel(musicalbum, src);
                setGenres(musicalbum, src);
                setStorageMedium(musicalbum, src);
                setEAN(musicalbum, src);
                
                dsr = new DiscogsSearchResult(musicalbum);
                dsr.setCoverUrl((String) src.get("cover_image"));
                dsr.setDetailsUrl((String) src.get("resource_url"));
                
                result.add(dsr);
                
                count++;
                
                if (count == getMaximum()) break;                
            }
        } catch (Exception e) {
            throw new OnlineServiceError(e);
        } */
        
        return result;
    }
}
