package org.datacrow.server.web.api.model;

import org.datacrow.core.utilities.definitions.WebFieldDefinition;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldSetting {

	@JsonProperty("enabled")
	private boolean enabled;
	@JsonProperty("fieldIdx")
	private int fieldIdx;
	@JsonProperty("labelKey")
	private String labelKey;
	@JsonProperty("order")
	private int order;
	
	public FieldSetting() {}
	
	public FieldSetting(WebFieldDefinition wf, int order) {
		this.fieldIdx = wf.getFieldIdx();
		this.labelKey = wf.getLabelKey();
		this.enabled = wf.isEnabled();
		this.order = order;
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
