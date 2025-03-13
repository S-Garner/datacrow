package org.datacrow.server.web.api.model;

import java.io.File;

import org.datacrow.core.DcConfig;
import org.datacrow.core.objects.DcObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RelatedItem {

	@JsonProperty("id")
	private String id;
	@JsonProperty("moduleIdx")
	private int moduleIdx;
	@JsonProperty("name")
	private String name;
	@JsonProperty("scaledImageUrl")
	private final String scaledImageUrl;
	
	public RelatedItem(DcObject src) {
		id = src.getID();
		name = src.toString();
		moduleIdx = src.getModuleIdx();

		if (new File(new File(DcConfig.getInstance().getImageDir(), id), "picture1.jpg").exists()) {
			scaledImageUrl = src.getScaledImageUrl();
		} else {
			scaledImageUrl = null;
		}
	}
	
	public String getScaledImageUrl() {
		return scaledImageUrl;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public int getModuleIdx() {
		return moduleIdx;
	}	
}