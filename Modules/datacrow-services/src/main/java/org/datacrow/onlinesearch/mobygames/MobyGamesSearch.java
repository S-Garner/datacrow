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

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacrow.core.DcRepository;
import org.datacrow.core.http.HttpConnection;
import org.datacrow.core.http.HttpConnectionUtil;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Software;
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
        
        attributes.put("Minimum RAM Required", DcResources.getText("lblMobyGamesAttribRAM"));
        attributes.put("Minimum CPU Class Required", DcResources.getText("lblMobyGamesAttribMinCPU"));
        attributes.put("Minimum OS Class Required", DcResources.getText("lblMobyGamesAttribMinOS"));
        attributes.put("Video Resolutions Supported", DcResources.getText("lblMobyGamesAttribVidRes"));
        attributes.put("Video Modes Supported", DcResources.getText("lblMobyGamesAttribVidModes"));
        attributes.put("Minimum DirectX Version Required", DcResources.getText("lblMobyGamesAttribMinDirectX"));
        attributes.put("Input Devices Required", DcResources.getText("lblMobyGamesAttribInputDevices"));
        attributes.put("Supported Systems/Models", DcResources.getText("lblMobyGamesAttribSupportedSystems"));
        attributes.put("Minimum Video Memory Required", DcResources.getText("lblMobyGamesAttribMinVidMem"));
        attributes.put("Multiplayer Options", DcResources.getText("lblMobyGamesAttribMultiplayerOptions"));
        attributes.put("Number of Online Players", DcResources.getText("lblMobyGamesAttribNrOfOnlinePlayer"));
        attributes.put("Number of Offline Players", DcResources.getText("lblMobyGamesAttribNrOfOfflinePlayer"));
        attributes.put("Multiplayer Game Modes", DcResources.getText("lblMobyGamesAttribMultiplayerGameModes"));
        attributes.put("Save Game Methods", DcResources.getText("lblMobyGamesAttribSaveGameMethods"));
        
        apiKey = DcSettings.getString(DcRepository.Settings.stMobyGamesApiKey);
        
        if (apiKey != null)
            apiKey = apiKey.replace("+", "%2B");
    }

    @Override
    protected DcObject getItem(Object key, boolean full) throws Exception {
        
        MobyGamesResult mgr = (MobyGamesResult) key;
        DcObject item = mgr.getDco();
        
        String serviceUrl = (String) item.getValue(Software._SYS_SERVICEURL);
        
        waitBetweenRequest();
        
        URL url = new URL(serviceUrl);
        HttpConnection connection = HttpConnectionUtil.getConnection(url);
        String result = connection.getString(StandardCharsets.UTF_8);
        
        Map<?, ?> game = gson.fromJson(result, Map.class);
        
        setAttributes(game, item);
        setCompanies(game, item);
        setCountries(game, item);
        
        setPictures(mgr, item);
        setServiceInfo(item);
        
        extendDescription(game, item);
        
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
            msg = "<html>" + msg + "<br><u><a href=\"https://www.mobygames.com/info/api\">https://www.mobygames.com/info/api</a></u></html>";
            
            throw new OnlineSearchUserError(msg);
        } else {
            return "https://api.mobygames.com/v1/games?api_key=" + apiKey;
        }
    }
    
    @Override
    protected Collection<Object> getItemKeys() throws OnlineServiceError, OnlineSearchUserError {
        Collection<Object> keys = new ArrayList<Object>();
        
        String sUrl = getBaseUrl();
        sUrl += "&title=" + getQuery();
        
        Map<String, Object> additionalFilters = getAdditionalFilters();
        
        if (additionalFilters != null) {
            platform = (MobyGamesPlatform) additionalFilters.get(DcResources.getText("lblPlatform"));
            platform = CoreUtilities.isEmpty(platform.getId()) ? null : platform;
    
            if (platform != null)
                sUrl += "&platform=" + platform.getId();
        }
        
        waitBetweenRequest(); // prevent button smashing
        try {
            URL url = new URL(sUrl);
            
            HttpConnection connection = HttpConnectionUtil.getConnection(url);
            String result = connection.getString(StandardCharsets.UTF_8);
            
            Map<?, ?> map = gson.fromJson(result, Map.class);
            
            @SuppressWarnings({"unchecked"})
            List<LinkedTreeMap> games = (List<LinkedTreeMap>) map.get("games");
            
            Software item;
            int mobygamesId;
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
                setPlatformDetails(game, item, mobygamesId);
                
                MobyGamesResult mgr = new MobyGamesResult(item);
                setScreenshots(game, mgr);
                setPictureFront(game, mgr);
                
                keys.add(mgr);
                
                count++;
                
                if (count == getMaximum()) break;
            }
        } catch (Exception e) {
            throw new OnlineServiceError(e);
        }
        
        return keys;
    }
    
    private void setAttributes(Map<?, ?> game, DcObject item) {
        
        List attributes = (List) game.get("attributes");
        
        if (attributes == null) return;
        
        LinkedTreeMap attribute;
        for (Object o : attributes) {
            attribute = (LinkedTreeMap) o;
            
            // multiplayer stuff
            if (attribute.get("attribute_category_name").equals("Multiplayer Game Modes")) {
                item.setValue(Software._AB_MULTI, Boolean.TRUE);

                if (attribute.get("attribute_name").equals("Team"))
                    item.setValue(Software._AA_COOP, Boolean.TRUE);
            }

            // storage medium
            if (attribute.get("attribute_category_name").equals("Media Type")) {
                item.createReference(Software._W_STORAGEMEDIUM, attribute.get("attribute_name"));
            }
        }
    }
    
    private static final Map<String, String> attributes = new HashMap<>();
    
    private void extendDescription(Map<?, ?> game, DcObject item) {
        
        List attributes = (List) game.get("attributes");
        
        if (attributes == null) return;
        
        String attributeName;
        String attributeValue;
        
        HashMap<String, List<String>> properties = new HashMap<>();
        properties.put("Minimum RAM Required", new ArrayList<String>());
        properties.put("Minimum CPU Class Required", new ArrayList<String>());
        properties.put("Minimum OS Class Required", new ArrayList<String>());
        properties.put("Video Resolutions Supported", new ArrayList<String>());
        properties.put("Video Modes Supported", new ArrayList<String>());
        properties.put("Minimum DirectX Version Required", new ArrayList<String>());
        properties.put("Input Devices Required", new ArrayList<String>());
        properties.put("Supported Systems/Models", new ArrayList<String>());
        properties.put("Minimum Video Memory Required", new ArrayList<String>());
        
        properties.put("Multiplayer Options", new ArrayList<String>());
        properties.put("Number of Online Players", new ArrayList<String>());
        properties.put("Number of Offline Players", new ArrayList<String>());
        properties.put("Multiplayer Game Modes", new ArrayList<String>());

        properties.put("Save Game Methods", new ArrayList<String>());

        LinkedTreeMap attribute;
        for (Object o : attributes) {
            attribute = (LinkedTreeMap) o;
            
            attributeName = (String) attribute.get("attribute_category_name");
            attributeValue = (String) attribute.get("attribute_name");
            
            if (properties.containsKey(attributeName))
                properties.get(attributeName).add(attributeValue);
        }
        
        String description = (String) item.getValue(Software._B_DESCRIPTION);
        
        String part = createSection(
                DcResources.getText("lblTechnicalInfo"), 
                properties, new String[] {
                        "Minimum RAM Required", 
                        "Minimum CPU Class Required",
                        "Minimum OS Class Required",
                        "Video Resolutions Supported",
                        "Video Modes Supported",
                        "Minimum DirectX Version Required",
                        "Input Devices Required",
                        "Supported Systems/Models",
                        "Minimum Video Memory Required"});
        
        if (part.trim().length() > 0)
            description += "\r\n\r\n" + part;
        
        part = createSection(
                DcResources.getText("lblMultiplayer"), 
                properties, new String[] {
                        "Multiplayer Options", 
                        "Number of Online Players",
                        "Number of Offline Players",
                        "Multiplayer Game Modes"});
        
        if (part.trim().length() > 0)
            description += "\r\n\r\n" + part;        
        
        item.setValue(Software._B_DESCRIPTION, description);
    }
    
    private String createSection(String title, HashMap<String, List<String>> values, String[] selection) {
        String section = "";
        
        int groupCount = 0;
        int valueCount = 0;
        for (String key : selection) {
            
            valueCount = 0;
            
            if (values.containsKey(key) && values.get(key).size() > 0) {
            
                if (groupCount == 0) {
                    section += title + ":\r\n";
                }
                
                if (groupCount > 0) section += " / ";
                    
                section += attributes.get(key) + ": ";
                    
                for (String value : values.get(key)) {
                    if (valueCount > 0) section += ", ";
                    section += value.replace(" / ", ", ");
                    valueCount++;
                }
                
                groupCount++;
            }
        }

        return section;
    }
    
    
    private void setCountries(Map<?, ?> game, DcObject item) {
        List releases = (List) game.get("releases");
        
        if (releases == null) return;
        
        LinkedTreeMap release;
        
        List<String> countries = new ArrayList<>();
        
        for (Object o : releases) {
            release = (LinkedTreeMap) o;
            @SuppressWarnings("unchecked")
            List<String> c = (List<String>) release.get("countries");
            
            if (c != null) {
                for (String s : c) {
                    if (!countries.contains(s)) {
                        countries.add(s);
                    }
                }
            }
        }

        for (String country : countries) 
            item.createReference(Software._F_COUNTRY, country);
    }
    
    private void setCompanies(Map<?, ?> game, DcObject item) {
        
        List releases = (List) game.get("releases");
        
        if (releases == null) return;
        
        LinkedTreeMap release;
        
        String role;
        String companyName;
        
        List<String> developers = new ArrayList<>();
        List<String> publishers = new ArrayList<>();
        
        for (Object o : releases) {
            release = (LinkedTreeMap) o;
            
            @SuppressWarnings("unchecked")
            List<LinkedTreeMap> companies = (List<LinkedTreeMap>) release.get("companies");
            
            for (LinkedTreeMap company : companies) { 
                role = (String) company.get("role");
                companyName = (String) company.get("company_name");
                
                if (role.equals("Developed by") && !developers.contains(companyName))
                    developers.add(companyName);

                if (role.equals("Published by") && !publishers.contains(companyName))
                    publishers.add(companyName);
            }
        }
        
        for (String developer : developers)
            item.createReference(Software._F_DEVELOPER, developer);
        
        for (String publisher : publishers)
            item.createReference(Software._G_PUBLISHER, publisher);
    }    
    
    private void setPictures(MobyGamesResult mgr, DcObject item) {
        int[] fields = new int[] {Software._P_SCREENSHOTONE, Software._Q_SCREENSHOTTWO, Software._R_SCREENSHOTTHREE};
        int fieldIdx = 0;
        
        DcImageIcon image;
        for (String link : mgr.getScreenshotLinks()) {
            image = CoreUtilities.getImage(link);
            if (image != null)
                item.setValue(fields[fieldIdx++], image);
            
            if (fieldIdx > 2) break;
        }
        
        if (!CoreUtilities.isEmpty(mgr.getCover())) {
	        image = CoreUtilities.getImage(mgr.getCover());
	        if (image != null)
	            item.setValue(Software._M_PICTUREFRONT, image);
        }
    }
    
    private void setTitle(LinkedTreeMap game, Software item) {
        item.setValue(Software._A_TITLE, game.get("title"));
    }

    private void setUrl(LinkedTreeMap game, Software item) {
        String url = (String) game.get("moby_url");
                
        if (!CoreUtilities.isEmpty(url))
            item.setValue(Software._I_WEBPAGE, url);
    }

    @SuppressWarnings("unchecked")
    private void setScreenshots(LinkedTreeMap game, MobyGamesResult result) {
        List<LinkedTreeMap> screenshots = (List<LinkedTreeMap>) game.get("sample_screenshots");        

        if (screenshots == null) return;
        
        for (LinkedTreeMap screenshot : screenshots) {
            String link = (String) screenshot.get("image");
            link = link.replace("http://", "https://");
            result.addScreenshot(link);
        }
    }
    

    private void setPictureFront(LinkedTreeMap game, MobyGamesResult result) {
        LinkedTreeMap pic = (LinkedTreeMap) game.get("sample_cover");
                
        if (!CoreUtilities.isEmpty(pic)) {
            String link = (String) pic.get("image");
            result.addCover(link);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void setCategories(LinkedTreeMap game, Software item) {
        List<LinkedTreeMap> genres = (List<LinkedTreeMap>) game.get("genres");

        if (genres != null) {
            for (LinkedTreeMap genre : genres) {
                if (genre.get("genre_category").equals("Basic Genres")) 
                    item.createReference(Software._K_CATEGORIES, genre.get("genre_name"));
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
    private void setPlatformDetails(LinkedTreeMap game, Software item, int mobygamesId) {
        List<LinkedTreeMap> platforms = (List<LinkedTreeMap>) game.get("platforms");
        
        if (platforms == null)
            return;
        
        for (LinkedTreeMap platform : platforms) {
            int platformId = ((Double) platform.get("platform_id")).intValue();

            String platformName = (String) platform.get("platform_name");
            String releasedOn = (String) platform.get("first_release_date");
            
            // use the platform as selected from the drop down filter
            if (this.platform != null) {
                platformName = this.platform.getDescription();
            }

            item.createReference(Software._H_PLATFORM, platformName);
            
            String serviceUrl = "https://api.mobygames.com/v1/games/" + mobygamesId 
                    + "/platforms/" + (this.platform != null ? this.platform.getId() : platformId) + "?api_key=" + apiKey;
            
            item.setValue(Software._SYS_SERVICEURL, serviceUrl);
            
            if (releasedOn != null && releasedOn.length() >= 4)
                item.setValue(Software._C_YEAR, releasedOn.substring(0, 4));
            
            break;
        }
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