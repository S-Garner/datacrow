package org.datacrow.server.web.api.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;

public class Modules {

    private static Modules instance = new Modules();
    
    private final Collection<Module> allModules = new ArrayList<Module>();
    private final Collection<Module> mainModules = new ArrayList<Module>();
    
    /**
     * Returns the sole instance of this configuration class.
     * @return  the only instance of this class as used for the current session.
     */
    public static Modules getInstance() {
        return instance;
    }
	
    private Modules() {
    	for (DcModule m : DcModules.getAllModules())
    		allModules.add(new Module(m.getIndex(), m.getName(), m.getIcon32()));
    	
    	for (DcModule m : DcModules.getAllModules()) {
    		if (m.isTopModule())
    			mainModules.add(new Module(m.getIndex(), m.getName(), m.getIcon32()));
    	}
    }
    
    public List<Module> getMainModules() {
        return mainModules.stream().collect(Collectors.toList());
    }
    
    public List<Module> getAll() {
        return allModules.stream().collect(Collectors.toList());
    }
}
