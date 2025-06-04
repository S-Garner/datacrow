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

package org.datacrow.onlinesearch.mobygames;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.datacrow.core.DcRepository;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.http.HttpConnectionUtil;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Software;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.OnlineSearchUserError;
import org.datacrow.core.services.OnlineServiceError;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.SearchTaskUtilities;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.CoreUtilities;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

@SuppressWarnings("rawtypes")
public class MobyGamesSearch extends SearchTask {

    private MobyGamesPlatform platform;
    private String apiKey;
    
    private final Gson gson = new Gson();
    
    public MobyGamesSearch(
            IOnlineSearchClient listener, 
            IServer server, 
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, null, mode, query, additionalFilters);
        
        apiKey = DcSettings.getString(DcRepository.Settings.stMobyGamesApiKey);
        
        if (apiKey != null)
            apiKey = apiKey.replace("+", "%2B");
    }

    @Override
    protected DcObject getItem(Object key, boolean full) throws Exception {
        MobyGamesResult mgr = (MobyGamesResult) key;
        DcObject item = mgr.getDco();
        
        setPictures(mgr, item);
        setServiceInfo(item);
        
        return item; 
    }

    @Override
    protected DcObject getItem(URL url) throws Exception {
        return null;
    }

    @Override
    public String getWhiteSpaceSubst() {
        return "%20";
    }

    @Override
    protected void preSearchCheck() {
        SearchTaskUtilities.checkForIsbn(this);
    }
    
    private String getBaseUrl() throws OnlineSearchUserError {
        if (CoreUtilities.isEmpty(apiKey)) {
            String msg = DcResources.getText("msgMobyGamesNoApiKeyDefined");
            msg = "<html>" + msg + "<br><u><a href=\"https://www.mobygames.com/mobyplus/\">https://www.mobygames.com/mobyplus/</a></u></html>";
            
            throw new OnlineSearchUserError(msg);
        } else {
            return "https://api.mobygames.com/v2/games?api_key=" + apiKey;
        }
    }
    
    @Override
    protected Collection<Object> getItemKeys() throws OnlineServiceError, OnlineSearchUserError {
        Collection<Object> keys = new ArrayList<Object>();
        
        String sUrl = getBaseUrl();
        sUrl += "&title=" + getQuery() + "&include=covers,description,developers,game_id,genres,moby_score,moby_url,official_url,platforms,publishers,release_date,screenshots,title";
        
        Map<String, Object> additionalFilters = getAdditionalFilters();
        
        if (additionalFilters != null) {
            platform = (MobyGamesPlatform) additionalFilters.get(DcResources.getText("lblPlatform"));
            platform = CoreUtilities.isEmpty(platform.getId()) ? null : platform;
    
            if (platform != null)
                sUrl += "&platform=" + platform.getId();
        }
        
        waitBetweenRequest(); // prevent button smashing
        try {
            URL url = new URI(sUrl).toURL();
            
            HttpConnection connection = HttpConnectionUtil.getConnection(url);
            String result = connection.getString(StandardCharsets.UTF_8);
            
            Map<?, ?> map = gson.fromJson(result, Map.class);
            
            @SuppressWarnings({"unchecked"})
            List<LinkedTreeMap> games = (List<LinkedTreeMap>) map.get("games");
            
            Software item;
            int mobygamesId;
            int platformId;
            int count = 0;
    
            for (LinkedTreeMap game : games) {
                item = new Software();
                
                mobygamesId = ((Double) game.get("game_id")).intValue();
                
                item.addExternalReference(DcRepository.ExternalReferences._MOBYGAMES, String.valueOf(mobygamesId));
                
                setTitle(game, item);
                setUrl(game, item);
                setDescription(game, item);
                setRating(game, item);
                setCategories(game, item);
                
                platformId = setPlatformDetails(game, item, mobygamesId);
                
                if (platformId > -1) {
                    MobyGamesResult mgr = new MobyGamesResult(item, platformId);
                    
                    setCovers(game, mgr);
                    setScreenshots(game, mgr);
                    setPublishers(game, item);
                    setDevelopers(game, item);

                    keys.add(mgr);
                } else {
                    listener.addError("No known platform found for [" + item + "], excluding this result from the search.");
                }
                
                count++;
                
                if (count == getMaximum()) break;
            }
        } catch (Exception e) {
            throw new OnlineServiceError(e);
        }
        
        return keys;
    }
    
    private void setDevelopers(Map<?, ?> game, DcObject item) {
        List developers = (List) game.get("developers");
        
        if (developers == null) return;
        
        LinkedTreeMap developer;
        
        String companyName;
        
        for (Object o : developers) {
            developer = (LinkedTreeMap) o;
            companyName = (String) developer.get("name");
            item.createReference(Software._F_DEVELOPER, companyName);
        }
    }    
    
    private void setPublishers(Map<?, ?> game, DcObject item) {
        List publishers = (List) game.get("publishers");
        
        if (publishers == null) return;
        
        LinkedTreeMap publisher;
        
        String companyName;
        
        for (Object o : publishers) {
            publisher = (LinkedTreeMap) o;
            companyName = (String) publisher.get("name");
            item.createReference(Software._G_PUBLISHER, companyName);
        }
    }    
    
    private void setPictures(MobyGamesResult mgr, DcObject item) {
       DcImageIcon image;
       
       for (String link : mgr.getCovers()) {
           image = CoreUtilities.downloadAndStoreImage(link);
           if (image != null)
               item.addNewPicture(new Picture(item.getID(), image));
       }
        
        for (String link : mgr.getScreenshots()) {
            image = CoreUtilities.downloadAndStoreImage(link);
            if (image != null)
                item.addNewPicture(new Picture(item.getID(), image));
        }
    }
    
    private void setTitle(LinkedTreeMap game, Software item) {
        String s = (String) game.get("title");
        s = "<html><body>" + s + "</body></html>";
        Document doc = Jsoup.parse(s);
        item.setValue(Software._A_TITLE, doc.body().text());
    }

    private void setUrl(LinkedTreeMap game, Software item) {
        String url = (String) game.get("moby_url");
                
        if (!CoreUtilities.isEmpty(url))
            item.setValue(Software._I_WEBPAGE, url);
    }

    @SuppressWarnings("unchecked")
    private void setScreenshots(LinkedTreeMap game, MobyGamesResult result) {
        List<LinkedTreeMap> screenshots = (List<LinkedTreeMap>) game.get("screenshots");        

        if (screenshots == null) return;
        
        List<LinkedTreeMap> images;
        for (LinkedTreeMap screenshot : screenshots) {
            images = (List<LinkedTreeMap>) screenshot.get("images");
            int counter = 0;
            for (LinkedTreeMap image : images) {
                if (counter++ > 10) break;
                
                String link = (String) image.get("image_url");
                link = link.replace("http://", "https://");
                result.addScreenshot(link);
            }
            
            break;
        }
    }
    
    @SuppressWarnings("unchecked")
    private void setCovers(LinkedTreeMap game, MobyGamesResult result) {
        List<LinkedTreeMap> covers = (List<LinkedTreeMap>) game.get("covers");        

        if (covers == null) return;
        
        List<LinkedTreeMap> images;
        for (LinkedTreeMap cover : covers) {
            images = (List<LinkedTreeMap>) cover.get("images");
            int counter = 0;
            for (LinkedTreeMap image : images) {
                if (counter++ > 3) break;
                
                String link = (String) image.get("image_url");
                link = link.replace("http://", "https://");
                result.addCover(link);
            }
            
            break;
        }
    }
    
    @SuppressWarnings("unchecked")
    private void setCategories(LinkedTreeMap game, Software item) {
        List<LinkedTreeMap> genres = (List<LinkedTreeMap>) game.get("genres");

        if (genres != null) {
            for (LinkedTreeMap genre : genres) {
                if (genre.get("category").equals("Basic Genres")) 
                    item.createReference(Software._K_CATEGORIES, genre.get("name"));
            }
        }
    }

    private void setRating(LinkedTreeMap game, Software item) {
        Double d = (Double) game.get("moby_score");
        if (d != null) {
            int rating = d.intValue();
            item.setValue(Software._E_RATING, Integer.valueOf(rating));    
        }
    }
    
    @SuppressWarnings("unchecked")
    private int setPlatformDetails(LinkedTreeMap game, Software item, int mobygamesId) {
        List<LinkedTreeMap> platforms = (List<LinkedTreeMap>) game.get("platforms");
        
        int platformId = 0;
        
        for (LinkedTreeMap platform : platforms) {
            platformId = ((Double) platform.get("platform_id")).intValue();

            String platformName = (String) platform.get("name");
            String releasedOn = (String) platform.get("release_date");
            
            // use the platform as selected from the drop down filter
            if (this.platform != null) {
                platformName = this.platform.getDescription();
            }

            item.createReference(Software._H_PLATFORM, platformName);
            
            String serviceUrl = "https://api.mobygames.com/v2/games/" + mobygamesId 
                    + "/platforms/" + (this.platform != null ? this.platform.getId() : platformId) + "?api_key=" + apiKey;
            
            item.setValue(Software._SYS_SERVICEURL, serviceUrl);
            
            if (releasedOn != null && releasedOn.length() >= 4)
                item.setValue(Software._C_YEAR, releasedOn.substring(0, 4));
            
            break;
        }
        
        return platformId;
    }
    
    private void setDescription(LinkedTreeMap game, Software item) {
        String s = (String) game.get("description");
        if (!CoreUtilities.isEmpty(s)) {
            s = "<html><body>" + s + "</body></html>";
            Document doc = Jsoup.parse(s);
            s = doc.body().text();
            item.setValue(Software._B_DESCRIPTION, s);
        }
    }
}