package org.datacrow.onlinesearch.discogs;

import java.util.HashMap;
import java.util.Map;

import org.datacrow.core.objects.DcAssociate;

public class DiscogsArtistCache {
    
    private final Map<String, DcAssociate> artists = new HashMap<>();
    
    public DiscogsArtistCache() {}
    
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
