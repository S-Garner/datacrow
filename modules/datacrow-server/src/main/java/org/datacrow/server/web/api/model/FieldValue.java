package org.datacrow.server.web.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldValue {

	@JsonProperty("field")
	private final Field field;
	@JsonProperty("value")
	private Object value;
	
	public FieldValue(Field field, Object value) {
		this.value = value;
		this.field = field;
	}

	public Field getField() {
		return field;
	}
	
	public Object getValue() {
		return value;
	}
}
