package org.datacrow.synch.helpers;

import org.datacrow.core.objects.DcField;

public class SimpleField {

	private int index;
	private String name;
	private String columnName;
	private int valueType;
	private int maxLength;
	
	public SimpleField(DcField f) {
		this(f.getIndex(),
			 f.getLabel(), 
			 f.getDatabaseFieldName(), 
			 f.getValueType(), 
			 f.getMaximumLength());
	}
	
	public SimpleField(
			int index,
			String name,
			String columnName,
			int valueType,
			int maxLength) {
		
		this.index = index;
		this.name = name;
		this.columnName = columnName;
		this.valueType = valueType;
		this.maxLength = maxLength;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public String getColumnName() {
		return columnName;
	}

	public int getValueType() {
		return valueType;
	}

	public int getMaxLength() {
		return maxLength;
	}
}
