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

package org.datacrow.onlinesearch.discogs;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import org.datacrow.core.objects.helpers.Software;
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

    private final String address = "https://api.discogs.com/database";
    private final String consumerKey;
    private final String consumerSecret;

    private final Gson gson;
    
    private final DiscogsArtistCache artistCache = new DiscogsArtistCache();
    private final DiscogsArtistCache composerCache = new DiscogsArtistCache();
    
    public DiscogsSearch(
            IOnlineSearchClient listener, 
            IServer server, 
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, null, mode, query, additionalFilters);
        
        setMaximum(100);
        
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
        setComposers(null, dco, src);
        setRating(dco, src);
        addTracks(dco, src);
        
        setServiceInfo(dco);
        dco.setValue(Software._SYS_SERVICEURL, dsr.getDetailsUrl());
        
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
            
        	if (trackData.get("type_") == null) continue;
        	
        	if (trackData.get("type_").equals("track")) {
                mt = new MusicTrack();
                
                mt.setValue(MusicTrack._F_TRACKNUMBER, trackData.get("position"));
                mt.setValue(MusicTrack._A_TITLE, trackData.get("title"));
                
                setPlaylength(mt, trackData);
                setArtists(mt, trackData);
                setComposers(mt, dco, trackData);
                
                if (!mt.isFilled(MusicTrack._G_ARTIST))
                    setArtists(mt, albumData);
                
                dco.addChild(mt);        		
        		
        	} else if (trackData.get("type_").equals("index")) {
        		@SuppressWarnings("unchecked")
				Collection<LinkedTreeMap<?, ?>> subTracksData = 
        				(Collection<LinkedTreeMap<?, ?>>) trackData.get("sub_tracks");
        		
        		for (LinkedTreeMap<?, ?> subTrackData : subTracksData) {
        			mt = new MusicTrack();
                    
                    mt.setValue(MusicTrack._F_TRACKNUMBER, subTrackData.get("position"));
                    mt.setValue(MusicTrack._A_TITLE, subTrackData.get("title"));
                    
                    setPlaylength(mt, subTrackData);
                    setArtists(mt, subTrackData);
                    setComposers(mt, dco, trackData);
                    
                    if (!mt.isFilled(MusicTrack._G_ARTIST))
                        setArtists(mt, albumData);
                    
                    dco.addChild(mt);        			
        		}
        	}
        } 
    }  
    
    @SuppressWarnings("unchecked")
    private void setComposers(DcObject track, DcObject album, Map<?, ?> src) {
    	Collection<LinkedTreeMap<?, ?>> artistsData = (Collection<LinkedTreeMap<?, ?>>) src.get("extraartists");

        if (artistsData == null)
            return;
        
        DcAssociate composer;
        String artistId;
        
        for (LinkedTreeMap<?, ?> artistData : artistsData) {
            artistId = String.valueOf(((Double) artistData.get("id")).intValue());
            if ("Composed By".equals(artistData.get("role"))) { 
            
	            if (composerCache.contains(artistId)) {
	            	composer = composerCache.getArtist(artistId);
	            } else {
	            	composer = new DcAssociate(DcModules._COMPOSER);
	                
	            	composer.addExternalReference(ExternalReferences._DISCOGS, artistId);
	            	composer.setValue(DcAssociate._A_NAME, artistData.get("name"));
	            	composer.setValue(DcAssociate._C_WEBPAGE, "https://www.discogs.com/artist/" + artistId);
	            	composer.setIDs();
	                
	            	composerCache.addArtist(composer, artistId);
	            }
	            
	            if (album != null)
	            	album.createReference(MusicAlbum._R_COMPOSER, composer);
	            
	            if (track != null)
	            	track.createReference(MusicTrack._Q_COMPOSER, composer);
            }
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
