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

package org.datacrow.onlinesearch.archiveorg;

import java.util.Map;

import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Movie;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.plugin.IServer;

public class ArchiveOrgMovieSearch extends ArchiveOrgSearch {
    
	private final String address = 
			"https://archive.org/advancedsearch.php?fl[]=identifier&fl[]=avg_rating&fl[]=collection&fl[]=date&fl[]=description&fl[]=format&fl[]=language&" +
			"fl[]=mediatype&fl[]=name&fl[]=subject&fl[]=title&fl[]=type&fl[]=volume&fl[]=week&fl[]=year&rows=50&output=json";
			
    public ArchiveOrgMovieSearch(
            IOnlineSearchClient listener, 
            IServer server, 
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, mode, query, additionalFilters);
    }
    
	@Override
	protected String getSearchAddress() {
		return address + "&q=title:%22" + getQuery() + "%22%20AND%20mediatype:%22movies%22";
	}    
    
	@Override
	protected DcObject parseItem(Map<?, ?> item, ArchiveOrgSearchResult aosr) {
		DcObject dco =  aosr.getDco();
        setImages(dco, item, aosr.getId());
        
        dco.setValue(Movie._G_WEBPAGE, "https://archive.org/details/" + aosr.getId());
        
        Map<?, ?> metadata = (Map<?, ?>) item.get("metadata");

        if (metadata != null) {
	        setDirectors(dco, metadata);
	     
	        if (metadata.containsKey("color"))
	        	dco.createReference(Movie._13_COLOR, "Color");

	        if (metadata.containsKey("series"))
	        	dco.createReference(Movie._8_SERIES, metadata.get("series"));
	        
	        setRuntime(dco, metadata);
	        setDimension(dco, metadata);
        }
		
        return dco;
	}

	private void setDimension(DcObject dco, Map<?, ?> metadata) {
        if (metadata.containsKey("vimeo-width")) {
        	dco.setValue(Movie._P_WIDTH, metadata.get("vimeo-width"));
        }

        if (metadata.containsKey("vimeo-height")) {
        	dco.setValue(Movie._Q_HEIGHT, metadata.get("vimeo-height"));
        }
	}
	
	private void setRuntime(DcObject dco, Map<?, ?> metadata) {
        if (metadata.containsKey("runtime")) {
        	String time = (String) metadata.get("runtime");
        	
        	if (time != null && time.length() == 8) {
        		String[] parts = time.split(":");
        		
        		if (parts.length == 3) {
        			try {
		        		int runtime = 
		        				(Integer.valueOf(parts[0]) * 60 * 60) +
		        				(Integer.valueOf(parts[1]) * 60) + 
		        				Integer.valueOf(parts[2]);
		        		dco.setValue(Movie._L_PLAYLENGTH, runtime);
        			} catch (NumberFormatException nfe) {
        				listener.addError("Could not parse time from [" + time + "]");
        			}
        		}
        	}
        }
	}
	
    private void setImages(DcObject dco, Map<?, ?> item, String id) {
    	String server = (String) item.get("d1");
    	String dir = (String) item.get("dir");

    	if (server != null && dir != null) {
	    	setImages(
	    			dco, 
	    			item,
	    			new String[] {},
	    			new String[] {"thumb"},
	    			new int[] {Movie._X_PICTUREFRONT},
	    			server,
	    			dir);
    	}
    }
    
    private void setDirectors(DcObject dco, Map<?, ?> metadata) {
    	if (metadata.containsKey("creator")) {
    		String s = (String) metadata.get("creator");
    		String[] creators = 
    				s.indexOf(",") > 0 ? s.split(",") : 
    				s.indexOf("&") > 0 ? s.split("&") : 
    				s.split("/");
    		for (String creator : creators) {
    			dco.createReference(Movie._J_DIRECTOR, creator.trim());
    		}
    	}
    }    
}