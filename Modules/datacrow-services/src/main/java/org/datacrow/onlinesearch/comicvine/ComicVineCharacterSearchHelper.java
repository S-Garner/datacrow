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
import java.util.List;
import java.util.Map;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository.ExternalReferences;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.ComicCharacter;
import org.datacrow.core.server.Connector;
import org.datacrow.onlinesearch.util.JsonHelper;

import com.google.gson.Gson;

public class ComicVineCharacterSearchHelper {
    
    private static final Gson gson = new Gson();
    
	private final DcObject dco;
	private final String url;
	private final String userAgent;
	
    public ComicVineCharacterSearchHelper(DcObject dco, String userAgent, String url) {
        this.dco = dco;
        this.url = url;
        this.userAgent = userAgent;
    }
    
    public void search() throws Exception {
        HttpConnection conn = new HttpConnection(new URL(url), userAgent);
        String json = conn.getString(StandardCharsets.UTF_8);
        conn.close();
        
        Map<?, ?> result = gson.fromJson(json, Map.class);
        result = (Map<?, ?>) result.get("results");
        
        String id = String.valueOf(((Number) result.get("id")).intValue());
        dco.addExternalReference(ExternalReferences._COMICVINE, id);
        
        JsonHelper.setHtmlAsString(result, "description", dco, ComicCharacter._C_DESCRIPTION);
        JsonHelper.setString(result, "real_name", dco, ComicCharacter._B_REALNAME);
        JsonHelper.setString(result, "site_detail_url", dco, ComicCharacter._D_URL);
        
        setEnemies(result, dco);
        setFriends(result, dco);
        
        if (!dco.isNew()) {
            Connector connector = DcConfig.getInstance().getConnector();
            connector.saveItem(dco);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void setEnemies(Map<?, ?> map, DcObject dco) {
        if (map.containsKey("character_enemies")) {
            List<Map<?, ?>> enemies = (List<Map<?, ?>>) map.get("character_enemies");
            
            for (Map<?, ?> enemy : enemies)
                dco.createReference(ComicCharacter._E_ENEMIES, enemy.get("name"));
        }
    }
    
    @SuppressWarnings("unchecked")
    private void setFriends(Map<?, ?> map, DcObject dco) {
        if (map.containsKey("character_friends")) {
            List<Map<?, ?>> friends = (List<Map<?, ?>>) map.get("character_friends");
            
            for (Map<?, ?> friend : friends)
                dco.createReference(ComicCharacter._F_FRIENDS, friend.get("name"));
        }
    }
}
