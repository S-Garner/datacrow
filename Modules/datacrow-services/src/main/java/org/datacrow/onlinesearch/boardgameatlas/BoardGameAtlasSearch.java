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

package org.datacrow.onlinesearch.boardgameatlas;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.datacrow.core.DcRepository.ExternalReferences;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.http.HttpConnectionUtil;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.BoardGame;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.OnlineSearchUserError;
import org.datacrow.core.services.OnlineServiceError;
import org.datacrow.core.services.Region;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.Servers;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.onlinesearch.util.JsonHelper;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

public class BoardGameAtlasSearch extends SearchTask {
    
    private static Logger logger = DcLogManager.getLogger(BoardGameAtlasSearch.class.getName());

	private static final Gson gson = new Gson();
    private final String apiKey;
    
    private static final Map<Object, String> mechanics = new HashMap<>();
    private static final Map<Object, String> categories = new HashMap<>();
    
    public BoardGameAtlasSearch(
            IOnlineSearchClient listener, 
            IServer server, 
            Region region,
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, region, mode, query, additionalFilters);
        apiKey = Servers.getInstance().getApiKey("boardgameatlas");
        
        if (mechanics.size() == 0)
            retrieveMechanicsInformation();
        
        if (categories.size() == 0)
            retrieveCategoryInformation();
    }
    
	@Override
	protected DcObject getItem(URL url) throws Exception {
		return null;
	}
    
    @Override
    protected DcObject getItem(Object key, boolean full) throws Exception {
    	BoardGameAtlasSearchResult bgasr = (BoardGameAtlasSearchResult) key;
    	DcObject dco = bgasr.getDco();
    	
    	waitBetweenRequest();
    	
    	Map<?, ?> src = bgasr.getBoardGameData();
    	
        JsonHelper.setString(src, "name", dco, BoardGame._A_TITLE);
        JsonHelper.setYear(src, "year_published", dco);
        JsonHelper.setString(src, "url", dco, BoardGame._M_WEBPAGE);
        JsonHelper.setString(src, "rules_url", dco, BoardGame._N_RULES_URL);
        JsonHelper.setLong(src, "min_age", dco, BoardGame._L_MINIMUM_AGE);

        setRating(src, dco);
        setDescription(src, dco);
        setNumberOfPlayers(src, dco);
        setPlaytime(src, dco);
        setImage(src, dco);
        //setMechanics(src, dco);
        
        return dco;
    }
    
    @Override
    protected Collection<Object> getItemKeys() throws OnlineSearchUserError, OnlineServiceError {
        Collection<Object> results = new ArrayList<>();
        
        try {
            waitBetweenRequest();

            String url = "https://api.boardgameatlas.com/api/search?name=" + getQuery() + "&client_id=" + apiKey;
            
            HttpConnection conn = new HttpConnection(new URL(url), userAgent);
            String json = conn.getString(StandardCharsets.UTF_8);
            conn.close();
            
            Map<?, ?> result = gson.fromJson(json, Map.class);
            @SuppressWarnings("unchecked")
            ArrayList<LinkedTreeMap<?, ?>> raw = (ArrayList<LinkedTreeMap<?, ?>>) result.get("games");
            
			int count = 0;
            BoardGameAtlasSearchResult bgasr;
            DcObject boardgame;
            String id;
            for (Map<?, ?> src : raw) {
                boardgame = DcModules.get(DcModules._BOARDGAME).getItem();
            	
            	id = (String) src.get("id");

            	setServiceInfo(boardgame);
            	boardgame.addExternalReference(ExternalReferences._BOARDGAMEATLAS, id);
            	
            	bgasr = new BoardGameAtlasSearchResult(boardgame);
            	bgasr.setBoardGameData(src);
            	results.add(bgasr);
            	
            	count++;
            	if (count == getMaximum()) break;
            }
        } catch (Exception e) {
            throw new OnlineServiceError(e);
        }
        
        return results;
    }
    
    private void setRating(Map<?, ?> map, DcObject dco) {
    	if (map.containsKey("average_user_rating")) {
    	    Number rating = (Number) map.get("average_user_rating");
    		dco.setValue(BoardGame._E_RATING, Integer.valueOf(rating.intValue() * 2));
    	}
    }
    
    private void setImage(Map<?, ?> map, DcObject dco) {
    	if (map.containsKey("image_url")) {
    		String url = (String) map.get("image_url");
    		if (url != null) {
    			try {
    				byte[] img = HttpConnectionUtil.retrieveBytes(url);
    				dco.setValue(BoardGame._Q_PICTURE1, img);
    			} catch (Exception e) {
    				listener.addMessage("Could not retrieve image: " + e.getMessage());
    			}
    		}
    	}
    }
    
    private void setPlaytime(Map<?, ?> src, DcObject dco) {
        Number min = (Number) src.get("min_playtime");
        Number max = (Number) src.get("max_playtime");
        
        if (min != null || max != null) {
            Long l = min == null ? 
                           Long.valueOf(max.longValue()) :
                               Long.valueOf(min.longValue());
            dco.setValue(BoardGame._K_PLAYTIME, l);
        }
    }      
    
    private void setNumberOfPlayers(Map<?, ?> src, DcObject dco) {
        Number minPlayers = (Number) src.get("min_players");
        Number maxPlayers = (Number) src.get("max_players");
        
        if (minPlayers != null || maxPlayers != null) {
            String s = minPlayers == null ? 
                           String.valueOf(maxPlayers.longValue()) :
                               maxPlayers == null ?
                                   String.valueOf(minPlayers.longValue()) :
                                       String.valueOf(minPlayers.longValue()) + "-" + String.valueOf(maxPlayers.longValue());
            dco.setValue(BoardGame._J_NR_OF_PLAYERS, s);
        }
    }    
    
    private void setDescription(Map<?, ?> src, DcObject dco) {
        JsonHelper.setString(src, "description", dco, BoardGame._B_DESCRIPTION);        
        if (dco.isFilled(BoardGame._B_DESCRIPTION)) {
            String s = (String) dco.getValue(BoardGame._B_DESCRIPTION);
            s = s.replaceAll("<p ", "");
            s = s.replaceAll("</p ", "");
            s = s.replaceAll("<p>", "");
            s = s.replaceAll("</p>", "");
            s = s.replaceAll("<br / ", "\r\n");
            s = s.replaceAll("</strong ", "");
            s = s.replaceAll("<strong ", "");
            s = s.replaceAll("</em ", "");
            s = s.replaceAll("<em ", "");
            
            dco.setValue(BoardGame._B_DESCRIPTION, s);
        }   
    }    
    
    /**
     * The character used to substitute white spaces from the query (see {@link #getQuery()}).
     * Should be overridden by specific implementations.
     */
    public String getWhiteSpaceSubst() {
        return "%";
    }
    
    private void retrieveMechanicsInformation() {
        try {
            String url = "https://api.boardgameatlas.com/api/game/mechanics?client_id=" + apiKey;
            
            HttpConnection conn = new HttpConnection(new URL(url), userAgent);
            String json = conn.getString(StandardCharsets.UTF_8);
            conn.close();
            
            Map<?, ?> result = gson.fromJson(json, Map.class);
            @SuppressWarnings("unchecked")
            ArrayList<LinkedTreeMap<?, ?>> raw = (ArrayList<LinkedTreeMap<?, ?>>) result.get("mechanics");

            for (Map<?, ?> src : raw) {    
                mechanics.put(src.get("id"), (String) src.get("name"));
            }
        } catch (Exception e) {
            logger.error("Could not obtain mechanic information from BoardGamesAtlas.com", e);
        }
    }
    
    private void retrieveCategoryInformation() {
        try {
            String url = "https://api.boardgameatlas.com/api/game/categories?client_id=" + apiKey;
            
            HttpConnection conn = new HttpConnection(new URL(url), userAgent);
            String json = conn.getString(StandardCharsets.UTF_8);
            conn.close();
            
            Map<?, ?> result = gson.fromJson(json, Map.class);
            @SuppressWarnings("unchecked")
            ArrayList<LinkedTreeMap<?, ?>> raw = (ArrayList<LinkedTreeMap<?, ?>>) result.get("categories");

            for (Map<?, ?> src : raw) {    
                categories.put(src.get("id"), (String) src.get("name"));
            }
        } catch (Exception e) {
            logger.error("Could not obtain mechanic information from BoardGamesAtlas.com", e);
        }
    }     
}
