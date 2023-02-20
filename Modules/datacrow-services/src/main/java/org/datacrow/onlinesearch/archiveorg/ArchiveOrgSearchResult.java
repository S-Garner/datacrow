package org.datacrow.onlinesearch.archiveorg;

import org.datacrow.core.objects.DcObject;

public class ArchiveOrgSearchResult {

	private DcObject dco;
    private String id;
    
    public ArchiveOrgSearchResult(DcObject dco) {
        this.dco = dco;
    }
    
    public void setId(String id) {
    	this.id = id;
    }
    
    public String getId() {
    	return id;
    }
    
    public DcObject getDco() {
        return dco;
    }
}
