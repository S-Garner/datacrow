package org.datacrow.server.web.api.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.datacrow.core.DcConfig;
import org.datacrow.core.objects.DcObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Item {

	@JsonProperty("id")
	private String id;
	@JsonProperty("name")
	private String name;
	@JsonProperty("scaledImageUrl")
	private final String scaledImageUrl;
	@JsonProperty("imageUrl")
	private final String imageUrl;
	
	@JsonProperty("fields")
	private final List<FieldValue> fields = new LinkedList<FieldValue>();
	
	public Item(DcObject src, int[] fields) {

		id = src.getID();
		name = src.toString();
		
		if (new File(new File(DcConfig.getInstance().getImageDir(), id), "picture1.jpg").exists()) {
			scaledImageUrl = src.getScaledImageUrl();
			imageUrl = src.getImageUrl();
		} else {
			scaledImageUrl = null;
			imageUrl = null;
		}
		
		
		Module m = Modules.getInstance().getModule(src.getModuleIdx());
		Field field;
		
		for (int fieldIdx : fields) {
			field = m.getField(fieldIdx);
			this.fields.add(new FieldValue(field, src.getValue(fieldIdx)));
		}
	}
	public String getImageUrl() {
		return imageUrl;
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