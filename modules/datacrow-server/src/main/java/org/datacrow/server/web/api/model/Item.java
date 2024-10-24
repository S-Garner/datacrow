package org.datacrow.server.web.api.model;

import java.util.LinkedList;
import java.util.List;

import org.datacrow.core.objects.DcObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Item {

	@JsonProperty("id")
	private String id;
	@JsonProperty("name")
	private String name;
	@JsonProperty("scaledImageUrl")
	private String scaledImageUrl;

	@JsonProperty("fields")
	private final List<FieldValue> fields = new LinkedList<FieldValue>();
	
	public Item(DcObject src, int[] fields) {

		id = src.getID();
		name = src.toString();
		scaledImageUrl = src.getScaledImageUrl();
		
		Module m = Modules.getInstance().getModule(src.getModuleIdx());
		Field field;
		
		for (int fieldIdx : fields) {
			field = m.getField(fieldIdx);
			this.fields.add(new FieldValue(field, src.getValue(fieldIdx)));
		}
	}
	
	public String getScaledImageUrl() {
		return scaledImageUrl;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public List<FieldValue> getFields() {
		return fields;
	}
}