package org.datacrow.onlinesearch.archiveorg;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.datacrow.core.DcRepository;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcMediaObject;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Software;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.OnlineSearchUserError;
import org.datacrow.core.services.OnlineServiceError;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.StringUtils;
import org.jsoup.Jsoup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

public abstract class ArchiveOrgSearch extends SearchTask {
    
    protected final Gson gson;
    protected final Map<String, String> languages = DcRepository.Collections.getLanguages();
	
    public ArchiveOrgSearch(
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
    
    protected abstract DcObject parseItem(Map<?, ?> item, ArchiveOrgSearchResult aosr);
    protected abstract String getSearchAddress();
    
    @Override
    protected DcObject getItem(Object key, boolean full) throws Exception {
        ArchiveOrgSearchResult aosr = (ArchiveOrgSearchResult) key;
        String address = "https://archive.org/metadata/" + aosr.getId();
        
        waitBetweenRequest();
        
        HttpConnection conn = new HttpConnection(new URL(address), userAgent);
        String json = conn.getString(StandardCharsets.UTF_8);
        conn.close();

        Map<?, ?> item = gson.fromJson(json, Map.class);
        
        DcObject dco = parseItem(item, aosr);
        
        Map<?, ?> metadata = (Map<?, ?>) item.get("metadata");

        if (metadata != null) {
	        setYear(dco, metadata);
	        setExtendedDescription(dco, metadata);
	        setLanguage(dco, metadata);
        }
        
        dco.addExternalReference(DcRepository.ExternalReferences._ARCHIVEORG, String.valueOf(aosr.getId()));
        setServiceInfo(dco);
        dco.setValue(DcObject._SYS_SERVICEURL, address);
        
        return dco;
    }
    
    @Override
    protected DcObject getItem(URL url) throws Exception {
        return null;
    }
    
    @SuppressWarnings("unchecked")
	@Override
    protected Collection<Object> getItemKeys() throws OnlineSearchUserError, OnlineServiceError {
        Collection<Object> result = new ArrayList<>();
        
        waitBetweenRequest();
        
        try {
            String query = getSearchAddress();
            
            String topic = (String) getAdditionalFilters().get(DcResources.getText("lblCollection"));
            if (!CoreUtilities.isEmpty(topic)) {
            	topic = topic.replaceAll(" ", "").replaceAll("-", "");
            	query += "%20AND%20collection:*" + URLEncoder.encode(topic, StandardCharsets.UTF_8) + "*";
            }
            
            HttpConnection conn = new HttpConnection(new URL(query), userAgent);
            
            String json = conn.getString(StandardCharsets.UTF_8);
            conn.close();
            
            Map<?, ?> m = gson.fromJson(json, Map.class);
            
            ArrayList<LinkedTreeMap<?, ?>> items = 
            		(ArrayList<LinkedTreeMap<?, ?>>)
            			((LinkedTreeMap<?, ?>) m.get("response")).get("docs");
            
    		ArchiveOrgSearchResult aosr;
            DcObject dco;
            
            String description;
            int count = 0;
            for (LinkedTreeMap<?, ?> src : items) {
            	dco = DcModules.get(getServer().getModule()).getItem();
            	
            	dco.setValue(DcMediaObject._A_TITLE, src.get("title"));
            	
            	description = getAsString(src.get("description"), "\r\n\r\n");
            	description = description.replaceAll("<br />", "\r\n").replaceAll("<br>", "\r\n");
            	description = description == null ? null : Jsoup.parse(description).text();
            	description = description.replaceAll("<p", "").replaceAll("</p", "");
            	
            	dco.setValue(DcMediaObject._B_DESCRIPTION, description);
            	
            	aosr = new ArchiveOrgSearchResult(dco);
            	aosr.setId((String) src.get("identifier"));
            	
                count++;
                
                result.add(aosr);
                
                if (count == getMaximum()) break;                
            }
        } catch (Exception e) {
            throw new OnlineServiceError(e);
        }
        
        return result;
    }
    
    private void setYear(DcObject dco, Map<?, ?> metadata) {
    	Object year = metadata.get("year");
        if (metadata.containsKey("year")) {
        	if (year instanceof Collection) {
        		for (Object o : (Collection<?>) year) {
        			dco.setValue(DcMediaObject._C_YEAR, o);
        			break;
        		}
        	} else {
        		dco.setValue(DcMediaObject._C_YEAR, year);
        	}
        } else if (metadata.containsKey("date")) {
        	year = metadata.get("date");
        	String s = year.toString();
        	s = s.length() == 10 ? s.substring(0, 4) : s.length() == 4 ? s : null;
        	if (s != null && StringUtils.getContainedNumber(s).length() == 4)
        		dco.setValue(DcMediaObject._C_YEAR, s);
        }
    }
    
    private void setLanguage(DcObject dco, Map<?, ?> metadata) {
    	if (metadata.containsKey("language")) {
    		String language = (String) metadata.get("language");
    		language = languages.get(language.toLowerCase());
    		if (language != null)
    			dco.createReference(Software._D_LANGUAGE, language);
    	}
    }
    
    private void setExtendedDescription(DcObject dco, Map<?, ?> metadata) {
    	String description = (String) dco.getValue(DcMediaObject._B_DESCRIPTION);
    	
        if (metadata.get("notes") != null) {
        	description = CoreUtilities.isEmpty(description) ? "" : description + "\r\n\r\n";
        	description += metadata.get("notes");
        }
    	
    	description = CoreUtilities.isEmpty(description) ? "" : description + "\r\n\r\n";
        description += DcResources.getText("lblArchiveOrgStructure");

        if (metadata.get("collection") != null) {
	        description += "\r\n" + DcResources.getText("lblArchiveOrgCollection") + " ";
	        description += getAsString(metadata.get("collection"), ", ");
        }
        
        if (metadata.get("subject") != null) {
        	description += "\r\n" + DcResources.getText("lblArchiveOrgSubject") + " ";
        	description += getAsString(metadata.get("subject"), ", ");
        }
        
        dco.setValue(DcMediaObject._B_DESCRIPTION, description);
    }
    
    protected void setImages(
    		DcObject dco, 
    		Map<?, ?> item,
    		String[] filterEquals,
    		String[] filterUnequals,
    		int[] fields,
    		String server,
    		String dir) {

    	int fieldIdx = 0;
        
    	@SuppressWarnings("unchecked")
		List<LinkedTreeMap<?, ?>> files = (List<LinkedTreeMap<?, ?>>) item.get("files");
    	String name;
    	String link;
    	byte[] image;
    	long size;
    	
    	for (LinkedTreeMap<?, ?> file : files) {
    		name = (String) file.get("name");
    		name = name.replace(" ", "%20");
    		
    		if (file.get("size") != null) {
	    		size = Long.valueOf((String) file.get("size")).longValue();
	    		
	    		if (size < getMaximumImageSize() &&
	    				(name.toLowerCase().endsWith("jpg") || 
	    				 name.toLowerCase().endsWith("png") || 
	    				 name.toLowerCase().endsWith("jpeg") || 
	    				 name.toLowerCase().endsWith("gif"))) {
	    			
	    			boolean valid = true;
	    			for (String equals : filterEquals) {
	    				valid = name.toLowerCase().contains(equals);
	    				if (valid) break;
	    			}
	    			
	    			for (String unequals : filterUnequals)
	    				valid &= !name.toLowerCase().contains(unequals);
	    			
	    			if (valid) {
		    			link = "https://" + server + dir + "/" + name;
		    			image = getImageBytes(link);
		    			
		    			if (image != null)
		    				dco.setValue(fields[fieldIdx++], image);
		                
		                if (fieldIdx > fields.length - 1) break;
	    			}
	    		}
    		}
    	}
    }
    
    protected long getMaximumImageSize() {
    	return 7500000;
    }
    
    private String getAsString(Object o, String combineWith) {
    	String s = "";
        if (o instanceof String) {
        	s += o;
        } else if (o != null) {
            int i = 0;
            for (Object collection : (Collection<?>) o) {
            	s += i> 0 ? combineWith + collection : collection;
            	i++;
            }        	
        }    	
        return s;
    }
}