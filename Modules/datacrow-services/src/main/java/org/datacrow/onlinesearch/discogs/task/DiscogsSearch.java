package org.datacrow.onlinesearch.discogs.task;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.http.HttpConnectionUtil;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.MusicAlbum;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.OnlineSearchUserError;
import org.datacrow.core.services.OnlineServiceError;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.Servers;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.utilities.CoreUtilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

public class DiscogsSearch extends SearchTask {
    
    private static Logger logger = DcLogManager.getLogger(DiscogsSearch.class.getName());

    private final String userAgent = "DataCrow/4.5 +https://datacrow.org";
    private final String address = "https://api.discogs.com/database";
    private final String consumerKey;
    private final String consumerSecret;
    
    public DiscogsSearch(
            IOnlineSearchClient listener, 
            IServer server, 
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, null, mode, query, additionalFilters);
        
        consumerKey = Servers.getInstance().getApiKey("discogs-consumer_key");
        consumerSecret = Servers.getInstance().getApiKey("discogs-consumer_secret");
    }

    @Override
    protected DcObject getItem(Object key, boolean full) throws Exception {
        return (DcObject) key;
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
            String query = address + "/search?title=" + getQuery() + "&type=master&"  +  "key=" + consumerKey + "&secret=" + consumerSecret;
            HttpConnection conn = new HttpConnection(new URL(query), userAgent);
            
            Map<String, List<String>> responseHeaders = conn.getResponseHeaders();
            logger.debug("The request limit - per minute is [" + responseHeaders.get("X-Discogs-Ratelimit") + "]");
            logger.debug("The request limit - used rate limit [" + responseHeaders.get("X-Discogs-Ratelimit-Used") + "]");
            logger.debug("The request limit - remaining rate limit [" + responseHeaders.get("X-Discogs-Ratelimit-Remaining") + "]");
            
            String json = new String(conn.getBytes(), StandardCharsets.UTF_8);
            
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();

            Map<?, ?> musicalbums = gson.fromJson(json, Map.class);
            ArrayList<LinkedTreeMap<?, ?>> albums = (ArrayList<LinkedTreeMap<?, ?>>) musicalbums.get("results");
            
            MusicAlbum musicalbum;
            
            int count = 0;
            for (LinkedTreeMap<?, ?> src : albums) {
                musicalbum = new MusicAlbum();
                
                musicalbum.setValue(MusicAlbum._A_TITLE, src.get("title"));
                musicalbum.setValue(MusicAlbum._C_YEAR, src.get("year"));
                musicalbum.setValue(MusicAlbum._N_WEBPAGE, "https://discogs.com" + src.get("uri"));
                
                setCountry(musicalbum, src);
                setRecordLabel(musicalbum, src);
                setGenres(musicalbum, src);
                setStorageMedium(musicalbum, src);
                setEAN(musicalbum, src);
                
                setCoverImage(musicalbum, src);
                
                result.add(musicalbum);
                
                count++;
                
                if (count == getMaximum()) break;                
            }
        } catch (Exception e) {
            throw new OnlineServiceError(e);
        }
        
        return result;
    }
    
    private void setCoverImage(MusicAlbum musicalbum, LinkedTreeMap<?, ?> src) {
        String url = (String) src.get("cover_image");
        try {
            if (url != null && url.length() > 0) {
                byte[] b = HttpConnectionUtil.retrieveBytes(url);
                if (b != null && b.length > 50)
                    musicalbum.setValue(MusicAlbum._J_PICTUREFRONT, b);
            }
        } catch (Exception e) {
            logger.debug("Cannot download image from [" + url + "]", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void setEAN(MusicAlbum musicalbum, LinkedTreeMap<?, ?> src) {
        if (!CoreUtilities.isEmpty(src.get("barcode"))) {
            for (String barcode : (Collection<String>) src.get("barcode")) {
                musicalbum.setValue(MusicAlbum._P_EAN, barcode);
                break;
            }
        }
    }    
    
    private void setCountry(MusicAlbum musicalbum, LinkedTreeMap<?, ?> src) {
        if (!CoreUtilities.isEmpty(src.get("country")))
            musicalbum.createReference(MusicAlbum._F_COUNTRY, src.get("country"));
    }    
    
    @SuppressWarnings("unchecked")
    private void setGenres(MusicAlbum musicalbum, LinkedTreeMap<?, ?> src) {
        if (!CoreUtilities.isEmpty(src.get("genre"))) {
            for (String genre : (Collection<String>) src.get("genre"))
                musicalbum.createReference(MusicAlbum._G_GENRES, genre);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void setRecordLabel(MusicAlbum musicalbum, LinkedTreeMap<?, ?> src) {
        if (!CoreUtilities.isEmpty(src.get("label"))) {
            for (String label : (Collection<String>) src.get("label")) {
                musicalbum.createReference(MusicAlbum._Q_RECORDLABEL, label);
                break;
            }
        }
    }     
    
    @SuppressWarnings("unchecked")
    private void setStorageMedium(MusicAlbum musicalbum, LinkedTreeMap<?, ?> src) {
        if (!CoreUtilities.isEmpty(src.get("format"))) {
            for (String medium : (Collection<String>) src.get("format")) {
                musicalbum.createReference(MusicAlbum._I_STORAGEMEDIUM, medium);
                break;
            }
        }
    }    
}
