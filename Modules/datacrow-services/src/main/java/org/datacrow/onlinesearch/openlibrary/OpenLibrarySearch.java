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

package org.datacrow.onlinesearch.openlibrary;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.datacrow.core.DcRepository;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.http.HttpConnectionException;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcMediaObject;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Book;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.OnlineSearchUserError;
import org.datacrow.core.services.OnlineServiceError;
import org.datacrow.core.services.Region;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.StringUtils;
import org.datacrow.core.utilities.isbn.ISBN;
import org.datacrow.core.utilities.isbn.InvalidBarCodeException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

public class OpenLibrarySearch extends SearchTask {
    
    protected final Gson gson;
    
    private final HashMap<String, DcImageIcon> workImages = new HashMap<>();
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
	    	setEditionInformation(olsr.getEditionData(), olsr);
	        
	        dco.addExternalReference(DcRepository.ExternalReferences._OPENLIBRARY, olsr.getEditionId());        		
	        		
	        setServiceInfo(dco);
	        dco.setValue(DcObject._SYS_SERVICEURL, "https://openlibrary.org" + olsr.getEditionId() + ".json");
        } else {
        	dco = null;
        }
        
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
                String search = "q=" + getQuery();
                String author = (String) getAdditionalFilters().get(DcResources.getText("lblAuthor"));
                
                if (!CoreUtilities.isEmpty(author))
                    search += "&author=" + httpFormat(author);
                
            	// fetches works
            	query = "https://openlibrary.org/search.json?" + search + "&limit=" + getMaximum() + 
                 		"&fields=key,title,description,cover_edition_key,author_name,edition_key,first_publish_year";
            	
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
	                    
	                    listener.addMessage(DcResources.getText("msgRetrievingEditions", key));
	                    
	                    try {
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
			                	olsr.setWorkData(work);
			                	
			                	setWorkInformation(work, olsr);
			                	
			                    result.add(olsr);
		                    }
	                    
		                    count++;
	                    } catch (HttpConnectionException hce) {
	                    	listener.addMessage(
	                    			DcResources.getText("msgEditionsNotAvailable", 
	                    			new String[] {key, hce.getMessage()}));
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
	            		
	            		link = "https://openlibrary.org/search.json?q="+ workId;
	            		
	                    conn = new HttpConnection(new URL(link), userAgent);
	                    json = conn.getString(StandardCharsets.UTF_8);
	                    conn.close();
	            		
	                    item = gson.fromJson(json, Map.class);
	                    
	                    if (item.containsKey("docs")) {
	                        // get the correct record here as there could be multiple!
	                        for (LinkedTreeMap<?, ?> r : (ArrayList<LinkedTreeMap<?, ?>>) item.get("docs")) {
	                            if (r.get("key").equals(workId)) {
	                                setWorkInformation(r, olsr);
	                                olsr.setWorkData(r);
	                                
	                                result.add(olsr);
	                                
	                                // pick the first matching record
	                                break;
	                            }
	                        }
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
    
    private String getDescription(Map<?, ?> work, String key) {
    	
    	String description = "";
    	
    	try {
    		// if the description has been retrieved before for the given work key - use this
    		// else if the work details already have a description key - use this
    		// else query for the description and store this in the cache.
    		if (workDescription.containsKey(key)) {
    			description = workDescription.get(key);
    		} else if (work.containsKey("description")) {
	    		description = getDescriptionValue(work, "description");
	    		workDescription.put(key, description);
	    	} else { 
	    		query = "https://openlibrary.org" + key + ".json";
	    		
	            try {
	                sleep(200);
	            } catch (InterruptedException ie) {
	                listener.addError("Error, could not wait while retrieving description");
	            }
	    		
	            HttpConnection conn = new HttpConnection(new URL(query), userAgent);
	            String json = conn.getString(StandardCharsets.UTF_8);
	            conn.close();
	       	 	
	            Map<?, ?> item = gson.fromJson(json, Map.class);
	            description = getDescriptionValue(item, "description");
	            
	            workDescription.put(key, description);
	    	}
    	} catch (Exception e) {
    		listener.addError("Could not retrieve description for [" + key + "]. Error: " + e.getMessage());
    	}
    	
    	return description;
    }
    
    /**
     * Gets the string for the given tag. It checks whether the value is a simple String or
     * whether the value is of type Map where the map contains the value tag.
     */
    private String getDescriptionValue(Map<?, ?> item, String tag) {
    	Object o  = item.get(tag);
    	
    	String s = "";
    	if (o instanceof String) {
    		s = (String) o;
    	} else if (o instanceof Map<?, ?>) {
    		Map<?, ?> d = (Map<?, ?>) o;
    		s = d.containsKey("value") ? (String) d.get("value") : s;
    	}
    	
    	return s;
    }
    
    private void setWorkInformation(Map<?, ?> work, OpenLibrarySearchResult olsr) throws Exception {
    	String key = (String) work.get("key");
    	olsr.setWorkId(key);
    	
    	DcObject dco = olsr.getDco();
    	
    	dco.setValue(DcMediaObject._A_TITLE, work.get("title"));
    	
    	if (work.containsKey("first_publish_year")) {
    	    Long year = Long.valueOf(((Number) work.get("first_publish_year")).longValue());
    		dco.setValue(Book._AA_YEARFIRSTPUBLICATION, year);
    	}

    	if (work.containsKey("cover_edition_key"))
    		olsr.setMainCoverId((String) work.get("cover_edition_key"));
    	else if (work.containsKey("cover_i"))
    	    olsr.setMainCoverId("" + ((Double) work.get("cover_i")).intValue());
        else if (work.containsKey("cover_id"))
            olsr.setMainCoverId("" + ((Double) work.get("cover_id")).intValue());

        setLanguages(work, dco);
    	
    	setAuthors(work, dco);
    }
    
    private Map<String, String> workDescription = new HashMap<>();
    
    private void setEditionInformation(Map<?, ?> item, OpenLibrarySearchResult olsr) {
    	DcObject dco = olsr.getDco();
    	
        String key = (String) item.get("key");
		olsr.setEditionId(key);	
		
		
		String description = getDescription(olsr.getWorkData(), olsr.getWorkId());
    	if (!CoreUtilities.isEmpty(description))
    		dco.setValue(Book._B_DESCRIPTION, description);
		
		setIsbn(item, dco);
		setTitle(item, dco);
		setCover(item, olsr);
		setPublishers(item, dco);
		setTranslatedFrom(item, dco);
		setYear(item, dco);
		setTranslators(item, dco);
		
		dco.setValue(Book._H_WEBPAGE, "https://openlibrary.org" + key);

		if (item.containsKey("number_of_pages"))
			dco.setValue(Book._T_NROFPAGES, item.get("number_of_pages"));

		if (item.containsKey("translation_of"))
			dco.setValue(Book._X_ORIGINAL_TITLE, item.get("translation_of"));
		
		if (item.containsKey("physical_format"))
			dco.createReference(Book._U_BINDING, item.get("physical_format"));
		
		setGenres(item, dco);
		
		if (item.containsKey("edition_name"))
			dco.setValue(Book._W_EDITION_COMMENT, item.get("edition_name"));
		
		setSeries(item, dco);
    }

    private void setLanguages(Map<?, ?> item, DcObject dco) {
        if (item.containsKey("language")) {
            for (Object language : ((ArrayList<?>) item.get("language"))) {
                if (languages.containsKey(language))
                    dco.createReference(Book._D_LANGUAGE, languages.get(language));        
            }
        }
    }
    
    /**
     * Used to check if the edition matches the selected language
     */
    private boolean checkLanguage(OpenLibrarySearchResult olsr) {
    	
    	Map<?, ?> item = olsr.getEditionData();
    	
    	String code = getRegion().getCode();
    	
    	boolean valid = code.equals("-");
    	
		String language = null;
		DcObject dco = olsr.getDco();
		
		if (!valid && item.containsKey("languages")) {
			Map<?, ?> values = (Map<?, ?>) ((ArrayList<?>) item.get("languages")).get(0);
			language = (String) values.get("key");
			language = language.substring(
					language.lastIndexOf("/") > -1 ? language.lastIndexOf("/") + 1 : 0, language.length());
			
			// check if the language matches our filter.
			valid |= language.equals(code);
			
			// and create a reference (regardless of validity)
			if (languages.containsKey(language))
				dco.createReference(Book._D_LANGUAGE, languages.get(language));
		}

    	return valid;
    }
    
    private void setSeries(Map<?, ?> item, DcObject dco) {
    	Collection<String> series = getList(item, "series");

    	String s = "";
		for (Object serie : (Collection<?>) series)
			s += s.length() > 0 ? ", " + serie : serie;
    			
		dco.setValue(Book._O_SERIES, s);
    }
    
    private void setGenres(Map<?, ?> item, DcObject dco) {
    	Collection<String> genres = getList(item, "genres");
    	for (String genre : genres) {
    		genre = genre.endsWith(".") ? genre.substring(0, genre.length() - 1) : genre;
			dco.createReference(Book._I_CATEGORY, genre.trim());
		}
    }
    
    private void setYear(Map<?, ?> item, DcObject dco) {
    	String date = item.containsKey("publish_date") ? 
    			(String) item.get("publish_date") : 
    			(String) item.get("copyright_date");  
    	
    	if (!CoreUtilities.isEmpty(date)) {
    		date = date.length() > 4 ? date.substring(0, 4) : date;
    		
    		if (StringUtils.getContainedNumber(date).length() == 4)
    			dco.setValue(Book._C_YEAR, date);
    	}
    }
    
    private void setTranslatedFrom(Map<?, ?> item, DcObject dco) {
    	String language;
    	if (item.containsKey("translated_from")) {
			Map<?, ?> values = (Map<?, ?>) ((ArrayList<?>) item.get("translated_from")).get(0);
			language = (String) values.get("key");
			language = language.substring(
					language.lastIndexOf("/") > -1 ? language.lastIndexOf("/") + 1 : 0, language.length());

			if (languages.containsKey(language))
				dco.createReference(Book._Y_TRANSLATED_FROM, languages.get(language));
		}    	
    }

    private void setPublishers(Map<?, ?> item, DcObject dco) {
    	Collection<String> publishers = getList(item, "publishers");

		for (String publisher : publishers)
			dco.createReference(Book._F_PUBLISHER, publisher);
    }
    
    private void setCover(Map<?, ?> item, OpenLibrarySearchResult olsr) {
    	DcObject dco = olsr.getDco();
    	
    	DcImageIcon image = null;
    	if (item.containsKey("covers")) {
    		String coverId = getFirstEntry(item.get("covers"));
    		if (coverId.length() > 4) {
    			waitBetweenRequest();
    			String link = "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";
    			image = CoreUtilities.downloadAndStoreImage(link);
    		}
    	}

    	if (image == null && !CoreUtilities.isEmpty(olsr.getMainCoverId())) {
    		if (workImages.containsKey(olsr.getMainCoverId())) {
    			image = workImages.get(olsr.getMainCoverId());
    		} else {
    			String link = "https://covers.openlibrary.org/b/id/" + olsr.getMainCoverId() + "-L.jpg";
    			image = CoreUtilities.downloadAndStoreImage(link);
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
        String isbn10 = null;
        String isbn13 = null;
        ISBN isbnConvert;
        
    	if (item.containsKey("isbn_13")) {
    		isbn13 = getFirstEntry(item.get("isbn_13"));
    		// sometimes there's text appended...
    		isbn13 = isbn13.replaceAll(" ", "").replaceAll("-", "");
    		isbn13 = isbn13.length() > 13 ? isbn13.substring(0, 13) : isbn13;
    		try {
    		    isbnConvert = new ISBN(isbn13);
                dco.setValue(Book._N_ISBN13, isbnConvert.getIsbn13());
                
                isbn10 = isbnConvert.getIsbn10();
                dco.setValue(Book._J_ISBN10, isbn10);
            } catch (InvalidBarCodeException ibce) {
                listener.addError("Could not parse ISBN-13 from [" + isbn13 + "]. Error: " + ibce.getMessage());
            }    		
    	}
    		
		if (item.containsKey("isbn_10") && isbn10 == null) {
		    isbn10 = getFirstEntry(item.get("isbn_10"));
		    isbn10 = isbn10.replaceAll(" ", "").replaceAll("-", "");
		    isbn10 = isbn10.length() > 10 ? isbn10.substring(0, 10) : isbn10;
    		
            try {
                isbnConvert = new ISBN(isbn10);
                dco.setValue(Book._J_ISBN10, isbnConvert.getIsbn10());
                
                // calculate the ISBN 13 in case it is still blank
                if (isbn13 == null)
                    dco.setValue(Book._N_ISBN13, isbnConvert.getIsbn13());
            } catch (InvalidBarCodeException ibce) {
                listener.addError("Could not parse ISBN-10 from [" + isbn10 + "]. Error: " + ibce.getMessage());
            }     		
    	}
    }
    
    @SuppressWarnings("unchecked")
    private void setTranslators(Map<?, ?> data, DcObject dco) {
        if (data.containsKey("contributors")) {
            Collection<Map<?, ?>> contributors = (Collection<Map<?, ?>>)  data.get("contributors");
            
            for (Map<?, ?> contributor : contributors) {
                if ("Translator".equalsIgnoreCase((String) contributor.get("role"))) {
                    dco.createReference(Book._Z_TRANSLATOR, contributor.get("name"));
                }
            }
        }
    }
    
	private void setAuthors(Map<?, ?> data, DcObject dco) {
		Collection<String> authors = getList(data, "author_name");
		for (String author : authors)
			dco.createReference(Book._G_AUTHOR, author);
    }
	
    private Collection<String> getList(Map<?, ?> item, String tag) {
    	Object o = item.get(tag);
    	
    	Collection<String> v = new ArrayList<>();
    	
		if (o instanceof String) {
			v.add((String) o);
		} else if (o instanceof Collection) {
			for (Object i : (Collection<?>) o)
				v.add((String) i);
		}
    	return v;
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
}