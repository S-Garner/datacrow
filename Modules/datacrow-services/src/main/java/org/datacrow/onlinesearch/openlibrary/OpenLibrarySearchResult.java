package org.datacrow.onlinesearch.openlibrary;

import org.datacrow.core.objects.DcObject;

public class OpenLibrarySearchResult {

	private DcObject dco;
    
    private String edition;
    private String work;
    
    public OpenLibrarySearchResult(DcObject dco) {
        this.dco = dco;
    }
    
    public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public String getWork() {
		return work;
	}

	public void setWork(String work) {
		this.work = work;
	}

	public DcObject getDco() {
        return dco;
    }
}
