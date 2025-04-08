package org.datacrow.core.migration;

import org.datacrow.core.DcRepository.ValueTypes;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.utilities.Converter;

public final class XmlUtilities {

	public static String getFieldTag(DcField field) {
		String tag = field.getOriginalLabel();

		if (field.getValueType() == ValueTypes._DCOBJECTCOLLECTION || field.getValueType() == ValueTypes._DCOBJECTREFERENCE)
			tag = getElementTagForList(field);

		return Converter.getValidXmlTag(tag);
	}
	
	public static String getElementTagForList(DcField field) {
		return  field.getOriginalLabel() + "-items";
	}

	public static String getElementTagTypeForList(DcField field) {
		return getElementTagTypeForList(DcModules.getReferencedModule(field));
	}	

	public static String getElementTag(DcField field) {
		return getElementTag(DcModules.getReferencedModule(field));
	}

	public static String getElementTagType(DcField field) {
		return getElementTagType(DcModules.getReferencedModule(field));
	}
	
	public static String getElementNameForModule(DcModule m) {
		return Converter.getValidXmlTag(getName(m) + "-items");
	}

	public static String getElementTagTypeForList(DcModule m) {
		return Converter.getValidXmlTag(getName(m) + "-items-type");
	}	

	public static String getElementTag(DcModule m) {
		return Converter.getValidXmlTag(getName(m));
	}

	public static String getElementTagType(DcModule m) {
		return Converter.getValidXmlTag(getName(m) + "-type");
	}
	
	private static String getName(DcModule m) {
		return Converter.getValidXmlTag(m.getSystemObjectName());
	}
}
