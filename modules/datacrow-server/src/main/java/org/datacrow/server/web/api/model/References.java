package org.datacrow.server.web.api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class References {
	
	@JsonProperty("moduleIdx")
	private final int moduleIdx;
	@JsonProperty("items")
	private final List<Reference> items;
	
	public References(int moduleIdx, List<Reference> items) {
		this.moduleIdx = moduleIdx;
		this.items = items;
	}
	
	public int getModuleIdx() {
		return moduleIdx;
	}
	
	public List<Reference> getItems() {
		return items;
	}
}
