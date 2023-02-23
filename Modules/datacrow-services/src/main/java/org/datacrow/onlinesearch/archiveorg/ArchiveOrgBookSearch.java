package org.datacrow.onlinesearch.archiveorg;

import java.util.Collection;
import java.util.Map;

import org.datacrow.core.DcRepository;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Book;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.StringUtils;
import org.datacrow.core.utilities.isbn.ISBN;
import org.datacrow.core.utilities.isbn.InvalidBarCodeException;

public class ArchiveOrgBookSearch extends ArchiveOrgSearch {
    
	private final String address = 
			"https://archive.org/advancedsearch.php?fl[]=identifier&fl[]=avg_rating&fl[]=collection&fl[]=date&fl[]=description&fl[]=format&fl[]=language&" +
			"fl[]=mediatype&fl[]=name&fl[]=subject&fl[]=title&fl[]=type&fl[]=volume&fl[]=week&fl[]=year&rows=50&output=json";
			
    public ArchiveOrgBookSearch(
            IOnlineSearchClient listener, 
            IServer server, 
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, mode, query, additionalFilters);
    }
    
	@Override
	protected String getSearchAddress() {
		return address + "&q=title:%22" + getQuery() + "%22%20AND%20mediatype:%22texts%22";
	}    
    
	@Override
	protected DcObject parseItem(Map<?, ?> item, ArchiveOrgSearchResult aosr) {
		DcObject dco =  aosr.getDco();
        setImages(dco, item, aosr.getId());
        
        dco.setValue(Book._H_WEBPAGE, "https://archive.org/details/" + aosr.getId());
        
        Map<?, ?> metadata = (Map<?, ?>) item.get("metadata");

        if (metadata != null) {
        	setPublisher(dco, metadata);
        	setAuthor(dco, metadata);
        	setIsbn(dco, metadata);
        	setPages(dco, metadata);
        	
        	if (metadata.containsKey("openlibrary_edition")) {
        		String key = (String) metadata.get("openlibrary_edition");
        		key = key.startsWith("/books/") ? key : "/books/" + key;
        		
        		dco.addExternalReference(
        				DcRepository.ExternalReferences._OPENLIBRARY, key);
        	}
        }
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
	    			new String[] {},
	    			new int[] {Book._K_PICTUREFRONT},
	    			server,
	    			dir);
    	}
    }
    
    private void setPages(DcObject dco, Map<?, ?> metadata) {
    	String description = (String) dco.getValue(Book._B_DESCRIPTION);
    	
    	if (!CoreUtilities.isEmpty(description)) {
    		String pages = 
    				description.indexOf("p.,") < 10 && 
    				description.indexOf("p.,") > -1 ? 
    						description.substring(0, description.indexOf("p.,")) : null;
    		
    		pages = pages == null && 
    				description.indexOf("pages") < 10 && 
    				description.indexOf("pages") > -1 ? 
    						description.substring(0, description.indexOf("pages")) : pages;
    		
    		
    		pages = pages != null ? StringUtils.getContainedNumber(pages) : null;
    		if (pages != null) {
    			dco.setValue(Book._T_NROFPAGES, pages);
    		}
    	}
    }    
    
    
    private void setIsbn(DcObject dco, Map<?, ?> metadata) {
    	Object value = metadata.get("isbn");
    	
    	try {
	    	if (value != null) {
	    		if (value instanceof Collection) {
	    			for (Object o : (Collection<?>) value) {
	    				ISBN isbn = new ISBN((String) o);
	    				dco.setValue(Book._N_ISBN13, isbn.getIsbn13());
	    				break;
	    			}
	    		} else {
    				ISBN isbn = new ISBN((String) value);
    				dco.setValue(Book._N_ISBN13, isbn.getIsbn13());
	    		}
	    	}
    	} catch (InvalidBarCodeException ibce) {
    		listener.addError("Could not parse ISBN from [" + value + "]. Error: " + ibce.getMessage());
    	}
    }
    
    private void setAuthor(DcObject dco, Map<?, ?> metadata) {
    	Object creators = metadata.get("creator");
    	
    	if (creators != null) {
    		if (creators instanceof Collection) {
    			for (Object creator : (Collection<?>) creators) {
    				dco.createReference(Book._G_AUTHOR, creator);
    			}
    		} else {
    			dco.createReference(Book._G_AUTHOR, creators.toString());
    		}
    	}
    }
    
    private void setPublisher(DcObject dco, Map<?, ?> metadata) {
    	Object publishers = metadata.get("publisher");
    	
    	if (publishers != null) {
    		if (publishers instanceof Collection) {
    			for (Object publisher : (Collection<?>) publishers) {
    				String s = publisher.toString();
    				s = s.indexOf(":") > 0 ? s.substring(s.indexOf(":") + 1) : s;
    				dco.createReference(Book._F_PUBLISHER, s.trim());
    			}
    		} else {
				String s = publishers.toString();
				s = s.indexOf(":") > 0 ? s.substring(s.indexOf(":") + 1) : s;
    			dco.createReference(Book._F_PUBLISHER, s.trim());
    		}
    	}    	
    }     
}