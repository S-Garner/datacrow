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
