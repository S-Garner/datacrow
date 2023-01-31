package org.datacrow.onlinesearch.itunes.helpers;

import org.datacrow.core.objects.DcObject;

public class ITunesSearchResult {
    
    private DcObject dco;
    private String id;
    private String coverUrl;
    
    public ITunesSearchResult(DcObject dco, String id) {
        this.dco = dco;
        this.id = id;
    }
    
    public DcObject getDco() {
        return dco;
    }
    
    public String getId() {
        return id;
    }
    
    public void setCoverUrl(String url) {
        this.coverUrl = url;
    }
    
    public String getCoverUrl() {
        return coverUrl;
    }
}
