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

package org.datacrow.onlinesearch.tmdb;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.datacrow.core.DcRepository.ExternalReferences;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcAssociate;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Movie;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.OnlineSearchUserError;
import org.datacrow.core.services.OnlineServiceError;
import org.datacrow.core.services.Region;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.SearchTaskUtilities;
import org.datacrow.core.services.Servers;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.onlinesearch.util.JsonHelper;

import com.google.gson.Gson;

/**
 * Class for handling searches over TheMovieDatabase's API, powered by https://github.com/Omertron/api-themoviedb
 *
 * @author Robert Jan Van Der Waals - Initial implementation for api 3.x
 * @author FlagCourier - Conversion to api-themoviedb 4.x and general cleanup.
 */
public class TmdbMovieSearch extends SearchTask {

	private static final Gson gson = new Gson();
	
    private final String apiKey;
    private final String lang;
    
    private String imageBaseUrl;
    
    public TmdbMovieSearch(
            IOnlineSearchClient listener, 
            IServer server, 
            Region region,
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, region, mode, query, additionalFilters);
        
        apiKey = Servers.getInstance().getApiKey("tmdb");
        lang = getRegion().getCode();
        
        try {
        	imageBaseUrl = new TmdbConfigurationInfo(apiKey, userAgent).getImageUrl();
        	imageBaseUrl += "original";
        	
        } catch (Exception e) {
        	listener.addError("Could not retrieve the TMDB condifguration information. Message: " + e.getMessage());
        	imageBaseUrl = "https://image.tmdb.org/t/p/original";
        }
    }

    @Override
    public String getWhiteSpaceSubst() {
        return " ";
    }
    
	@Override
	protected DcObject getItem(URL url) throws Exception {
		return null;
	}
    
    @Override
    protected DcObject getItem(Object key, boolean full) throws Exception {
    	TmdbSearchResult tsr = (TmdbSearchResult) key;
    	DcObject dco = tsr.getDco();
    	
    	waitBetweenRequest();
    	
    	String additionalData = "images,casts,list,crew";
    	String url = "http://api.themoviedb.org/3/movie/" + tsr.getMovieId() + 
    			"?api_key=" + apiKey + "&append_to_response=" + additionalData + "&language=en";

        HttpConnection conn = new HttpConnection(new URL(url), userAgent);
        String json = conn.getString(StandardCharsets.UTF_8);
        conn.close();
        
        Map<?, ?> src = gson.fromJson(json, Map.class);

        setPlaylength(src, dco);
        setRating(src, dco);
        setImages(src, dco);
        
        setCast(src, "cast", dco, Movie._I_ACTORS, null);
        setCast(src, "crew", dco, Movie._J_DIRECTOR, "Director");
        
        setReferences(src, "production_countries", dco, Movie._F_COUNTRY);
        setReferences(src, "spoken_languages", dco, Movie._1_AUDIOLANGUAGE);
        setReferences(src, "spoken_languages", dco, Movie._D_LANGUAGE);
        
        setReferences(src, "genres", dco, Movie._H_GENRES);
        JsonHelper.setString(src, "homepage", dco, Movie._G_WEBPAGE);
        
        return dco;
    }
    
    @Override
    protected Collection<Object> getItemKeys() throws OnlineSearchUserError, OnlineServiceError {
        Collection<Object> results = new ArrayList<>();
        
        try {
            waitBetweenRequest();

            String url =  "http://api.themoviedb.org/3/search/movie?query=" + 
            		getQuery() + "&api_key=" + apiKey + "&language=" + lang;
            
            HttpConnection conn = new HttpConnection(new URL(url), userAgent);
            String json = conn.getString(StandardCharsets.UTF_8);
            conn.close();
            
            Map<?, ?> raw = gson.fromJson(json, Map.class);
            
            @SuppressWarnings("unchecked")
			ArrayList<Map<?, ?>> movies = 
				(ArrayList<Map<?, ?>>) raw.get("results");
            
            int count = 0;
            TmdbSearchResult tsr;
            DcObject movie;
            String id;
            for (Map<?, ?> src : movies) {
            	movie = DcModules.get(DcModules._MOVIE).getItem();
            	
            	id = String.valueOf(((Number) src.get("id")).longValue());
            	
            	JsonHelper.setString(src, "title", movie, Movie._A_TITLE);
            	JsonHelper.setString(src, "original_title", movie, Movie._F_TITLE_LOCAL);
            	JsonHelper.setString(src, "overview", movie, Movie._B_DESCRIPTION);
            	
            	movie.setValue(Movie._G_WEBPAGE, "https://www.themoviedb.org/movie/" + id);
            	
            	JsonHelper.setYear(src, "release_date" , movie);
            	
            	setServiceInfo(movie);
            	
            	movie.addExternalReference(ExternalReferences._TMDB, String.valueOf(id));
            	
            	tsr = new TmdbSearchResult(movie);
            	tsr.setMovieId(id);
            	results.add(tsr);
            	
            	count++;
            	if (count == getMaximum()) break;
            }
        } catch (Exception e) {
            throw new OnlineServiceError(e);
        }
        
        return results;
    }
    
    @SuppressWarnings("unchecked")
	private void setReferences(Map<?, ?> map, String tag, DcObject dco, int fieldIdx) {
    	if (!map.containsKey(tag)) return;
    	
		ArrayList<Map<?, ?>> values = 
    			(ArrayList<Map<?, ?>>) map.get(tag);
    	
		String name;
    	for (Map<? ,?> value : values) {
    		name = value.containsKey("name") ? 
    				(String) value.get("name") :
    					value.containsKey("english_name") ?
    						(String) value.get("english_name") : null;
    		
    		if (name != null) 
    			dco.createReference(fieldIdx, name);
    	}	
    }
    
    private void setRating(Map<?, ?> map, DcObject dco) {
    	if (map.containsKey("vote_average") && !CoreUtilities.isEmpty(map.get("vote_average"))) {
    		int rating = ((Number) map.get("vote_average")).intValue();
    		dco.setValue(Movie._E_RATING, Integer.valueOf(rating));
    	}
    }
    
    private void setPlaylength(Map<?, ?> map, DcObject dco) {
    	if (map.containsKey("runtime") && !CoreUtilities.isEmpty(map.get("runtime"))) {
    		int runtime = ((Number) map.get("runtime")).intValue();
    		runtime = runtime * 60;
    		dco.setValue(Movie._L_PLAYLENGTH, Integer.valueOf(runtime));
    	}
    }
    
    private void setImages(Map<?, ?> map, DcObject dco) {
    	DcImageIcon image;
    	
    	if (map.containsKey("poster_path") && !CoreUtilities.isEmpty(map.get("poster_path"))) {
    		image = CoreUtilities.downloadAndStoreImage(imageBaseUrl + map.get("poster_path"));
    		if (image != null)
    		    dco.addNewPicture(new Picture(dco.getID(), image));
    	}

        if (map.containsKey("backdrop_path") && !CoreUtilities.isEmpty(map.get("backdrop_path"))) {
            image = CoreUtilities.downloadAndStoreImage(imageBaseUrl + map.get("backdrop_path"));
            if (image != null)
                dco.addNewPicture(new Picture(dco.getID(), image));
        }
    }  
    
    @SuppressWarnings("unchecked")
	private void setCast(Map<?, ?> src, String castType, DcObject dco, int fieldIdx, String role) {
    	
    	if (src.containsKey("casts")) {
    		ArrayList<Map<?, ?>> castmembers = 
    				(ArrayList<Map<?, ?>>) ((Map<?, ?>)  src.get("casts")).get(castType);
    		
    		if (castmembers == null)
    			return;
    		
    		DcImageIcon image; 
    		DcObject person;
    		for (Map<? ,?> castmember : castmembers) {
    			
    			if (role != null && !role.equalsIgnoreCase((String) castmember.get("job")))
    				continue;
    			
    			person = dco.createReference(fieldIdx, (String) castmember.get("name"));
    			if (person.isNew() &&
                    !CoreUtilities.isEmpty(castmember.get("profile_path"))) {

				    image = CoreUtilities.downloadAndStoreImage(imageBaseUrl + castmember.get("profile_path"));
					person.setValue(DcAssociate._D_PHOTO, image);
                }
    		}
    	}
    }

    @Override
    protected void preSearchCheck() {
        SearchTaskUtilities.checkForIsbn(this);
    }
}
