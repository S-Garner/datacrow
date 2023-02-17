package org.datacrow.onlinesearch.discogs;

import org.datacrow.core.objects.DcObject;

public class DiscogsSearchResult {

	private DcObject dco;
    private String coverUrl;
    private String detailsUrl;
    
    public DiscogsSearchResult(DcObject dco) {
        this.dco = dco;
    }
    
    public DcObject getDco() {
        return dco;
    }
    
    public void setDetailsUrl(String url) {
        this.detailsUrl = url;
    }
    
    public String getDetailsUrl() {
        return detailsUrl;
    }
    
    public void setCoverUrl(String url) {
        this.coverUrl = url;
    }
    
    public String getCoverUrl() {
        return coverUrl;
    }
}
