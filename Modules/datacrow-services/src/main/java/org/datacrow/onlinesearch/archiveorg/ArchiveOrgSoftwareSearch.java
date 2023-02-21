package org.datacrow.onlinesearch.archiveorg;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.datacrow.core.DcRepository;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.log.DcLogManager;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

public class ArchiveOrgSoftwareSearch extends SearchTask {
    
    private static Logger logger = DcLogManager.getLogger(ArchiveOrgSoftwareSearch.class.getName());
    
    private final Gson gson;
	
	private final String address = 
			"https://archive.org/advancedsearch.php?fl[]=identifier&fl[]=avg_rating&fl[]=collection&fl[]=date&fl[]=description&fl[]=format&fl[]=language&" +
			"fl[]=mediatype&fl[]=name&fl[]=subject&fl[]=title&fl[]=type&fl[]=volume&fl[]=week&fl[]=year&rows=50&output=json";
			
    public ArchiveOrgSoftwareSearch(
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
        ArchiveOrgSearchResult aosr = (ArchiveOrgSearchResult) key;
        DcObject dco =  aosr.getDco();
        
        String address = "https://archive.org/metadata/" + aosr.getId();
        
        waitBetweenRequest();
        
        HttpConnection conn = new HttpConnection(new URL(address), userAgent);
        String json = conn.getString(StandardCharsets.UTF_8);
        conn.close();

        Map<?, ?> item = gson.fromJson(json, Map.class);

        // images
        setImages(dco, item, aosr.getId());
        
        Map<?, ?> metadata = (Map<?, ?>) item.get("metadata");

        if (metadata != null) {
	        // year
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
	        }
	
	        // URL
	        dco.setValue(Software._I_WEBPAGE, "https://archive.org/details/" + aosr.getId());
	
	        // Other
	        setExtendedDescription(dco, metadata);
	        setDevelopers(dco, metadata);
	        setLanguage(dco, metadata);
        }

        dco.addExternalReference(DcRepository.ExternalReferences._ARCHIVEORG, String.valueOf(aosr.getId()));
        setServiceInfo(dco);
        dco.setValue(Software._SYS_SERVICEURL, address);
        
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
            String query = address + "&q=title:%22" + getQuery() + "%22%20AND%20mediatype:%22software%22";
            
            String topic = (String) getAdditionalFilters().get(DcResources.getText("lblCollection"));
            if (!CoreUtilities.isEmpty(topic)) {
            	topic = topic.replaceAll(" ", "").replaceAll("-", "").replaceAll("_", "");
            	query += "%20AND%20collection:*" + URLEncoder.encode(topic, StandardCharsets.UTF_8) + "*";
            }
            
            HttpConnection conn = new HttpConnection(new URL(query), userAgent);
            
            String json = conn.getString(StandardCharsets.UTF_8);
            conn.close();
            
            Map<?, ?> softwareItems = gson.fromJson(json, Map.class);
            
            ArrayList<LinkedTreeMap<?, ?>> results = 
            		(ArrayList<LinkedTreeMap<?, ?>>)  ((LinkedTreeMap<?, ?>) softwareItems.get("response")).get("docs");
            
            ArchiveOrgSearchResult aosr;
            DcObject dco;
            
            int count = 0;
            for (LinkedTreeMap<?, ?> src : results) {
            	dco = new Software();
            	
            	dco.setValue(DcMediaObject._A_TITLE, src.get("title"));
            	dco.setValue(DcMediaObject._B_DESCRIPTION, src.get("description"));
            	
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
    
    
    private void setImages(DcObject dco, Map<?, ?> item, String id) {
    	
    	// exit clause #1
    	if (!item.containsKey("files")) {
    		logger.debug("No files present for " + id + ". Aborting picture retrieval.");
    		return;
    	}

    	String server = (String) item.get("d1");
    	String dir = (String) item.get("dir");
    	
    	// exit clause #2
    	if (CoreUtilities.isEmpty(server) || CoreUtilities.isEmpty(dir)) {
    		listener.addError(
    				"No server defined or no directoty defined for the files of " + id + 
    				". Aborting picture retrieval.");
    		return;
    	}
    	
    	id = URLEncoder.encode(id, StandardCharsets.UTF_8);
    	
    	setBoxArt(dco, item, id, server, dir);
    	setScreenshots(dco, item, id, server, dir);
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
    
    private void setLanguage(DcObject dco, Map<?, ?> metadata) {
    	if (metadata.containsKey("language")) {
    		String language = (String) metadata.get("language");
    		language = (String) DcRepository.Collections.languages.get(language.toLowerCase());
    		if (language != null)
    			dco.createReference(Software._D_LANGUAGE, language);
    	}
    }
    
    private void setExtendedDescription(DcObject dco, Map<?, ?> metadata) {
    	String description = (String) dco.getValue(DcMediaObject._B_DESCRIPTION);
    	
    	description = CoreUtilities.isEmpty(description) ? "" : description + "\r\n\r\n";
        description += DcResources.getText("lblArchiveOrgStructure");

        if (metadata.get("collection") != null) {
	        description += "\r\n" + DcResources.getText("lblArchiveOrgCollection") + " ";
	        description += getCommaSeparatedString(metadata.get("collection"));
        }
        
        if (metadata.get("subject") != null) {
        	description += "\r\n" + DcResources.getText("lblArchiveOrgSubject") + " ";
        	description += getCommaSeparatedString(metadata.get("subject"));
        }

        dco.setValue(DcMediaObject._B_DESCRIPTION, description);
    }
    
    private String getCommaSeparatedString(Object o) {
    	String s = "";
        if (o instanceof String) {
        	s += o;
        } else if (o != null) {
            int i = 0;
            for (Object collection : (Collection<?>) o) {
            	s += i> 0 ? ", " + collection : collection;
            	i++;
            }        	
        }    	
        return s;
    }
    
    private void setBoxArt(DcObject dco, Map<?, ?> item, String id, String server, String dir) {
    	
    	int[] fields = new int[] {Software._M_PICTUREFRONT, Software._N_PICTUREBACK};
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
	    		
	    		if (   !name.toLowerCase().contains("thumb") &&
	    				size < 6000000 &&
	    			   (	name.toLowerCase().contains("box") || 
	    					name.toLowerCase().contains("cover") || 
	    					name.toLowerCase().contains("front") ||
	    					name.toLowerCase().contains("back")) &&
	    			   (	name.toLowerCase().endsWith("jpg") || 
	    					name.toLowerCase().endsWith("png") || 
	    					name.toLowerCase().endsWith("jpeg") || 
	    					name.toLowerCase().endsWith("gif"))) {
	    			
	    			link = "https://" + server + dir + "/" + name;
	    			image = getImageBytes(link);
	    			
	    			if (image != null)
	    				dco.setValue(fields[fieldIdx++], image);
	                
	                if (fieldIdx > 1) break;
	    		}
    		}
    	}
    }
    
    private void setScreenshots(DcObject dco, Map<?, ?> item, String id, String server, String dir) {
    	int[] fields = new int[] {Software._P_SCREENSHOTONE, Software._Q_SCREENSHOTTWO, Software._R_SCREENSHOTTHREE};
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
	    		
	    		if (   !name.contains("thumb") &&
	    				size < 6000000 &&
	     			   (	!name.toLowerCase().contains("box") && 
	       					!name.toLowerCase().contains("cover") && 
	       					!name.toLowerCase().contains("front") &&
	       					!name.toLowerCase().contains("back")) &&    				
	    			   (	name.toLowerCase().endsWith("jpg") || 
	    					name.toLowerCase().endsWith("png") || 
	    					name.toLowerCase().endsWith("jpeg") || 
	    					name.toLowerCase().endsWith("gif"))) {
	    			
	    			link = "https://" + server + dir + "/" + name;
	    			image = getImageBytes(link);
	    			
	    			if (image != null)
	    				dco.setValue(fields[fieldIdx++], image);
	                
	                if (fieldIdx > 2) break;
	    		}
	    	}
    	}
    }    
}