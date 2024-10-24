package org.datacrow.server.web.api.model;

import java.util.LinkedList;
import java.util.List;

import org.datacrow.core.objects.DcObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Item {

	@JsonProperty("fields")
	private final List<FieldValue> fields = new LinkedList<FieldValue>();
	
	public Item(DcObject src, int[] fields) {
		
		Module m = Modules.getInstance().getModule(src.getModuleIdx());
		Field field;
		
		for (int fieldIdx : fields) {
			field = m.getField(fieldIdx);
			this.fields.add(new FieldValue(field, src.getValue(fieldIdx)));
		}
	}
}