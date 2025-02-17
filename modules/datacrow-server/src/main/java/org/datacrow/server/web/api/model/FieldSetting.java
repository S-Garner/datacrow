package org.datacrow.server.web.api.model;

import org.datacrow.core.utilities.definitions.WebFieldDefinition;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldSetting {

	@JsonProperty("enabled")
	private final boolean enabled;
	@JsonProperty("fieldIdx")
	private final int fieldIdx;
	@JsonProperty("labelKey")
	private final String labelKey;
	
	public FieldSetting(WebFieldDefinition wf) {
		this.fieldIdx = wf.getFieldIdx();
		this.labelKey = wf.getLabelKey();
		this.enabled = wf.isEnabled();
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
}
