package org.datacrow.core.migration;

import org.datacrow.core.DcRepository.ValueTypes;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.utilities.Converter;

public final class XmlUtilities {

	public static String getFieldTag(DcField field) {
		String tag = field.getSystemName();

		if (field.getValueType() == ValueTypes._DCOBJECTCOLLECTION)
			tag = getElementTagForList(field);

		return Converter.getValidXmlTag(tag);
	}
	
	public static String getElementTagForList(DcField field) {
		return getElementTagForList(DcModules.getReferencedModule(field));
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
	
	public static String getElementTagForList(DcModule m) {
		return Converter.getValidXmlTag(m.getTableName().replaceAll("\\_", "-") + "-items");
	}

	public static String getElementTagTypeForList(DcModule m) {
		return Converter.getValidXmlTag(m.getTableName().replaceAll("\\_", "-") + "-items-type");
	}	

	public static String getElementTag(DcModule m) {
		return Converter.getValidXmlTag(m.getTableName().replaceAll("\\_", "-"));
	}

	public static String getElementTagType(DcModule m) {
		return Converter.getValidXmlTag(m.getTableName().replaceAll("\\_", "-") + "-type");
	}	
}
