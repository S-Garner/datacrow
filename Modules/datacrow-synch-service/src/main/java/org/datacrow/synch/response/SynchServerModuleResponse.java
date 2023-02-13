package org.datacrow.synch.response;

import java.util.ArrayList;
import java.util.Collection;

import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.synch.helpers.SimpleModule;

public class SynchServerModuleResponse extends SynchServerResponse {
	
	private static final long serialVersionUID = 1L;
	
	private Collection<SimpleModule> modules = new ArrayList<>();

	public SynchServerModuleResponse() {
	    super(_RESPONSE_MODULES);
	    
	    for (DcModule m : DcModules.getModules()) {
	    	if (m.isSelectableInUI() && m.isEnabled())
	    		modules.add(new SimpleModule(m));
	    }
	}

	public Collection<SimpleModule> getModules() {
		return modules;
	}
}
