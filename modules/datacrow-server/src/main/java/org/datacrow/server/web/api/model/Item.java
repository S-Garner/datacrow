package org.datacrow.server.web.api.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.utilities.Base64;
import org.datacrow.core.utilities.definitions.WebFieldDefinition;
import org.datacrow.core.utilities.definitions.WebFieldDefinitions;
import org.datacrow.server.data.PictureManager;
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
	@JsonProperty("pictures")
	private final List<Picture> pictures = new LinkedList<Picture>();
	
	private final boolean viewMode;
	
	public Item(SecuredUser su, DcObject src, boolean viewMode) {
		this(su, src, null, viewMode);
	}
	
	public Item(SecuredUser su, DcObject src, int limitToFields[], boolean viewMode) {

		this.viewMode = viewMode;
		
		id = src.getID();
		name = src.toString();
		moduleIdx = src.getModuleIdx();

		List<Integer> fields = new LinkedList<Integer>();

		if (limitToFields != null) {
			for (int i : limitToFields)
				fields.add(Integer.valueOf(i));
		} else {
			WebFieldDefinitions definitions = (WebFieldDefinitions) 
					DcModules.get(moduleIdx).getSettings().getDefinitions(DcRepository.ModuleSettings.stWebFieldDefinitions);
			
			for (WebFieldDefinition definition : definitions.getDefinitions()) {
				fields.add(Integer.valueOf(definition.getFieldIdx()));
			}
		}
		
		if (new File(new File(DcConfig.getInstance().getImageDir(), id), "picture1.jpg").exists()) {
			scaledImageUrl = src.getScaledImageUrl();
			imageUrl = src.getImageUrl();
		} else {
			scaledImageUrl = null;
			imageUrl = null;
		}
		
		if (src.getIcon() != null)
			icon = String.valueOf(Base64.encode(src.getIcon().getBytes()));
		
		addFields(su, src, fields);
		
		if (viewMode)
			addPictures(src.getID());
	}
	
	private void addPictures(String id) {
		for (org.datacrow.core.pictures.Picture p : 
				PictureManager.getInstance().getPictures(id)) {
			
			pictures.add(new Picture(p));
		}
	}
	
	private void addFields(SecuredUser su, DcObject src, List<Integer> fields) {
		Module m = ModuleManager.getInstance().getModule(su, src.getModuleIdx());
		DcModule module = DcModules.get(m.getIndex());
		
		Field field;
		Field fieldCpy;
		for (int fieldIdx : fields) {
			field = m.getField(fieldIdx);
			
			if (   (module.getField(fieldIdx).isEnabled())  && // check on the master settings whether the field is enabled
					module.getField(fieldIdx).getValueType() != DcRepository.ValueTypes._PICTURE && // prevent the picture x..z fields from appearing
					su.isAuthorized(m.getIndex(), field.getIndex())) { // and the user needs to be authorized to see the field
				
				fieldCpy = new Field(field);
				
				if (!field.isReadOnly()) {
					// set editable based on permissions
					fieldCpy.setReadOnly(!su.isEditingAllowed(m.getIndex(), field.getIndex()));
				}
				
				this.fields.add(new FieldValue(
						fieldCpy, 
						toValidValue(fieldCpy, src.getValue(fieldIdx), src.getDisplayString(fieldIdx))));
			}
		}
	}	
	
	@SuppressWarnings("unchecked")
	private Object toValidValue(Field field, Object o, String formatted) {
		Object value = o;
		
		if (o instanceof DcObject) {
			DcObject dco = (DcObject) o;
			
			if (viewMode) {
				value = new Reference(dco);
			} else {
				value = dco.getID();
			}
		
		} else if (
				field.getType() == Field._MULTIRELATE ||
				field.getType() == Field._TAGFIELD) {

			if (o == null || ((Collection<DcMapping>) o).size() == 0) {
				
				value = viewMode ? null : new ArrayList<Object>();

			} else if (!viewMode) {
				Collection<String> c = new ArrayList<String>();

				for (DcMapping item : (Collection<DcMapping>) o)
					c.add(item.getReferencedID());
				
				value = c;

			} else {
				Collection<Reference> c = new ArrayList<Reference>();

				for (DcMapping item : (Collection<DcMapping>) o)
					c.add(new Reference(item.getReferencedObject()));
				
				value = c;
			}
		} else if (o instanceof DcModule || o instanceof Collection) {
			
			value = "";
			
		} else if (field.getType() == Field._RATING) {
		
			value = o;
			
		} else if (o instanceof Date) {
		
			value = viewMode ? formatted : o.toString();
		
		} else if (viewMode) {
			// catch all for the view mode;
			value = formatted;
		}
		
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
	
	public List<Picture> getPictures() {
		return pictures;
	}
}