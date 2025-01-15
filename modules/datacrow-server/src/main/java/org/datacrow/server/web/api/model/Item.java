package org.datacrow.server.web.api.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.datacrow.core.DcConfig;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.utilities.Base64;
import org.datacrow.server.web.api.manager.ModuleManager;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Item {

	@JsonProperty("id")
	private String id;
	@JsonProperty("moduleIdx")
	private int moduleIdx;
	@JsonProperty("name")
	private String name;
	@JsonProperty("scaledImageUrl")
	private final String scaledImageUrl;
	@JsonProperty("imageUrl")
	private final String imageUrl;
	@JsonProperty("icon")
	private String icon;
	
	@JsonProperty("fields")
	private final List<FieldValue> fields = new LinkedList<FieldValue>();
	
	public Item(DcObject src, int[] fields) {

		id = src.getID();
		name = src.toString();
		moduleIdx = src.getModuleIdx();
		
		if (new File(new File(DcConfig.getInstance().getImageDir(), id), "picture1.jpg").exists()) {
			scaledImageUrl = src.getScaledImageUrl();
			imageUrl = src.getImageUrl();
		} else {
			scaledImageUrl = null;
			imageUrl = null;
		}
		
		if (src.getIcon() != null)
			icon = String.valueOf(Base64.encode(src.getIcon().getBytes()));
		
		Module m = ModuleManager.getInstance().getModule(src.getModuleIdx());
		DcModule module = DcModules.get(m.getIndex());
		
		Field field;
		
		for (int fieldIdx : fields) {
			field = m.getField(fieldIdx);
			
			if (module.getField(fieldIdx).isEnabled())
				this.fields.add(new FieldValue(field, toValidValue(field, src.getValue(fieldIdx))));
		}
	}
	
	@SuppressWarnings("unchecked")
	private Object toValidValue(Field field, Object o) {
		Object value = o;
		
		if (o instanceof DcObject)
			value = ((DcObject) o).getID();
		else if (field.getType() == Field._MULTIRELATE ||
				field.getType() == Field._TAGFIELD) {
			Collection<String> c = new ArrayList<String>();
			
			if (o != null) {
				for (DcMapping item : (Collection<DcMapping>) o)
					c.add(item.getReferencedID());
			}
			
			value = c;
			
		} else if (o instanceof DcModule || o instanceof Collection)
			value = "";
		else if (o instanceof Date)
			value = o.toString();
		
		return value;
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
	
	public String getIcon() {
		return icon;
	}
	
	public String getName() {
		return name;
	}
	
	public int getModuleIdx() {
		return moduleIdx;
	}	

	public List<FieldValue> getFields() {
		return fields;
	}
}