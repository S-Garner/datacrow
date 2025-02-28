package org.datacrow.server.web.api.model;

import org.datacrow.core.DcConfig;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.server.Connector;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Reference {
	
	@JsonProperty("id")
	private final String id;
	@JsonProperty("name")
	private final String name;
	@JsonProperty("iconUrl")
	private final String iconUrl;
	
	public Reference(DcObject dco) {

		Connector connector = DcConfig.getInstance().getConnector();

		this.name = dco.toString();
		this.id = dco.getID();
		
    	DcImageIcon icon = dco.getIcon();
    	
    	if (icon != null) {
    		iconUrl = "http://" + connector.getImageServerAddress() + ":" + connector.getImageServerPort() + "/icons/" + icon.getFile().getName();
    		icon.flush();
    	} else {
    		iconUrl = null;
    	}
	}
	
	public String getIconUrl() {
		return iconUrl;
	}
	
	public String getName() {
		return name;
	}
	
	public String getId() {
		return id;
	}
}