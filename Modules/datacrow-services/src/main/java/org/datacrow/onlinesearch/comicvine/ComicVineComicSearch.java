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

import org.datacrow.core.DcRepository;
import org.datacrow.core.DcRepository.ExternalReferences;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Comic;
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
import org.datacrow.onlinesearch.util.JsonHelper;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

public class ComicVineComicSearch extends SearchTask {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ComicVineComicSearch.class.getName());

	private static final Gson gson = new Gson();
    private final String apiKey;
    
    private final ComicVineCharacterSearchHelper characterSearchHelper;
    
    public ComicVineComicSearch(
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
    	
        HttpConnection conn = new HttpConnection(new URL(address), userAgent);
        String json = conn.getString(StandardCharsets.UTF_8);
        conn.close();
        
        Map<?, ?> result = gson.fromJson(json, Map.class);
        result = (Map<?, ?>) result.get("results");
        
        setCharacters(result, dco);
        setImage(result, dco);
        setPersons(result, dco);
        setTeams(result, dco);
        
        String volumeUrl = setSeries(result, dco);
        
        if (volumeUrl != null) {
            waitBetweenRequest();
            setVolumeDetails(volumeUrl + "?api_key=" + apiKey + "&format=json", dco);
        }
        
        JsonHelper.setHtmlAsString(result, "description", dco, Comic._B_DESCRIPTION);
        JsonHelper.setYear(result, "cover_date", dco);
        JsonHelper.setString(result, "site_detail_url", dco, Comic._L_URL1);
        
        return dco;
    }
    
    private void setVolumeDetails(String url, DcObject dco) {
        try {
            HttpConnection conn = new HttpConnection(new URL(url), userAgent);
            String json = conn.getString(StandardCharsets.UTF_8);
            conn.close();
            
            Map<?, ?> result = gson.fromJson(json, Map.class);
            result = (Map<?, ?>) result.get("results");
            
            if (result.containsKey("publisher")) {
                Map<?, ?> publisher =  (Map<?, ?>) result.get("publisher");
                dco.createReference(Comic._Q_PUBLISHERS, publisher.get("name"));
            }
        } catch (Exception e) {
            logger.error("Could not retrieve volume details from " + url, e);
            listener.addError("Could not retrieve volume details: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void setTeams(Map<?, ?> map, DcObject dco) {
        if (map.containsKey("team_credits")) {
            List<Map<?, ?>> teams = (List<Map<?, ?>>) map.get("team_credits");
            for (Map<?, ?> team : teams)
                dco.createReference(Comic._P_TEAMS, team.get("name"));
        }
    }
    
    private String setSeries(Map<?, ?> map, DcObject dco) {
        if (map.containsKey("volume")) {
            Map<?, ?> volume = (Map<?, ?>) map.get("volume");
            dco.createReference(Comic._H_SERIES, volume.get("name"));    
        
            return (String) volume.get("api_detail_url");
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private void setPersons(Map<?, ?> map, DcObject dco) {
        if (map.containsKey("person_credits")) {
            List<Map<?, ?>> persons = (List<Map<?, ?>>) map.get("person_credits");
            
            String role;
            String name;
            
            for (Map<?, ?> person : persons) {
                role = (String) person.get("role");
                name = (String) person.get("name");
                
                if (role.equals("writer")) {
                    dco.createReference(Comic._S_AUTHOR, name);
                } else {
                    dco.createReference(Comic._R_ARTISTS, name);
                }
            }
        }
    }
    
    private void setImage(Map<?, ?> map, DcObject dco) {
        if (map.containsKey("image")) {
            Map<?, ?> images = (Map<?, ?>) map.get("image");
            
            if (images.containsKey("original_url")) {
                DcImageIcon img = CoreUtilities.downloadAndStoreImage((String) images.get("original_url"));
                if (img != null)
                    dco.setValue(Comic._V_PICTURE1, img);
            }   
        }
    }
    
    @SuppressWarnings("unchecked")
    private void setCharacters(Map<?, ?> map, DcObject dco) {
        if (map.containsKey("character_credits")) {
            List<Map<?, ?>> characters = (List<Map<?, ?>>) map.get("character_credits");
            
            for (Map<?, ?> character : characters) {
                DcObject ref = dco.createReference(Comic._O_CHARACTERS, character.get("name"));
                
                if (!ref.isNew())
                    ref.initializeReferences(ComicCharacter._SYS_EXTERNAL_REFERENCES, true);
             
                // only query characters in case the character is new, or, the character has not been searched for before.
                if (   !isCancelled() &&
                        character.containsKey("api_detail_url") && 
                        ref.isNew() || dco.getExternalReference(DcRepository.ExternalReferences._COMICVINE) == null) {
                    
                    waitBetweenRequest();
                    setCharacterDetails((String) character.get("api_detail_url") + "?api_key=" + apiKey + "&format=json", ref);
                }
            }
        }
    }
    
    private void setCharacterDetails(String url, DcObject dco) {
        try {
            characterSearchHelper.search(dco, userAgent, url, true);
        } catch (Exception e) {
            logger.error("Could not retrieve character details from " + url, e);
            listener.addError("Could not retrieve character details: " + e.getMessage());
        }
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
