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

package org.datacrow.onlinesearch.moviemeter;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.datacrow.core.DcRepository.ExternalReferences;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.modules.DcModules;
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

public class MovieMeterSearch extends SearchTask {

	private static final Gson gson = new Gson();
    private final String apiKey;
    
    public MovieMeterSearch(
            IOnlineSearchClient listener, 
            IServer server, 
            Region region,
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, region, mode, query, additionalFilters);
        apiKey = Servers.getInstance().getApiKey("moviemeter");
    }

	@Override
	protected DcObject getItem(URL url) throws Exception {
		return null;
	}
    
    @Override
    protected DcObject getItem(Object key, boolean full) throws Exception {
    	MovieMeterSearchResult tsr = (MovieMeterSearchResult) key;
    	DcObject dco = tsr.getDco();
    	
    	waitBetweenRequest();

    	String url = "https://www.moviemeter.nl/api/film/" + tsr.getMovieId() + 
    			"?api_key=" + apiKey;

        HttpConnection conn = new HttpConnection(new URL(url), userAgent);
        String json = conn.getString(StandardCharsets.UTF_8);
        conn.close();
        
        Map<?, ?> src = gson.fromJson(json, Map.class);
        
        JsonHelper.setString(src, "plot", dco, Movie._B_DESCRIPTION);
        JsonHelper.setString(src, "url", dco, Movie._G_WEBPAGE);
        
        if (src.containsKey("imdb"))
        	dco.addExternalReference(ExternalReferences._IMDB, (String) src.get("imdb"));
        
        setPlaylength(src, dco);
        setImage(src, dco);
        
        setReferences(src, "countries", dco, Movie._F_COUNTRY);
        setReferences(src, "genres", dco, Movie._H_GENRES);
        setReferences(src, "actors", dco, Movie._I_ACTORS);
        setReferences(src, "directors", dco, Movie._J_DIRECTOR);
        
        return dco;
    }
    
    @Override
    protected Collection<Object> getItemKeys() throws OnlineSearchUserError, OnlineServiceError {
        Collection<Object> results = new ArrayList<>();
        
        try {
            waitBetweenRequest();

            String url = getServer().getUrl() + "?q=" + getQuery() + "&api_key=" + apiKey;
            
            HttpConnection conn = new HttpConnection(new URL(url), userAgent);
            String json = conn.getString(StandardCharsets.UTF_8);
            conn.close();
            
            @SuppressWarnings("unchecked")
			List<Map<?, ?>> raw = gson.fromJson(json, List.class);
            
            int count = 0;
            MovieMeterSearchResult mmsr;
            DcObject movie;
            String id;
            for (Map<?, ?> src : raw) {
            	movie = DcModules.get(DcModules._MOVIE).getItem();
            	
            	id = String.valueOf(((Number) src.get("id")).longValue());

            	JsonHelper.setString(src, "title", movie, Movie._A_TITLE);
            	JsonHelper.setYear(src, "year", movie);
            	setRating(src, movie);

            	setServiceInfo(movie);
            	movie.addExternalReference(ExternalReferences._MOVIEMETER, String.valueOf(id));
            	
            	mmsr = new MovieMeterSearchResult(movie);
            	mmsr.setMovieId(id);
            	results.add(mmsr);
            	
            	count++;
            	if (count == getMaximum()) break;
            }
        } catch (Exception e) {
            throw new OnlineServiceError(e);
        }
        
        return results;
    }
    
    private void setPlaylength(Map<?, ?> map, DcObject dco) {
    	if (map.containsKey("duration") && !CoreUtilities.isEmpty(map.get("duration"))) {
    		int runtime = ((Number) map.get("duration")).intValue();
    		runtime = runtime * 60;
    		dco.setValue(Movie._L_PLAYLENGTH, Integer.valueOf(runtime));
    	}
    }
    
    private void setRating(Map<?, ?> map, DcObject dco) {
    	if (map.containsKey("average")) {
    		Double rating = (Double) map.get("average");
    		dco.setValue(Movie._E_RATING, Integer.valueOf(rating.intValue() * 2));
    	}
    }
    
    private void setImage(Map<?, ?> map, DcObject dco) {
    	if (map.containsKey("posters")) {
    		String url = (String) ((Map<?, ?>) map.get("posters")).get("large");
    		
    		if (url != null) {
    			try {
    				DcImageIcon img = CoreUtilities.downloadAndStoreImage(url);
    				if (img != null)
    				    dco.addNewPicture(new Picture(dco.getID(), img));
    			} catch (Exception e) {
    				listener.addMessage("Could not retrieve image: " + e.getMessage());
    			}
    		}
    	}
    }    
    
    /**
     * The character used to substitute white spaces from the query (see {@link #getQuery()}).
     * Should be overridden by specific implementations.
     */
    public String getWhiteSpaceSubst() {
        return "%";
    }
    
	@SuppressWarnings("rawtypes")
	private void setReferences(Map<?, ?> map, String tag, DcObject dco, int fieldIdx) {
    	if (!map.containsKey(tag)) return;
    	
		ArrayList<?> values = (ArrayList<?>) map.get(tag);
		
		for (Object entry : values) {
			
			if (entry instanceof String) {
				dco.createReference(fieldIdx, entry);
			} else if (entry instanceof Map) {
				String name = (String) ((Map) entry).get("name");
				if (name != null)
					dco.createReference(fieldIdx, name);
			}
		}
    }

    @Override
    protected void preSearchCheck() {
        SearchTaskUtilities.checkForIsbn(this);
    }
}
