package org.datacrow.onlinesearch.itunes.helpers;

import java.util.HashMap;
import java.util.Map;

import org.datacrow.core.objects.DcAssociate;

public class ITunesArtistCache {
    
    private final Map<String, DcAssociate> artists = new HashMap<>();
    
    public ITunesArtistCache() {}
    
    public void addArtist(DcAssociate person, String discogsId) {
        artists.put(discogsId, person);
    }

    public boolean contains(String discogsid) {
        return artists.containsKey(discogsid);
    }
    
    public DcAssociate getArtist(String discogsId) {
        return artists.get(discogsId);
    }
}
