package org.datacrow.server.web.api.model;

import java.util.ArrayList;
import java.util.Collection;

import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.utilities.Base64;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Module {

	@JsonProperty("index")
	private final int index;
	@JsonProperty("name")
	private final String name;
	@JsonProperty("icon")
	private final String icon;
	@JsonProperty("main")
	private final boolean main;
	@JsonProperty("references")
	private final Collection<Module> references = new ArrayList<Module>();
	@JsonProperty("fields")
	private final Collection<Field> fields = new ArrayList<Field>();
	@JsonProperty("hasChild")
	private boolean hasChild;
	@JsonProperty("child")
	private Module child;
	@JsonProperty("itemName")
	private final String itemName;
	@JsonProperty("itemNamePlural")
	private final String itemNamePlural;
	@JsonProperty("isAbstract")
	private final boolean isAbstract;
	
	public Module(DcModule module, boolean addReferences) {
		this.index = module.getIndex();
		this.name = module.getModuleResourceKey();
		this.main = module.isSelectableInUI();
		this.hasChild = module.isParentModule();
		this.itemNamePlural = module.getItemPluralResourceKey();
		this.itemName = module.getItemResourceKey();
		this.isAbstract = module.isAbstract();
		
		if (hasChild)
			this.child = new Module(module.getChild(), false);
		
		DcImageIcon icon = module.getIcon32();
		
		this.icon = icon == null ? null : String.valueOf(Base64.encode(icon.getBytes()));

		for (DcField field : DcModules.get(index).getFields())
			fields.add(new Field(field));
		
		if (addReferences) {
			Module reference;
			for (DcModule m : DcModules.getReferencedModules(index)) {
		
				if (m.isEnabled() && 
			       !m.isSelectableInUI() && // if this is set, it is already added as a main module
					m.getIndex() != index && 
					m.getType() != DcModule._TYPE_PROPERTY_MODULE &&
					m.getType() != DcModule._TYPE_EXTERNALREFERENCE_MODULE &&
					m.getIndex() != DcModules._CONTACTPERSON &&
				    m.getIndex() != DcModules._TAG &&
				    m.getIndex() != DcModules._PERMISSION &&
					m.getIndex() != DcModules._CONTAINER) {
		
					reference = new Module(m, false);
					
					references.add(reference);
				}
			}
		}
	}
	
	public Collection<Field> getFields() {
		return fields;
	}
	
	public Field getField(int idx) {
		for (Field fld : fields) {
			if (fld.getIndex() == idx)
				return fld;
		}
		
		return null;
	}
	
	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public String getIcon() {
		return icon;
	}
	
	public boolean isMain() {
		return main;
	}
	
	public boolean isAbstract() {
		return isAbstract;
	}
	
	public String getItemName() {
		return itemName;
	}

	public String getItemNamePlural() {
		return itemNamePlural;
	}
	
	public Module[] getReferences() {
		Module[] moduleArray = new Module[references.size()];
		moduleArray = references.toArray(moduleArray);
		return moduleArray;
	}
}