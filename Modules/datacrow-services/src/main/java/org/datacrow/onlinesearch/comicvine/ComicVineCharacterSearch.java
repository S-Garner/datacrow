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
import java.util.Map;

import org.datacrow.core.DcRepository;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.ComicCharacter;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.OnlineSearchUserError;
import org.datacrow.core.services.OnlineServiceError;
import org.datacrow.core.services.Region;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.CoreUtilities;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

public class ComicVineCharacterSearch extends SearchTask {
    
	private static final Gson gson = new Gson();
    private final String apiKey;
    
    private final ComicVineCharacterSearchHelper characterSearchHelper;
    
    public ComicVineCharacterSearch(
            IOnlineSearchClient listener, 
            IServer server, 
            Region region,
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, region, mode, query, additionalFilters);
        apiKey = DcSettings.getString(DcRepository.Settings.stComicVineApiKey).trim();
        characterSearchHelper = new ComicVineCharacterSearchHelper(listener);
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
    	
    	characterSearchHelper.search(dco, address, address, false);
        
        return dco;
    }
    
    private void checkApiKey() throws OnlineSearchUserError {
        if (CoreUtilities.isEmpty(apiKey)) {
            String msg = DcResources.getText("msgComicVineNoApiKeyDefined");
            msg = "<html>" + msg + "<br><u><a href=\"https://comicvine.gamespot.com/api\">https://comicvine.gamespot.com/api</a></u></html>";
            
            throw new OnlineSearchUserError(msg);
        }
    }
    
    @Override
    protected Collection<Object> getItemKeys() throws OnlineSearchUserError, OnlineServiceError {
        
        checkApiKey();
        
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
            DcObject comicCharacter;
            for (Map<?, ?> src : raw) {
                comicCharacter = DcModules.get(DcModules._COMICCHARACTER).getItem();
                
                comicCharacter.setValue(ComicCharacter._A_NAME, src.get("name"));

                setServiceInfo(comicCharacter);
                
                cvsr = new ComicVineSearchResult(comicCharacter);
                cvsr.setData(src);
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
