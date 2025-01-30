package org.datacrow.server.web.api.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.server.web.api.model.Module;

public class ModuleManager {

    private static ModuleManager instance = new ModuleManager();
    
    private final Collection<Module> allModules = new ArrayList<Module>();
    private final Collection<Module> mainModules = new ArrayList<Module>();
    
    public static ModuleManager getInstance() {
        return instance;
    }
	
    private ModuleManager() {
    	for (DcModule m : DcModules.getAllModules())
    		allModules.add(new Module(m));
    	
    	for (DcModule m : DcModules.getAllModules()) {
    		if (m.isSelectableInUI() && m.isEnabled())
    			mainModules.add(new Module(m));
    	}
    }
    
    public Module getModule(int index) {
    	for (Module module : allModules) {
    		if (module.getIndex() == index)
    			return module;
    	}
    	
    	return null;
    }
    
    public List<Module> getMainModules() {
        return mainModules.stream().collect(Collectors.toList());
    }
    
    public List<Module> getAll() {
        return allModules.stream().collect(Collectors.toList());
    }
}
