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
import org.datacrow.core.objects.helpers.Software;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.plugin.IServer;

public class ArchiveOrgSoftwareSearch extends ArchiveOrgSearch {
    
	private final String address = 
			"https://archive.org/advancedsearch.php?fl[]=identifier&fl[]=avg_rating&fl[]=collection&fl[]=date&fl[]=description&fl[]=format&fl[]=language&" +
			"fl[]=mediatype&fl[]=name&fl[]=subject&fl[]=title&fl[]=type&fl[]=volume&fl[]=week&fl[]=year&rows=50&output=json";
			
    public ArchiveOrgSoftwareSearch(
            IOnlineSearchClient listener, 
            IServer server, 
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, mode, query, additionalFilters);
    }
    
	@Override
	protected String getSearchAddress() {
		return address + "&q=title:%22" + getQuery() + "%22%20AND%20mediatype:%22software%22";
	}    

	@Override
	protected DcObject parseItem(Map<?, ?> item, ArchiveOrgSearchResult aosr) {
		DcObject dco =  aosr.getDco();
		
        setImages(dco, item, aosr.getId());
        dco.setValue(Software._I_WEBPAGE, "https://archive.org/details/" + aosr.getId());
        
        Map<?, ?> metadata = (Map<?, ?>) item.get("metadata");

        if (metadata != null)
	        setDevelopers(dco, metadata);
        
        return dco;
	}    
    
    private void setImages(DcObject dco, Map<?, ?> item, String id) {
    	
    	String server = (String) item.get("d1");
    	String dir = (String) item.get("dir");

    	if (server != null && dir != null) {
	    	setImages(
	    			dco, 
	    			item,
	    			new String[] {},
	    			new String[] {"front","cover","back","cd","disc","floppy","box","thumb"},
	    			server,
	    			dir);
	    	
	    	setImages(
	    			dco, 
	    			item, 
	    			new String[] {"front","cover","back","cd","disc","floppy","box"},
	    			new String[] {"thumb"},
	    			server,
	    			dir);
    	}
    }
    
    private void setDevelopers(DcObject dco, Map<?, ?> metadata) {
    	if (metadata.containsKey("creator")) {
    		String s = (String) metadata.get("creator");
    		String[] creators = s.indexOf(",") > 0 ? s.split(",") : s.split("/");
    		for (String creator : creators) {
    			dco.createReference(Software._F_DEVELOPER, creator.trim());
    		}
    	}
    }
}