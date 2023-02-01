package org.datacrow.api.rest.json;

import org.datacrow.core.objects.DcField;

public class JsonField {

    private final int moduleIndex;
    private final int index;

    private final int fieldType;
    private final int valueType;
    private final int maxValueLength;
    
    private final String columnName;
    private final String label;
    
    public JsonField(DcField field) {
        this.moduleIndex = field.getModule();
        this.index  = field.getIndex();
        this.fieldType = field.getFieldType();
        this.valueType = field.getValueType();
        this.label = field.getLabel();
        this.columnName = field.getDatabaseFieldName();
        this.maxValueLength = field.getMaximumLength();
    }
    
    public JsonField(int index, int moduleIndex, int fieldType, int valueType, int maxValueLength, String label, String columnName) {
        this.moduleIndex = moduleIndex;
        this.index  = index;
        this.fieldType = fieldType;
        this.valueType = valueType;
        this.label = label;
        this.columnName = columnName;
        this.maxValueLength = maxValueLength;
    }
    
    public int getModuleIndex() {
        return moduleIndex;
    }

    public int getIndex() {
        return index;
    }

    public int getFieldType() {
        return fieldType;
    }

    public int getValueType() {
        return valueType;
    }

    public int getMaxValueLength() {
        return maxValueLength;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getLabel() {
        return label;
    }    
}
