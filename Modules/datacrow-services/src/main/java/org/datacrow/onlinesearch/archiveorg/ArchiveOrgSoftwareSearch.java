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
	    			new int[] {Software._P_SCREENSHOTONE, Software._Q_SCREENSHOTTWO, Software._R_SCREENSHOTTHREE},
	    			server,
	    			dir);
	    	
	    	setImages(
	    			dco, 
	    			item, 
	    			new String[] {"front","cover","back","cd","disc","floppy","box"},
	    			new String[] {"thumb"},
	    			new int[] {Software._M_PICTUREFRONT, Software._N_PICTUREBACK, Software._O_PICTURECD},
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