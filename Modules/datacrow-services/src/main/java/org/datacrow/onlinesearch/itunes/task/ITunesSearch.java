package org.datacrow.onlinesearch.itunes.task;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.Logger;
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
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.onlinesearch.itunes.helpers.ITunesArtistCache;
import org.datacrow.onlinesearch.itunes.helpers.ITunesSearchResult;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

public class ITunesSearch extends SearchTask {
    
    private static Logger logger = DcLogManager.getLogger(ITunesSearch.class.getName());
    private final Gson gson;
    private final ITunesArtistCache artistCache = new ITunesArtistCache();
    
    public ITunesSearch(
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
        ITunesSearchResult isr = (ITunesSearchResult) key;
        DcObject dco =  isr.getDco();
        
        try {
            waitBetweenRequest();
            
            setCoverImage(dco, isr.getCoverUrl());
            
            String url = "https://itunes.apple.com/lookup?entity=song&id=" + isr.getId();
            
            dco.setValue(DcObject._SYS_SERVICEURL, url);
            
            HttpConnection conn = new HttpConnection(new URL(url));
            String json = conn.getString(StandardCharsets.UTF_8);
            conn.close();
            
            Map<?, ?> r = gson.fromJson(json, Map.class);
            @SuppressWarnings("unchecked")
            ArrayList<LinkedTreeMap<?, ?>> tracks = (ArrayList<LinkedTreeMap<?, ?>>) r.get("results");
            
            MusicTrack track;
            Integer playLength;
            int disc;
            int trackNumber;
            String trackNumberFormatted;

            for (LinkedTreeMap<?, ?> src : tracks) {
                
                if (!src.get("wrapperType").equals("track"))
                    continue;
                
                track = new MusicTrack();
                
                track.setValue(MusicTrack._A_TITLE, src.get("trackName"));

                setArtists(track, src);
                
                if (!CoreUtilities.isEmpty(src.get("trackNumber"))) {
                    trackNumber = ((Number) src.get("trackNumber")).intValue();
                    
                    trackNumberFormatted = (trackNumber < 10 ? 
                            "0" + trackNumber : String.valueOf(trackNumber));
                    
                    if (    !CoreUtilities.isEmpty(src.get("discCount")) &&
                            ((Number) src.get("discCount")).intValue() > 1) {

                        disc = ((Number) src.get("discNumber")).intValue();
                        track.setValue(MusicTrack._F_TRACKNUMBER, disc + "-" + trackNumberFormatted); 
                    } else {
                        track.setValue(MusicTrack._F_TRACKNUMBER, trackNumberFormatted);
                    }
                }
                    
                if (!CoreUtilities.isEmpty(src.get("trackTimeMillis"))) {
                    playLength = Integer.valueOf(((Number) src.get("trackTimeMillis")).intValue() / 1000);
                    track.setValue(MusicTrack._J_PLAYLENGTH, playLength);
                }
                
                dco.addChild(track);
            }
        } catch (Exception e) {
            throw new OnlineServiceError(e);
        }
        
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
            String url;
            if (getMode().getFieldBinding() == MusicAlbum._A_TITLE)
                url = getServer().getUrl() + "/search?term=" + getQuery() + "&media=music&entity=album&limit=" + getMaximum();
            else
                url = getServer().getUrl() + "/lookup?upc=" + getQuery();
            
            HttpConnection conn = new HttpConnection(new URL(url));           
            String json = conn.getString(StandardCharsets.UTF_8);
            Map<?, ?> r = gson.fromJson(json, Map.class);

            ArrayList<LinkedTreeMap<?, ?>> albums = (ArrayList<LinkedTreeMap<?, ?>>) r.get("results");
            
            MusicAlbum album;
            ITunesSearchResult isr;
            String year;
            
            for (LinkedTreeMap<?, ?> src : albums) {
                
                album = new MusicAlbum();
                
                album.setValue(MusicAlbum._A_TITLE, src.get("collectionName"));
                album.setValue(MusicAlbum._N_WEBPAGE, src.get("collectionViewUrl"));
                
                year = (String) src.get("releaseDate");
                if (!CoreUtilities.isEmpty(year) && year.length() > 0)
                    album.setValue(MusicAlbum._C_YEAR, year.substring(0, 4));
                
                setArtists(album, src);
                
                album.createReference(MusicAlbum._F_COUNTRY, src.get("country"));
                album.createReference(MusicAlbum._G_GENRES, src.get("primaryGenreName"));
                album.createReference(MusicAlbum._I_STORAGEMEDIUM, src.get("Audio CD"));
                
                if (getMode().getFieldBinding() == MusicAlbum._P_EAN)
                    album.setValue(MusicAlbum._P_EAN, getQuery());
                
                String id = String.valueOf(((Number) src.get("collectionId")).longValue());
                album.addExternalReference(DcRepository.ExternalReferences._ITUNES, id);
                setServiceInfo(album);
                
                isr = new ITunesSearchResult(album, id);
                isr.setCoverUrl((String) src.get("artworkUrl100"));
                
                result.add(isr);
            }
            
        } catch (Exception e) {
            throw new OnlineServiceError(e);
        }
        
        return result;
    }
    
    private void setArtists(DcObject dco, LinkedTreeMap<?, ?> src) {

        String artistId = String.valueOf(((Number) src.get("artistId")).longValue());
        String artistName = (String) src.get("artistName");
        String viewUrl = (String) src.get("artistViewUrl");
        
        if (CoreUtilities.isEmpty(artistName) || CoreUtilities.isEmpty(artistId))
            return;
        
        DcAssociate artist;
        
        if (artistCache.contains(artistId)) {
            artist = artistCache.getArtist(artistId);
        } else {
            artist = new DcAssociate(DcModules._MUSICARTIST);
            
            artist.addExternalReference(ExternalReferences._DISCOGS, artistId);
            artist.setValue(DcAssociate._A_NAME, artistName);
            artist.setValue(DcAssociate._C_WEBPAGE, viewUrl);
            artist.setIDs();
            
            artistCache.addArtist(artist, artistId);
        }
        
        dco.createReference(
                dco.getModuleIdx() == DcModules._MUSIC_ALBUM ? 
                        MusicAlbum._F_ARTISTS : MusicTrack._G_ARTIST, artist);
    }     
    
    private void setCoverImage(DcObject musicalbum, String url) {
        try {
            if (url != null && url.length() > 0) {
                url = url.replaceAll("100x100bb", "500x500");
                byte[] b = HttpConnectionUtil.retrieveBytes(url);
                if (b != null && b.length > 50)
                    musicalbum.setValue(MusicAlbum._J_PICTUREFRONT, b);
            }
        } catch (Exception e) {
            logger.debug("Cannot download image from [" + url + "]", e);
        }
    }
}