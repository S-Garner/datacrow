package org.datacrow.server.web.api.model;

import java.util.ArrayList;
import java.util.Collection;

import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.utilities.Base64;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Module {

	@JsonProperty("index")
	private final int index;
	@JsonProperty("name")
	private final String name;
	@JsonProperty("icon")
	private String icon;

	@JsonProperty("children")
	private Collection<Module> children = new ArrayList<Module>();

	public Module(int index, String name, DcImageIcon icon) {
		this.index = index;
		this.name = name;
		this.icon = icon == null ? null : String.valueOf(Base64.encode(icon.getBytes()));

		for (DcModule child : DcModules.getReferencedModules(index)) {

			if (child.isEnabled() && 
		       !child.isSelectableInUI() && // if this is set, it is already added as a main module
				child.getIndex() != index && 
				child.getType() != DcModule._TYPE_PROPERTY_MODULE &&
				child.getType() != DcModule._TYPE_EXTERNALREFERENCE_MODULE &&
				child.getIndex() != DcModules._CONTACTPERSON &&
			    child.getIndex() != DcModules._TAG &&
				child.getIndex() != DcModules._CONTAINER) {

				children.add(new Module(child.getIndex(), child.getLabel(), child.getIcon32()));
			}
		}
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

	public Module[] getChildren() {
		Module[] moduleArray = new Module[children.size()];
		moduleArray = children.toArray(moduleArray);
		return moduleArray;
	}
}
