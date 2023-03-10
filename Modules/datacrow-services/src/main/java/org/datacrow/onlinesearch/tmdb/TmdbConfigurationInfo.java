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
import java.util.Map;

import org.datacrow.core.http.HttpConnection;

import com.google.gson.Gson;

public class TmdbConfigurationInfo {
	
	private final Gson gson = new Gson();
	
	private final String imageUrl;
	
	public TmdbConfigurationInfo(String apiKey, String userAgent) throws Exception {
    	String url = 
    			"https://api.themoviedb.org/3/configuration?api_key=" + apiKey;
    	
        HttpConnection conn = new HttpConnection(new URL(url), userAgent);
        String json = conn.getString(StandardCharsets.UTF_8);
        conn.close();
        
        Map<?, ?> src = gson.fromJson(json, Map.class);
        
        Map<?, ?> images = (Map<?, ?>) src.get("images");
        imageUrl = (String) images.get("secure_base_url");
	}

	public String getImageUrl() {
		return imageUrl;
	}
}
