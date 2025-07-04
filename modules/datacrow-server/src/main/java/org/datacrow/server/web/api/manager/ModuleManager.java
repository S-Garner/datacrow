package org.datacrow.server.web.api.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.server.web.api.model.Module;

public class ModuleManager {

    private static ModuleManager instance = new ModuleManager();
    
    private final Collection<Module> modules = new ArrayList<Module>();
    
    public static ModuleManager getInstance() {
        return instance;
    }
	
    private ModuleManager() {
    	for (DcModule m : DcModules.getAllModules()) {
    		modules.add(new Module(m, true));
    	}
    }
    
    public Module getModule(SecuredUser su, int index) {
    	Collection<Module> copy = new ArrayList<Module>(modules);
    	
    	for (Module module : copy) {
    		if (module.getIndex() == index && su.isAuthorized(module.getIndex()))
    			return module;
    	}
    	
    	return null;
    }
    
    public List<Module> getModules(SecuredUser su) {
    	Collection<Module> copy = new ArrayList<Module>(modules);
    	
        return copy.stream()
        		.filter(module -> DcModules.get(module.getIndex()).isEnabled() && module.getIndex() != DcModules._USER && su.isAuthorized(module.getIndex()))
        		.collect(Collectors.toList());
    }
}
