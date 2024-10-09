package org.datacrow.server.web.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Module {

	@JsonProperty("index")
	private final int index;
	@JsonProperty("name")
	private final String name;
	
	public Module(int index, String name) {
		this.index = index;
		this.name = name;
	}
	
	public int getIndex() {
		return index;
	}
	
	public String getName() {
		return name;
	}
}
