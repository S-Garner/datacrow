package org.datacrow.onlinesearch.discogs.task;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.DcRepository.ExternalReferences;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.http.HttpConnectionUtil;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcAssociate;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.MusicAlbum;
import org.datacrow.core.objects.helpers.MusicTrack;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.OnlineSearchUserError;
import org.datacrow.core.services.OnlineServiceError;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.Servers;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.onlinesearch.discogs.helpers.DiscogsArtistCache;
import org.datacrow.onlinesearch.discogs.helpers.DiscogsSearchResult;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

public class DiscogsSearch extends SearchTask {
    
    private static Logger logger = DcLogManager.getLogger(DiscogsSearch.class.getName());

    private final String userAgent = "DataCrow/" + DcConfig.getInstance().getVersion().toString() +  " +https://datacrow.org";
    private final String address = "https://api.discogs.com/database";
    private final String consumerKey;
    private final String consumerSecret;

    private final Gson gson;
    
    private final DiscogsArtistCache artistCache = new DiscogsArtistCache();
    
    public DiscogsSearch(
            IOnlineSearchClient listener, 
            IServer server, 
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, null, mode, query, additionalFilters);
        
        consumerKey = Servers.getInstance().getApiKey("discogs-consumer_key");
        consumerSecret = Servers.getInstance().getApiKey("discogs-consumer_secret");
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }
    
    private void logUsageInformation(HttpConnection conn) {
        Map<String, List<String>> responseHeaders = conn.getResponseHeaders();
        logger.debug(
                "The request limit per minute is: " + 
                responseHeaders.get("X-Discogs-Ratelimit") + ", used: " + 
                responseHeaders.get("X-Discogs-Ratelimit-Used") + ", remaining: " + 
                responseHeaders.get("X-Discogs-Ratelimit-Remaining"));
    }

    @Override
    protected DcObject getItem(Object key, boolean full) throws Exception {
        DiscogsSearchResult dsr = (DiscogsSearchResult) key;
        DcObject dco =  dsr.getDco();
        
        waitBetweenRequest();
        
        setCoverImage(dco, dsr.getCoverUrl());
        
        waitBetweenRequest();
        
        HttpConnection conn = new HttpConnection(new URL(dsr.getDetailsUrl() + "?key=" + consumerKey + "&secret=" + consumerSecret), userAgent);
        String json = conn.getString(StandardCharsets.UTF_8);
        logUsageInformation(conn);
        conn.close();
        
        Map<?, ?> src = gson.fromJson(json, Map.class);

        dco.setValue(MusicAlbum._N_WEBPAGE, src.get("uri"));
        
        Double id = (Double) src.get("id");
        dco.addExternalReference(DcRepository.ExternalReferences._DISCOGS, String.valueOf(id.intValue()));

        setArtists(dco, src);
        setRating(dco, src);
        
        addTracks(dco, src);
        
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
            String query = address + "/search?title=" + getQuery() + "&type=release&"  +  "key=" + consumerKey + "&secret=" + consumerSecret;
            HttpConnection conn = new HttpConnection(new URL(query), userAgent);
            logUsageInformation(conn);
            
            String json = conn.getString(StandardCharsets.UTF_8);
            conn.close();
            
            Map<?, ?> musicalbums = gson.fromJson(json, Map.class);
            ArrayList<LinkedTreeMap<?, ?>> albums = (ArrayList<LinkedTreeMap<?, ?>>) musicalbums.get("results");
            
            DiscogsSearchResult dsr;
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
        }
        
        return result;
    }
    
    private void addTracks(DcObject dco,  Map<?, ?> albumData) {
        @SuppressWarnings("unchecked")
        Collection<LinkedTreeMap<?, ?>> tracksData = (Collection<LinkedTreeMap<?, ?>>) albumData.get("tracklist");
        
        MusicTrack mt;
        for (LinkedTreeMap<?, ?> trackData : tracksData) {
            
            if (trackData.get("type_") == null || !trackData.get("type_").equals("track"))
                continue;
            
            mt = new MusicTrack();
            
            mt.setValue(MusicTrack._F_TRACKNUMBER, trackData.get("position"));
            mt.setValue(MusicTrack._A_TITLE, trackData.get("title"));
            
            setPlaylength(mt, trackData);
            
            setArtists(mt, trackData);
            
            if (!mt.isFilled(MusicTrack._G_ARTIST)) {
                setArtists(mt, albumData);
            }
            
            dco.addChild(mt);
        }        
    }    
    
    @SuppressWarnings("unchecked")
    private void setArtists(DcObject dco, Map<?, ?> src) {
        Collection<LinkedTreeMap<?, ?>> artistsData = (Collection<LinkedTreeMap<?, ?>>) src.get("artists");

        if (artistsData == null)
            return;
        
        DcAssociate artist;
        String artistId;
        
        for (LinkedTreeMap<?, ?> artistData : artistsData) {
            artistId = String.valueOf(((Double) artistData.get("id")).intValue());
            
            if (artistCache.contains(artistId)) {
                artist = artistCache.getArtist(artistId);
            } else {
                artist = new DcAssociate(DcModules._MUSICARTIST);
                
                artist.addExternalReference(ExternalReferences._DISCOGS, artistId);
                artist.setValue(DcAssociate._A_NAME, artistData.get("name"));
                artist.setValue(DcAssociate._C_WEBPAGE, "https://www.discogs.com/artist/" + artistId);
                artist.setIDs();
                
                artistCache.addArtist(artist, artistId);
            }
            
            dco.createReference(
                    dco.getModuleIdx() == DcModules._MUSIC_ALBUM ? 
                            MusicAlbum._F_ARTISTS : MusicTrack._G_ARTIST, artist);
        }
    }    
    
    private void setPlaylength(DcObject track, LinkedTreeMap<?, ?> src) {
        String duration = (String) src.get("duration");
        if (!CoreUtilities.isEmpty(duration)) {
            String[] parts = duration.split(":");
            int hours =   parts.length == 3 ? Integer.valueOf(parts[0]) : 0;
            int minutes = parts.length > 1 ? Integer.valueOf(parts[parts.length - 2]) : 0;
            int seconds = parts.length > 0 ? Integer.valueOf(parts[parts.length - 1]) : 0;
            
            track.setValue(
                    MusicTrack._J_PLAYLENGTH, 
                    Integer.valueOf(seconds + (minutes * 60) + (hours * 60 * 60)));
        }
    }
    
    private void setCoverImage(DcObject musicalbum, String url) {
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
    private void setEAN(DcObject musicalbum, LinkedTreeMap<?, ?> src) {
        if (!CoreUtilities.isEmpty(src.get("barcode"))) {
            for (String barcode : (Collection<String>) src.get("barcode")) {
                musicalbum.setValue(MusicAlbum._P_EAN, barcode);
                break;
            }
        }
    }    
    
    private void setCountry(DcObject musicalbum, LinkedTreeMap<?, ?> src) {
        if (!CoreUtilities.isEmpty(src.get("country")))
            musicalbum.createReference(MusicAlbum._F_COUNTRY, src.get("country"));
    }
    
    private void setRating(DcObject ma, Map<?, ?> albumData) {
        LinkedTreeMap<?, ?> communityData = (LinkedTreeMap<?, ?>) albumData.get("community");
        
        LinkedTreeMap<?, ?> rating = communityData != null ? (LinkedTreeMap<?, ?>) communityData.get("rating") : null;
        Double avg = rating != null ? (Double) rating.get("average") : null;
        
        if (avg != null) {
            ma.setValue(MusicAlbum._E_RATING, Math.round(avg.intValue() * 2));
        }
    }
    
    @SuppressWarnings("unchecked")
    private void setGenres(DcObject musicalbum, LinkedTreeMap<?, ?> src) {
        if (!CoreUtilities.isEmpty(src.get("genre"))) {
            for (String genre : (Collection<String>) src.get("genre"))
                musicalbum.createReference(MusicAlbum._G_GENRES, genre);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void setRecordLabel(DcObject musicalbum, LinkedTreeMap<?, ?> src) {
        if (!CoreUtilities.isEmpty(src.get("label"))) {
            for (String label : (Collection<String>) src.get("label")) {
                musicalbum.createReference(MusicAlbum._Q_RECORDLABEL, label);
                break;
            }
        }
    }     
    
    @SuppressWarnings("unchecked")
    private void setStorageMedium(DcObject musicalbum, LinkedTreeMap<?, ?> src) {
        if (!CoreUtilities.isEmpty(src.get("format"))) {
            for (String medium : (Collection<String>) src.get("format")) {
                medium = medium.toUpperCase().equals("CD") ? "Audio CD" : medium;
                musicalbum.createReference(MusicAlbum._I_STORAGEMEDIUM, medium);
                break;
            }
        }
    }    
}
