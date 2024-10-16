package org.datacrow.server.web.api.model;

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
	
	public Module(int index, String name, DcImageIcon icon) {
		this.index = index;
		this.name = name;
		this.icon = icon == null ? null : String.valueOf(Base64.encode(icon.getBytes()));
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
}
