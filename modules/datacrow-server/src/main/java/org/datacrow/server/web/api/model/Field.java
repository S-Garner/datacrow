package org.datacrow.server.web.api.model;

import org.datacrow.core.DcRepository;
import org.datacrow.core.console.UIComponents;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Field {
	
    public static final int _CHECKBOX = 0;
    public static final int _TEXTFIELD = 1;
    public static final int _LONGFIELD = 2;
    public static final int _DROPDOWN = 3;
    public static final int _URLFIELD = 4;
    public static final int _MULTIRELATE = 5;
    public static final int _DATE = 6;
    public static final int _FILE = 7;
    public static final int _TAGFIELD = 8;
    public static final int _RATING = 9;
    public static final int _ICON = 10;
    public static final int _NUMBER = 11;
    public static final int _DOUBLE = 12;
    public static final int _DURATION = 13;
	
    @JsonProperty("type")
	private int type;
    @JsonProperty("index")
    private final int index;
    @JsonProperty("moduleIdx")
    private final int moduleIdx;
    @JsonProperty("referencedModuleIdx")
    private final int referencedModuleIdx;
    @JsonProperty("maximumLength")
    private final int maximumLength;
    @JsonProperty("label")
    private final String label;
    @JsonProperty("readOnly")
    private final boolean readOnly;
    @JsonProperty("required")
    private final boolean required;
    
	public Field(DcField src) {
		index = src.getIndex();
		moduleIdx = src.getModule();
		referencedModuleIdx = src.getReferenceIdx();
		maximumLength = src.getMaximumLength();
		label = src.getResourceKey();
		readOnly = src.isReadOnly();
		required  = src.isRequired();
		setType(src);
	}
	
	public int getIndex() {
		return index;
	}
	
	public int getType() {
		return type;
	}

	public int getMaximumLength() {
		return maximumLength;
	}

	public int getReferencedModuleIdx() {
		return referencedModuleIdx;
	}
	
	public int getModuleIdx() {
		return moduleIdx;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	
	public boolean isRequired() {
		return required;
	}	
	
	private void setType(DcField src) {
		if (src.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
	        type = _DROPDOWN;
	    } else  if (src.getFieldType() == UIComponents._RATINGCOMBOBOX) {
	        type = _RATING;
	    } else  if (src.getFieldType() == UIComponents._TIMEFIELD) {
	        type = _DURATION;            
	    } else if (src.getValueType() == DcRepository.ValueTypes._BIGINTEGER ||
	               src.getValueType() == DcRepository.ValueTypes._LONG ||
	               src.getFieldType() == UIComponents._NUMBERFIELD) {
	        type = _NUMBER;
	    } else if (src.getValueType() == DcRepository.ValueTypes._DOUBLE) {
	        type = _DOUBLE;    
	    } else if (src.getValueType() == DcRepository.ValueTypes._ICON) {
	        type = _ICON;            
	    } else if (src.getValueType() == DcRepository.ValueTypes._BOOLEAN) {
	        type = _CHECKBOX;
	    } else if (src.getFieldType() == UIComponents._LONGTEXTFIELD) {
	        type = _LONGFIELD;
	    } else if (src.getFieldType() == UIComponents._URLFIELD) {
	        type = _URLFIELD;
	    } else if (src.getFieldType() == UIComponents._TAGFIELD) {
	        type = _TAGFIELD;
	    } else if (src.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
	        type = _MULTIRELATE;
	    } else if ((src.getValueType() == DcRepository.ValueTypes._DATE ||
	                src.getValueType() == DcRepository.ValueTypes._DATETIME) &&
	               src.getIndex() != DcObject._SYS_LOANDUEDATE) {
	        type = _DATE;
	    } else if (src.getFieldType() == UIComponents._FILEFIELD ||
	               src.getFieldType() == UIComponents._FILELAUNCHFIELD) {
	        type = _FILE;
	    } else {
	        type = _TEXTFIELD;
	    } 		
	}
}
