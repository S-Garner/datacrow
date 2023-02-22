package org.datacrow.onlinesearch.openlibrary;

import java.util.Collection;

import org.datacrow.core.objects.DcObject;

public class OpenLibrarySearchResult {

	private DcObject dco;
    private String workId;
    private Collection<String> editions;
    
    public OpenLibrarySearchResult(DcObject dco) {
        this.dco = dco;
    }
    
	public String getWorkId() {
		return workId;
	}

	public void setWorkId(String workId) {
		this.workId = workId;
	}
	
	public void addEdition(String editionId) {
		editions.add(editionId);
	}

	public DcObject getDco() {
        return dco;
    }
}
