package org.datacrow.server.web.api.model;

import org.datacrow.core.utilities.definitions.WebOverviewFieldDefinition;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OverviewFieldSetting {
	
	@JsonProperty("id")
	private int id;
	@JsonProperty("enabled")
	private boolean enabled;
	@JsonProperty("fieldIdx")
	private int fieldIdx;
	@JsonProperty("labelKey")
	private String labelKey;
	@JsonProperty("order")
	private int order;
	
	public OverviewFieldSetting() {}
	
	public OverviewFieldSetting(WebOverviewFieldDefinition wf, int order) {
		this.id = wf.getFieldIdx();
		this.fieldIdx = wf.getFieldIdx();
		this.labelKey = wf.getLabelKey();
		this.enabled = wf.isEnabled();
		
		this.order = order;
	}

	public int getId() {
		return id;
	}
	
	public int getFieldIdx() {
		return fieldIdx;
	}
	
	public String getLabelKey() {
		return labelKey;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public int getOrder() {
		return order;
	}
}
