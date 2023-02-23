package org.datacrow.onlinesearch.openlibrary;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
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
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.plugin.IServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

public class OpenLibrarySearch extends SearchTask {
    
    protected final Gson gson;
	
    public OpenLibrarySearch(
            IOnlineSearchClient listener, 
            IServer server, 
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, null, mode, query, additionalFilters);
        
        setMaximum(50);
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    @Override
    protected DcObject getItem(Object key, boolean full) throws Exception {
        OpenLibrarySearchResult olsr = (OpenLibrarySearchResult) key;
        DcObject dco = olsr.getDco();
        
        String address = "https://openlibrary.org/" + olsr.getWorkId() + "/editions.json";
        
        waitBetweenRequest();
        
        HttpConnection conn = new HttpConnection(new URL(address), userAgent);
        String json = conn.getString(StandardCharsets.UTF_8);
        conn.close();

        Map<?, ?> item = gson.fromJson(json, Map.class);
        
        String editionId = (String) item.get("key");
        
        // if there's just one edition; just get that and be done with it (ISBN search)
        // if there's only a work id; get all editions:
        //     https://openlibrary.org/works/OL45804W/editions.json
        

        
        // editions:
        // - number_of_pages
        // - publishers (names)
        // - isbn_10 & isbn_13
        // - physical_format (hardcover, etc)
        // - full_title (if exists)
        // - notes (description)
        // - languages
        // - series
        // - copyright_date
        // - translation_of (original title)
        // - edition_name
        // 
        
        // covers (where not -1): see https://openlibrary.org/books/OL38565767M.json
        // - https://covers.openlibrary.org/b/id/12904717-L.jpg -> if not exists, use edition image. 
        
        
        
        
//        
//        DcObject dco = parseItem(item, aosr);
//        
//        Map<?, ?> metadata = (Map<?, ?>) item.get("metadata");
//
//        if (metadata != null) {
//	        setYear(dco, metadata);
//	        setExtendedDescription(dco, metadata);
//	        setLanguage(dco, metadata);
//        }
        
        dco.addExternalReference(DcRepository.ExternalReferences._OPENLIBRARY, editionId);        		
        		
        setServiceInfo(dco);
        dco.setValue(DcObject._SYS_SERVICEURL, address);
        
        return dco;
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
                 		getQuery() + "&limit=" + getMaximum() + "&fields=key,title,description,cover_edition_key,author_name";
            } else {
            	// fetches an edition
           	 	query = "https://openlibrary.org/isbn/" + getQuery() + ".json";            	
            }
            
            HttpConnection conn = new HttpConnection(new URL(query), userAgent);
            String json = conn.getString(StandardCharsets.UTF_8);
            conn.close();
            
            Map<?, ?> m = gson.fromJson(json, Map.class);
            
            ArrayList<LinkedTreeMap<?, ?>> items = (ArrayList<LinkedTreeMap<?, ?>>) m.get("docs");
            
    		OpenLibrarySearchResult olsr;
            DcObject dco;
            int count = 0;
            String key;
            Map<?, ?> data = null;
            for (Map<?, ?> src : items) {
            	key = (String) src.get("key");

            	dco = DcModules.get(getServer().getModule()).getItem();
            	olsr = new OpenLibrarySearchResult(dco);

            	if (getMode().getFieldBinding() == Book._A_TITLE) { // we're working with a work
            		data = src;
            		olsr.setWorkId(key);
            	} else { // we're working with an edition. We'll store the edition id but also query the work data.
            		olsr.setEditionId(key);
            		olsr.setEditionData(src);
            		
                	Collection works = (Collection) src.get("works");
                	String workId;
                	for (Object work : works) {
                		workId = (String) ((Map<?, ?>) work).get("key");
                		olsr.setWorkId(workId);
                		
                        conn = new HttpConnection(new URL("https://openlibrary.org/works" + workId + ".json"), userAgent);
                        json = conn.getString(StandardCharsets.UTF_8);
                        conn.close();
                		
                        m = gson.fromJson(json, Map.class);
                        items = (ArrayList<LinkedTreeMap<?, ?>>) m.get("docs");                        
                        data = items.get(0); 
                		
                		break;
                	}
            	}
            	
            	dco.setValue(DcMediaObject._A_TITLE, data.get("title"));
            	
            	if (data.containsKey("first_publish_year"))
            		dco.setValue(DcMediaObject._C_YEAR, data.get("first_publish_year"));

            	if (data.containsKey("cover_edition_key"))
            		olsr.setMainCoverId((String) data.get("cover_edition_key"));

            	setAuthors(data, dco);
            	
                count++;
                
                result.add(olsr);
                
                if (count == getMaximum()) break;                
            }
        } catch (Exception e) {
            throw new OnlineServiceError(e);
        }
        
        return result;
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