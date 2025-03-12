package org.datacrow.server.web.api.manager;

import java.util.ArrayList;
import java.util.List;

import org.datacrow.core.DcConfig;
import org.datacrow.core.data.DataFilter;
import org.datacrow.core.data.DataFilterEntry;
import org.datacrow.core.data.Operator;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.server.Connector;
import org.datacrow.server.web.api.model.Reference;

public class ReferenceManager {
	
	private static ReferenceManager instance = new ReferenceManager();
	 
    public static ReferenceManager getInstance() {
        return instance;
    }

	public List<Reference> getReferences(int moduleIdx) {
		
		DcModule module = DcModules.get(moduleIdx);
		List<Reference> references = new ArrayList<Reference>();
        
        DataFilter df = new DataFilter(moduleIdx);
        df.setOrder(module.getDescriptiveFields());
        
        Connector connector = DcConfig.getInstance().getConnector();
        List<DcObject> items = connector.getItems(df, module.getMinimalFields(null));
        
        for (DcObject dco : items)
        	references.add(new Reference(dco));
		
        return references;
	}
	
	public Reference getReference(int moduleIdx, String itemID) {
		DcModule module = DcModules.get(moduleIdx);
        
        DataFilter df = new DataFilter(moduleIdx);
        df.addEntry(new DataFilterEntry(moduleIdx, DcObject._ID, Operator.EQUAL_TO, itemID));
        df.setOrder(module.getDescriptiveFields());
        
        Connector connector = DcConfig.getInstance().getConnector();
        List<DcObject> items = connector.getItems(df, module.getMinimalFields(null));
        
        for (DcObject dco : items)
        	return new Reference(dco);
		
        return null;
	}	
}
