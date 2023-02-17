package org.datacrow.onlinesearch.itunes;

import java.util.HashMap;
import java.util.Map;

import org.datacrow.core.objects.DcAssociate;

public class ITunesArtistCache {
    
    private final Map<String, DcAssociate> artists = new HashMap<>();
    
    public ITunesArtistCache() {}
    
    public void addArtist(DcAssociate person, String id) {
        artists.put(id, person);
    }

    public boolean contains(String id) {
        return artists.containsKey(id);
    }
    
    public DcAssociate getArtist(String id) {
        return artists.get(id);
    }
}
