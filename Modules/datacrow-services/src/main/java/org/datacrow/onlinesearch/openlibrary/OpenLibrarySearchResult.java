package org.datacrow.onlinesearch.openlibrary;

import java.util.Map;

import org.datacrow.core.objects.DcObject;

public class OpenLibrarySearchResult {

	private DcObject dco;
    private String workId;
    private String editionId;
    private String mainCoverId;
    private Map<?, ?> editionData;
    private Map<?, ?> workData;
    
    public Map<?, ?> getEditionData() {
		return editionData;
	}

	public void setEditionData(Map<?, ?> editionData) {
		this.editionData = editionData;
	}

	public void setWorkData(Map<?, ?> workData) {
		this.workData = workData;
	}
	
	public Map<?, ?> getWorkData() {
		return workData;
	}

	public String getEditionId() {
		return editionId;
	}

	public void setEditionId(String editionId) {
		this.editionId = editionId;
	}

	public OpenLibrarySearchResult(DcObject dco) {
        this.dco = dco;
    }
    
	public String getWorkId() {
		return workId;
	}

	public void setWorkId(String workId) {
		this.workId = workId;
	}

	public String getMainCoverId() {
		return mainCoverId;
	}

	public void setMainCoverId(String mainCoverId) {
		this.mainCoverId = mainCoverId;
	}

	public DcObject getDco() {
        return dco;
    }
}
