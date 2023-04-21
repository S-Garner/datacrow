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

package org.datacrow.onlinesearch.comicvine;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.datacrow.core.DcRepository.ExternalReferences;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Comic;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.OnlineSearchUserError;
import org.datacrow.core.services.OnlineServiceError;
import org.datacrow.core.services.Region;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.Servers;
import org.datacrow.core.services.plugin.IServer;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

public class ComicVineSearch extends SearchTask {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ComicVineSearch.class.getName());

	private static final Gson gson = new Gson();
    private final String apiKey;
    
    public ComicVineSearch(
            IOnlineSearchClient listener, 
            IServer server, 
            Region region,
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, region, mode, query, additionalFilters);
        apiKey = Servers.getInstance().getApiKey("comicvine");
    }
    
	@Override
	protected DcObject getItem(URL url) throws Exception {
		return null;
	}
    
    @Override
    protected DcObject getItem(Object key, boolean full) throws Exception {
    	ComicVineSearchResult cvsr = (ComicVineSearchResult) key;
    	DcObject dco = cvsr.getDco();
    	
    	waitBetweenRequest();
    	
    	String address = (String) cvsr.getComicData().get("api_detail_url");
    	address += "?api_key=" + apiKey + "&format=json";
    	
        HttpConnection conn = new HttpConnection(new URL(address), userAgent);
        String json = conn.getString(StandardCharsets.UTF_8);
        conn.close();
        
        Map<?, ?> result = gson.fromJson(json, Map.class);
    	
        return dco;
    }
    
    private void setCharacters(Map<?, ?> map, DcObject dco) {
        if (map.containsKey("character_credits")) {
            List<Map<?, ?>> characters = (List<Map<?, ?>>) map.get("character_credits");
            
//            for (Map<?, ?> character : characters) {
//                dco.createReference(Comic._O_CHARACTERS, character.get("name"));
//                // TODO: add URL field
//            }
//            
//            String name = (String) designer.get("name");
//            
//            if (!CoreUtilities.isEmpty(name)) {
//                dco.createReference(BoardGame._H_DESIGNERS, name);
//            }
        }
    }
    
    @Override
    protected Collection<Object> getItemKeys() throws OnlineSearchUserError, OnlineServiceError {
        Collection<Object> results = new ArrayList<>();
        
        try {
            waitBetweenRequest();

            String url = getRegion().getUrl() + "?api_key=" + apiKey + "&filter=name:" + getQuery() + "&format=json"; 
            
            HttpConnection conn = new HttpConnection(new URL(url), userAgent);
            String json = conn.getString(StandardCharsets.UTF_8);
            conn.close();
            
            Map<?, ?> result = gson.fromJson(json, Map.class);
            @SuppressWarnings("unchecked")
            ArrayList<LinkedTreeMap<?, ?>> raw = (ArrayList<LinkedTreeMap<?, ?>>) result.get("results");
            
            int count = 0;
            ComicVineSearchResult cvsr;
            DcObject comic;
            String id;
            for (Map<?, ?> src : raw) {
                comic = DcModules.get(DcModules._COMIC).getItem();
                
                comic.setValue(Comic._A_TITLE, src.get("name"));
                comic.setValue(Comic._F_ISSUE_NUMBER, src.get("issue_number"));
                
                id = String.valueOf(((Number) src.get("id")).intValue());

                setServiceInfo(comic);
                comic.addExternalReference(ExternalReferences._COMICVINE, id);
                
                cvsr = new ComicVineSearchResult(comic);
                cvsr.setComicData(src);
                results.add(cvsr);
                
                count++;
                if (count == getMaximum()) break;
            }
        } catch (Exception e) {
            throw new OnlineServiceError(e);
        }
        
        return results;
    }
    
    /**
     * The character used to substitute white spaces from the query (see {@link #getQuery()}).
     * Should be overridden by specific implementations.
     */
    public String getWhiteSpaceSubst() {
        return "%";
    }
}
