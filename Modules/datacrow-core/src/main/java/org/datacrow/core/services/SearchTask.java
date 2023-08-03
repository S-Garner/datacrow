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

package org.datacrow.core.services;

import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.imageio.ImageIO;

import org.datacrow.core.DcConfig;
import org.datacrow.core.http.HttpConnectionUtil;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.utilities.CoreUtilities;

/**
 * A search task performs the actual online search. The search task is used by the
 * online search form (see {@link OnlineSearchForm}) and by processed such as the mass
 * update.
 * 
 * The search is performed in multiple steps. 
 * 1) First the online service (web page or web server) is queried using the input 
 *    of the user ({@link #getItemKeys()}).
 * 2) For each result the item details are retrieved. See ({@link #run()}) and 
 *    {@link #getItems(String, boolean)} 
 * 3) The in step 2 retrieved items only contain the bare minimum of information.
 *    When the user (or any other process) selects one of the items the full details
 *    need to be retrieved ({@link #getItem(URL)})
 * 
 * This class needs to be extended for specific implementations.
 * 
 * @author Robert Jan van der Waals
 */
public abstract class SearchTask extends Thread {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(SearchTask.class.getName());

	protected static final String userAgent = "DataCrow/" + DcConfig.getInstance().getVersion().toString() +  " +https://datacrow.org";
	
    // retrieve minimal item details
    public static final int _ITEM_MODE_SIMPLE = 0;
    // retrieve full item details
    public static final int _ITEM_MODE_FULL = 1;
    
    protected final IOnlineSearchClient listener;
    
    private int maximum = 20;
    protected String query;
    private boolean isCancelled = false;
    
    private final String input;
    
    private final Map<String, Object> additionalFilters;
    
    // The currently used URL or address
    private final String address;
    // The selected server
    private final IServer server;
    // The selected search mode
    private SearchMode searchMode;
    // The selected region (EN, US, NL, ..)
    private final Region region;
    // The selected item retrieval mode
    private int itemMode = _ITEM_MODE_SIMPLE;
    
    private DcObject client;
    
    /**
     * Creates the search task.
     * @param listener
     * @param server
     * @param region
     * @param mode
     * @param query
     */
    public SearchTask(
            IOnlineSearchClient listener, 
            IServer server,
            Region region,
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {

		this.listener = listener;
		this.region = region;
		this.searchMode = mode;
		this.server = server;
		this.address = region != null ? region.getUrl() : server.getUrl();
		this.query = query;
		this.input = query;
		this.additionalFilters = additionalFilters;
	}

    /**
     * Sets the service info. This information is set on every item. This way Data Crow
     * knows where the retrieved information originally came from.
     */
    protected final void setServiceInfo(DcObject dco) {
        String service =  server.getName() + " / " + 
                         (region != null ? region.getCode() : "none") + " / " +
                         (searchMode != null ? searchMode.getDisplayName() : "none") + " / " + 
                          "value=[" + query + "]";
        dco.setValue(DcObject._SYS_SERVICE, service);
    }

    /**
     * Sets the item retrieval mode: {@link #_ITEM_MODE_FULL} or {@link #_ITEM_MODE_SIMPLE}.
     */
    public final void setItemMode(int mode) {
        this.itemMode = mode;
    }

    public boolean isItemModeSupported() {
        return true;
    }
    
    /**
     * Returns the retrieval mode: {@link #_ITEM_MODE_FULL} or {@link #_ITEM_MODE_SIMPLE}.
     */
    public final int getItemMode() {
        return itemMode;
    }
    
    /**
     * Set the maximum amount of items to be retrieved.
     */
    public final void setMaximum(int maximum) {
        this.maximum = maximum;
    }
    
    /**
     * Cancel the search.
     */
    public final void cancel() {
        isCancelled = true;
    }

    /**
     * Indicates if the search was (attempted) to be canceled.
     */
    public final boolean isCancelled() {
        return isCancelled;
    }

    /**
     * The currently used URL or address.
     */
    public final String getAddress() {
        return address;
    }

    /**
     * The currently used search mode.
     * @see SearchMode
     */
    public final SearchMode getMode() {
        return searchMode;
    }

    /**
     * The currently used region
     * @see Region.
     */
    public final Region getRegion() {
        return region;
    }
    
    public DcObject getClient() {
        return client;
    }

    public void setClient(DcObject client) {
        this.client = client;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setMode(SearchMode searchMode) {
        this.searchMode = searchMode;
    }

    public String httpFormat(String v) {
        String s = v;
        s = s.replaceAll("\n", " ");
        s = s.replaceAll("\r", " ");
        s = URLEncoder.encode(s, StandardCharsets.UTF_8);
        return s;
    }
    
    /**
     * The used query as specified by the user.
     */
    public String getQuery() {
        return httpFormat(query);
    }

    public Map<String, Object> getAdditionalFilters() {
        return additionalFilters;
    }
    
    /**
     * The currently used server
     * @see IServer
     */
    public final IServer getServer() {
        return server;
    }

    /**
     * The maximum amount of items to be retrieved.
     */
    public final int getMaximum() {
        return maximum;
    }    

    /**
     * The character used to substitute white spaces from the query (see {@link #getQuery()}).
     * Should be overridden by specific implementations.
     */
    public String getWhiteSpaceSubst() {
        return "+";
    }

    /**
     * Queries for the specified item. The service information (see {@link #setServiceInfo(DcObject)}) 
     * is used to retrieve the information.
     * @param dco The item to be updated.
     * @return The retrieved item or null if no item could be found.
     * @throws Exception
     */
    public DcObject query(DcObject dco) throws Exception {
        String link = (String) dco.getValue(DcObject._SYS_SERVICEURL); 
        if (link != null && link.length() > 0) {
            DcObject item = getItem(new URL(link));
            
            item = item == null ? dco : item;
            
            setServiceInfo(item);
            return item;
        }

        return null;
    }

    /**
     * Query for the item(s) using the web key. 
     * Note that a key is can be a fully qualified URL, an external ID or something else.
     * @param key The item key (The specific implementation decides the meaning of a key)
     * @param full Indicates if the full details should be retrieved.
     */
    protected Collection<DcObject> getItems(Object key, boolean full) throws Exception {
        Collection<DcObject> items = new ArrayList<DcObject>();
        DcObject dco = getItem(key, full);
        if (dco != null) items.add(dco);
        return items;
    }

    
    protected void waitBetweenRequest() {
        try {
            sleep(server.getWaitTimeBetweenRequest());
        } catch (InterruptedException ie) {
            logger.debug("Could not wait during image retrieval");
        }
    }
    
    /**
     * Query for the item using the web key. 
     * @param key The item key (The specific implementation decides the meaning of a key)
     * @param full Indicates if the full details should be retrieved.
     */
    protected abstract DcObject getItem(Object key, boolean full) throws Exception;
    
    /**
     * Query for the item via the URL 
     * @param url The direct link to the external item details.
     */
    protected abstract DcObject getItem(URL url) throws Exception;
    
    /**
     * Get every web ID from the page. With these IDs it should be possible to 
     * get to the detailed item information. 
     * @return The item keys or an empty collection.
     */
    protected abstract Collection<Object> getItemKeys() throws OnlineSearchUserError, OnlineServiceError;
    
    protected void preSearchCheck() {}
    
    /**
     * Here the actual search is performed. This is a standard implementation suited for
     * all online searches. 
     */
    @Override
    public void run() {
        
        preSearchCheck();
        
        Collection<Object> keys = new ArrayList<Object>();

        listener.addMessage(DcResources.getText("msgConnectingToServer", getAddress()));

        try {
            keys.addAll(getItemKeys());
        } catch (OnlineSearchUserError osue) {
            listener.notifyUser(osue.getMessage());
            logger.error(osue, osue);
        } catch (OnlineServiceError ose) {
            listener.addError(ose);
            logger.error(ose, ose);
        }
        
        listener.processingTotal(keys.size());

        if (keys.size() == 0) {
            listener.addMessage(DcResources.getText("msgNoResultsForKeywords", input));
            listener.stopped();
            return;
        }

        listener.addMessage(DcResources.getText("msgFoundXResults", String.valueOf(keys.size())));
        listener.addMessage(DcResources.getText("msgStartParsingXResults", String.valueOf(keys.size())));
        int counter = 0;
        
        for (Object key : keys) {
            
            if (isCancelled() || counter == getMaximum()) break;
            
            try {
                for (DcObject dco : getItems(key, getItemMode() == _ITEM_MODE_FULL)) {
                    dco.setIDs();
                    setServiceInfo(dco);
                    
                    listener.addMessage(DcResources.getText("msgParsingSuccessfull", dco.toString()));
                    listener.addObject(dco);
                    sleep(1000);
                }
                listener.processed(counter);
            } catch (Exception exp) {
                listener.addMessage(DcResources.getText("msgParsingError", "" + exp));
                logger.error(DcResources.getText("msgParsingError", "" + exp), exp);
                listener.processed(counter);
            }
            
            counter++;
        }
        
        listener.processed(counter);
        listener.stopped();        
    }
    
    protected DcImageIcon getImage(String url) {
    	
    	url = url.replace("http://", "https://");
    	
        try {
			Image img = ImageIO.read(new URL(url));
			DcImageIcon icon = new DcImageIcon(img);
			
			// write to temp folder
			File file = new File(CoreUtilities.getTempFolder(), CoreUtilities.getUniqueID() + ".jpg");
			CoreUtilities.writeMaxImageToFile(icon, file);
			
			// delete the file on exit of Data Crow
			file.deleteOnExit();
			
			// flush the in memory image
			icon.flush();
			
			// send image pointing to local disk storage
			return new DcImageIcon(file);
        } catch (Exception e) {
            logger.debug("Cannot download image from [" + url + "]", e);
        }
        return null;    	
    }
    
    @Deprecated
    protected byte[] getImageBytes(String url) {
        url = url.replace("http://", "https://");
        try {
            if (url != null && url.length() > 0) {
                byte[] b = HttpConnectionUtil.retrieveBytes(url);
                if (b != null && b.length > 50)
                    return b;
            }
        } catch (Exception e) {
            logger.debug("Cannot download image from [" + url + "]", e);
        }
        return null;
    }
}
