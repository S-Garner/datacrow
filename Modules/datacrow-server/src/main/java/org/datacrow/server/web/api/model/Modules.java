package org.datacrow.server.web.api.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;

public class Modules {

    private static Modules instance = new Modules();
    
    private final Collection<Module> modules = new ArrayList<Module>();
    
    /**
     * Returns the sole instance of this configuration class.
     * @return  the only instance of this class as used for the current session.
     */
    public static Modules getInstance() {
        return instance;
    }
	
    private Modules() {
    	for (DcModule m : DcModules.getAllModules())
    		modules.add(new Module(m.getIndex(), m.getName()));
    }
    
    public List<Module> getAll() {
        return modules.stream().collect(Collectors.toList());
    }
}
