/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.net                             *
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
import java.util.Map;

import org.datacrow.core.DcRepository.ExternalReferences;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.OnlineSearchUserError;
import org.datacrow.core.services.OnlineServiceError;
import org.datacrow.core.services.Region;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.SearchTaskUtilities;
import org.datacrow.core.services.Servers;
import org.datacrow.core.services.plugin.IServer;

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
    	
//    	String additionalData = "images,casts,list,crew";
//    	String url = "http://api.themoviedb.org/3/movie/" + tsr.getMovieId() + 
//    			"?api_key=" + apiKey + "&append_to_response=" + additionalData + "&language=en";
//
//        HttpConnection conn = new HttpConnection(new URL(url), userAgent);
//        String json = conn.getString(StandardCharsets.UTF_8);
//        conn.close();
//        
//        Map<?, ?> src = gson.fromJson(json, Map.class);
//
//        setPlaylength(src, dco);
//        setRating(src, dco);
//        setImages(src, dco);
//        
//        setCast(src, "cast", dco, Movie._I_ACTORS, null);
//        setCast(src, "crew", dco, Movie._J_DIRECTOR, "Director");
//        
//        setReferences(src, "production_countries", dco, Movie._F_COUNTRY);
//        setReferences(src, "spoken_languages", dco, Movie._1_AUDIOLANGUAGE);
//        setReferences(src, "spoken_languages", dco, Movie._D_LANGUAGE);
//        
//        setReferences(src, "genres", dco, Movie._H_GENRES);
//        setString(src, "homepage", dco, Movie._G_WEBPAGE);
        
        return dco;
    }
    
    @Override
    protected Collection<Object> getItemKeys() throws OnlineSearchUserError, OnlineServiceError {
        Collection<Object> results = new ArrayList<>();
        
        try {
            waitBetweenRequest();

            String url = getServer().getUrl() + "q=" + getQuery() + "&api_key=" + apiKey;
            
            HttpConnection conn = new HttpConnection(new URL(url), userAgent);
            String json = conn.getString(StandardCharsets.UTF_8);
            conn.close();
            
            Map<?, ?> raw = gson.fromJson(json, Map.class);
            
            @SuppressWarnings("unchecked")
			ArrayList<Map<?, ?>> movies = 
				(ArrayList<Map<?, ?>>) raw.get("results");
            
            int count = 0;
            MovieMeterSearchResult mmsr;
            DcObject movie;
            String id;
            for (Map<?, ?> src : movies) {
            	movie = DcModules.get(DcModules._MOVIE).getItem();
            	
            	id = String.valueOf(((Number) src.get("id")).longValue());
//            	
//            	setString(src, "title", movie, Movie._A_TITLE);
//            	setString(src, "original_title", movie, Movie._F_TITLE_LOCAL);
//            	setString(src, "overview", movie, Movie._B_DESCRIPTION);
//            	
//            	movie.setValue(Movie._G_WEBPAGE, "https://www.themoviedb.org/movie/" + id);
//            	
//            	setYear(src, "release_date" , movie);

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
    
//    private void setString(Map<?, ?> map, String tag, DcObject dco, int fieldIdx) {
//    	
//    	Object o = map.get(tag);
//    	
//    	if (!CoreUtilities.isEmpty(o)) {
//    		String s = o instanceof String ? (String) o : o.toString();
//    		dco.setValue(fieldIdx, s);
//    	}
//    }
//    
//    private void setYear(Map<?, ?> map, String tag, DcObject dco) {
//    	if (map.containsKey(tag)) {
//    		String year =  (String) map.get(tag);
//    		year = year.length() == 10 ? year.substring(0, 4) : null;
//    		
//    		if (year != null)
//    			dco.setValue(Movie._C_YEAR, year);
//    	}
//    }
//    
//    @SuppressWarnings("unchecked")
//	private void setReferences(Map<?, ?> map, String tag, DcObject dco, int fieldIdx) {
//    	if (!map.containsKey(tag)) return;
//    	
//		ArrayList<Map<?, ?>> values = 
//    			(ArrayList<Map<?, ?>>) map.get(tag);
//    	
//		String name;
//    	for (Map<? ,?> value : values) {
//    		name = value.containsKey("name") ? 
//    				(String) value.get("name") :
//    					value.containsKey("english_name") ?
//    						(String) value.get("english_name") : null;
//    		
//    		if (name != null) 
//    			dco.createReference(fieldIdx, name);
//    	}	
//    }
//    
//    private void setRating(Map<?, ?> map, DcObject dco) {
//    	if (map.containsKey("vote_average") && !CoreUtilities.isEmpty(map.get("vote_average"))) {
//    		int rating = ((Number) map.get("vote_average")).intValue();
//    		dco.setValue(Movie._E_RATING, Integer.valueOf(rating));
//    	}
//    }
//    
//    private void setPlaylength(Map<?, ?> map, DcObject dco) {
//    	if (map.containsKey("runtime") && !CoreUtilities.isEmpty(map.get("runtime"))) {
//    		int runtime = ((Number) map.get("runtime")).intValue();
//    		runtime = runtime * 60;
//    		dco.setValue(Movie._L_PLAYLENGTH, Integer.valueOf(runtime));
//    	}
//    }
//    
//    private void setImages(Map<?, ?> map, DcObject dco) {
//    	byte[] image;
//    	
//    	if (map.containsKey("backdrop_path") && !CoreUtilities.isEmpty(map.get("backdrop_path"))) {
//    		image = getImageBytes(imageBaseUrl + map.get("backdrop_path"));
//    		dco.setValue(Movie._Y_PICTUREBACK, image);
//    	}
//    	
//    	if (map.containsKey("poster_path") && !CoreUtilities.isEmpty(map.get("poster_path"))) {
//    		image = getImageBytes(imageBaseUrl + map.get("poster_path"));
//    		dco.setValue(Movie._X_PICTUREFRONT, image);
//    	}
//    }  
//    
//    @SuppressWarnings("unchecked")
//	private void setCast(Map<?, ?> src, String castType, DcObject dco, int fieldIdx, String role) {
//    	
//    	if (src.containsKey("casts")) {
//    		ArrayList<Map<?, ?>> castmembers = 
//    				(ArrayList<Map<?, ?>>) ((Map<?, ?>)  src.get("casts")).get(castType);
//    		
//    		if (castmembers == null)
//    			return;
//    		
//    		byte[] image; 
//    		DcObject person;
//    		for (Map<? ,?> castmember : castmembers) {
//    			
//    			if (role != null && !role.equalsIgnoreCase((String) castmember.get("job")))
//    				continue;
//    			
//    			person = dco.createReference(fieldIdx, (String) castmember.get("name"));
//    			if (person.isNew() &&
//                    DcModules.get(DcModules._MOVIE).getSettings().getBoolean(DcRepository.ModuleSettings.stOnlineSearchSubItems) &&
//                    !CoreUtilities.isEmpty(castmember.get("profile_path"))) {
//
//    				try {
//    					image = HttpConnectionUtil.retrieveBytes(imageBaseUrl + castmember.get("profile_path"));
//    					person.setValue(DcAssociate._D_PHOTO, new DcImageIcon(image));
//    				} catch (HttpConnectionException hce) {
//    					listener.addMessage("Could not retrieve photo for " + person + ". Message: " + hce.getMessage());
//    				}
//                }
//    		}
//    	}
//    }

    @Override
    protected void preSearchCheck() {
        SearchTaskUtilities.checkForIsbn(this);
    }
}
