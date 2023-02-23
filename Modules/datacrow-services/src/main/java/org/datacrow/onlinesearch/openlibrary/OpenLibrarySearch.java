package org.datacrow.onlinesearch.openlibrary;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.datacrow.core.DcRepository;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcMediaObject;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Book;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.OnlineSearchUserError;
import org.datacrow.core.services.OnlineServiceError;
import org.datacrow.core.services.Region;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.utilities.isbn.ISBN;
import org.datacrow.core.utilities.isbn.InvalidBarCodeException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

public class OpenLibrarySearch extends SearchTask {
    
    protected final Gson gson;
    
    private final HashMap<String, byte[]> workImages = new HashMap<>();
    private final Map<String, String> languages = DcRepository.Collections.getLanguages();
	
    public OpenLibrarySearch(
            IOnlineSearchClient listener, 
            IServer server, 
            SearchMode mode,
            Region region,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, region, mode, query, additionalFilters);
        
        setMaximum(50);
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    @Override
    protected DcObject getItem(Object key, boolean full) throws Exception {
        OpenLibrarySearchResult olsr = (OpenLibrarySearchResult) key;
        DcObject dco = olsr.getDco();
        
        if (checkLanguage(olsr)) {
	    	setEditionData(olsr.getEditionData(), olsr);
	        
	        dco.addExternalReference(DcRepository.ExternalReferences._OPENLIBRARY, olsr.getEditionId());        		
	        		
	        setServiceInfo(dco);
	        dco.setValue(DcObject._SYS_SERVICEURL, "https://openlibrary.org" + olsr.getEditionId() + ".json");
        } else {
        	dco = null;
        }
        
        return dco;
    }
    
    private boolean checkLanguage(OpenLibrarySearchResult olsr) {
    	
    	Map<?, ?> item = olsr.getEditionData();
    	
    	boolean valid = 
    			getRegion().getCode().equals("-") || 
    			!item.containsKey("languages");
    	
    	if (valid) {
    		String language = null;
    		DcObject dco = olsr.getDco();
    		
    		if (item.containsKey("languages")) {
    			Map<?, ?> values = (Map<?, ?>) ((ArrayList<?>) item.get("languages")).get(0);
    			language = (String) values.get("key");
    			language = language.substring(
    					language.lastIndexOf("/") > -1 ? language.lastIndexOf("/") + 1 : 0, language.length());
    			
    			if (languages.containsKey(language))
    				dco.createReference(Book._D_LANGUAGE, languages.get(language));
    		}
    	}

    	return valid;
    }
    
    private void setEditionData(Map<?, ?> item, OpenLibrarySearchResult olsr) {
    	DcObject dco = olsr.getDco();
    	
        String key = (String) item.get("key");
		olsr.setEditionId(key);	
		
		setIsbn(item, dco);
		setTitle(item, dco);
		setCover(item, olsr);

		if (item.containsKey("number_of_pages"))
			dco.setValue(Book._T_NROFPAGES, item.get("number_of_pages"));

    	
        // editions:
        // - publishers (names)
        // - physical_format (hardcover, etc)
        // - notes (description)
        // - languages
        // - series
        // - copyright_date
        // - translation_of (original title)
        // - edition_name
        // 
        
        // covers (where not -1): see https://openlibrary.org/books/OL38565767M.json
        // - https://covers.openlibrary.org/b/id/12904717-L.jpg -> if not exists, use edition image. 
    }
    
    private void setCover(Map<?, ?> item, OpenLibrarySearchResult olsr) {
    	DcObject dco = olsr.getDco();
    	
    	byte[] image = null;
    	if (item.containsKey("covers")) {
    		String coverId = getFirstEntry(item.get("covers"));
    		if (coverId.length() > 4) {
    			waitBetweenRequest();
    			String link = "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";
    			image = getImageBytes(link);
    		}
    	} else {
    		if (workImages.containsKey(olsr.getMainCoverId())) {
    			image = workImages.get(olsr.getMainCoverId());
    		} else {
    			String link = "https://covers.openlibrary.org/b/olid/" + olsr.getMainCoverId() + "-L.jpg";
    			image = getImageBytes(link);
				workImages.put(olsr.getMainCoverId(), image);
    		}
    	}
    	
		if (image != null)
            dco.setValue(Book._K_PICTUREFRONT, image);
    }
    
    private void setTitle(Map<?, ?> item, DcObject dco) {
		if (item.containsKey("title")) {
			String title = (String) item.get("title");
			
			if (item.containsKey("subtitle"))
				title += " " + item.get("subtitle");
			
			dco.setValue(Book._A_TITLE, title);
		}
    }
    
    private void setIsbn(Map<?, ?> item, DcObject dco) {
    	String isbn = "";
    	if (item.containsKey("isbn_13")) {
    		isbn = getFirstEntry(item.get("isbn_13"));
    		// sometimes there's text appended...
    		isbn = isbn.replaceAll(" ", "").replaceAll("-", "");
    		isbn = isbn.length() > 13 ? isbn.substring(0, 13) : isbn;
    	} else if (item.containsKey("isbn_10")) {
    		isbn = getFirstEntry(item.get("isbn_10"));
    		isbn = isbn.replaceAll(" ", "").replaceAll("-", "");
    		isbn = isbn.length() > 10 ? isbn.substring(0, 10) : isbn;
    	}
    	
    	try {
    		isbn = new ISBN(isbn).getIsbn13();
    		dco.setValue(Book._N_ISBN13, isbn);
    	} catch (InvalidBarCodeException ibce) {
    		listener.addError("Could not parse ISBN-13 from [" + isbn + "]. Error: " + ibce.getMessage());
    	}
    }
    
    private String getFirstEntry(Object o) {
    	String result = "";
    	if (o instanceof ArrayList<?>) {
    		ArrayList<?> c = (ArrayList<?>) o;
    		if (c.size() > 0) {
    			o = c.get(0) == null ? "" : c.get(0);
    			result = o instanceof Double ? "" + ((Double) o).longValue() : o.toString();
    		}
    	} else if (o instanceof String) {
    		result = o instanceof Double ? "" + ((Double) o).longValue() : (String) o;
    	}
    	
    	return result;
    }
    
    @Override
    protected DcObject getItem(URL url) throws Exception {
        return null;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    protected Collection<Object> getItemKeys() throws OnlineSearchUserError, OnlineServiceError {
        Collection<Object> result = new ArrayList<>();
        
        waitBetweenRequest();
        
        try {
            String query;
            
            if (getMode().getFieldBinding() == Book._A_TITLE) {
            	// fetches works
            	query = "https://openlibrary.org/search.json?q=" + 
                 		getQuery() + "&limit=" + getMaximum() + 
                 		"&fields=key,title,description,cover_edition_key,author_name,edition_key";
            	
                HttpConnection conn = new HttpConnection(new URL(query), userAgent);
                String json = conn.getString(StandardCharsets.UTF_8);
                conn.close();
                
                Map<?, ?> m = gson.fromJson(json, Map.class);
                
                if (m.containsKey("docs")) {
	                ArrayList<LinkedTreeMap<?, ?>> works = (ArrayList<LinkedTreeMap<?, ?>>) m.get("docs");
	                
	        		OpenLibrarySearchResult olsr;
	                DcObject dco;
	                int count = 0;
	                String key;
	                ArrayList<LinkedTreeMap<?, ?>> editions;
	                
	                for (Map<?, ?> work : works) {
	                	
	                	key = (String) work.get("key");
	                	
	                	waitBetweenRequest();
	                	
	                	// next get the editions for this work
	                    String address = "https://openlibrary.org/" + key + "/editions.json";
	                    
	                    conn = new HttpConnection(new URL(address), userAgent);
	                    json = conn.getString(StandardCharsets.UTF_8);
	                    conn.close();

	                    m = gson.fromJson(json, Map.class);

	                    editions = (ArrayList<LinkedTreeMap<?, ?>>) m.get("entries");
	                    
	                    for (Map<?, ?> edition : editions) {
	                    	// store edition information
	                    	
	                    	dco = DcModules.get(getServer().getModule()).getItem();
		                	olsr = new OpenLibrarySearchResult(dco);
		                	
		                	olsr.setEditionData(edition);
		                	setWorkInformation(work, olsr);
		                	
		                    count++;
		                    
		                    result.add(olsr);
	                    }
	                    
	                    if (count == getMaximum()) break;                
	                }
                }
            	
            } else {
            	// fetches an edition
           	 	query = "https://openlibrary.org/isbn/" + getQuery() + ".json";
           	 	
                HttpConnection conn = new HttpConnection(new URL(query), userAgent);
                String json = conn.getString(StandardCharsets.UTF_8);
                conn.close();
           	 	
                Map<?, ?> item = gson.fromJson(json, Map.class);
                
                if (item != null && item.containsKey("key")) {
	                DcObject dco = DcModules.get(getServer().getModule()).getItem();
	                OpenLibrarySearchResult olsr = new OpenLibrarySearchResult(dco);
	                
	        		olsr.setEditionData(item);
	        		
	            	Collection works = (Collection) item.get("works");
	            	String workId;
	            	String link;
	            	for (Object work : works) {
	            		workId = (String) ((Map<?, ?>) work).get("key");
	            		olsr.setWorkId(workId);
	            		
	            		waitBetweenRequest();
	            		
	            		link = "https://openlibrary.org/search.json?q="+ workId +
	            				"&fields=key,title,description,cover_edition_key,author_name";
	            		
	                    conn = new HttpConnection(new URL(link), userAgent);
	                    json = conn.getString(StandardCharsets.UTF_8);
	                    conn.close();
	            		
	                    item = gson.fromJson(json, Map.class);
	                    
	                    if (item.containsKey("docs")) {
		                    item = ((ArrayList<LinkedTreeMap<?, ?>>) item.get("docs")).get(0);
		                    setWorkInformation(item, olsr);
		                    result.add(olsr);
	                    }
	            		
	            		break; // we assume we're dealing with one work, not multiple
	            	}
                }
            }
        } catch (Exception e) {
            throw new OnlineServiceError(e);
        }
        
        return result;
    }
    
    private void setWorkInformation(Map<?, ?> work, OpenLibrarySearchResult olsr) {
    	String key = (String) work.get("key");
    	olsr.setWorkId(key);
    	
    	DcObject dco = olsr.getDco();
    	
    	dco.setValue(DcMediaObject._A_TITLE, work.get("title"));
    	
    	if (work.containsKey("first_publish_year"))
    		dco.setValue(DcMediaObject._C_YEAR, work.get("first_publish_year"));

    	if (work.containsKey("cover_edition_key"))
    		olsr.setMainCoverId((String) work.get("cover_edition_key"));

    	setAuthors(work, dco);
    }
    
    @SuppressWarnings("rawtypes")
	private void setAuthors(Map<?, ?> data, DcObject dco) {
    	if (data.containsKey("author_name")) {
    		Collection authors = (Collection) data.get("author_name");
    		String name;
    		for (Object author : authors) {
    			name = author.toString();
    			dco.createReference(Book._G_AUTHOR, name);
    		}
    	}
    }
}