package org.datacrow.synch.helpers;

import java.util.ArrayList;
import java.util.Collection;

import org.datacrow.core.modules.DcModule;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcImageIcon;

public class SimpleModule {
	
	private int index;
	private String name;
	private DcImageIcon icon;
	
	private Collection<SimpleField> fields = new ArrayList<>();

	public SimpleModule(DcModule m) {
		this(m.getIndex(), m.getName(), m.getIcon32());
		
		for (DcField f : m.getFields()) {
			fields.add(new SimpleField(f));
		}
	}
	
	public SimpleModule(
			int index,
			String name,
			DcImageIcon icon) {
		
		this.index = index;
		this.name = name;
		this.icon = icon;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public DcImageIcon getIcon() {
		return icon;
	}
	
	public Collection<SimpleField> getFields() {
		return fields;
	}
}