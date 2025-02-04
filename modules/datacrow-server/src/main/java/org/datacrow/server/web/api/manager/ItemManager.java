package org.datacrow.server.web.api.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.datacrow.core.DcConfig;
import org.datacrow.core.data.DataFilter;
import org.datacrow.core.data.DataFilters;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.ValidationException;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.server.Connector;
import org.datacrow.server.web.api.model.Item;

public class ItemManager {
	
	private static ItemManager instance = new ItemManager();
	 
    public static ItemManager getInstance() {
        return instance;
    }

	public Item getItem(SecuredUser su, int moduleIdx, String id) {
		DcModule module = DcModules.get(moduleIdx);
		DcObject dco = DcConfig.getInstance().getConnector().getItem(moduleIdx, id);
		
		if (dco != null)
			return new Item(su, dco, module.getFieldIndices());
		
		return null;
	}
	
	public List<Item> getChildren(SecuredUser su, int childModuleIdx, String id) {
		
		List<Item> children = new ArrayList<Item>(); 
		
		DcModule cm = DcModules.get(childModuleIdx);
		Connector conn = DcConfig.getInstance().getConnector();
		
		for (DcObject child : conn.getChildren(id, childModuleIdx, cm.getMinimalFields(null))) {
			children.add(new Item(su, child, cm.getFieldIndices()));
		}
	
		return children;
	}
    
	public List<Item> getItems(SecuredUser su, int moduleIdx) {
		List<Item> items = new ArrayList<Item>();
		
		DcModule module = DcModules.get(moduleIdx);
		int[] fields = module.getMinimalFields(null);
		
		List<DcObject> objects = DcConfig.getInstance().getConnector().getItems(new DataFilter(moduleIdx), fields);
		
		for (DcObject dco : objects)
			items.add(new Item(su, dco, fields));
		
		return items;
	}
	
	public List<Item> getItems(SecuredUser su, int moduleIdx, String search) {
		List<Item> items = new ArrayList<Item>();
		
		DcModule module = DcModules.get(moduleIdx);
		int[] fields = module.getMinimalFields(null);
		DataFilter df = DataFilters.createSearchAllFilter(moduleIdx, search);
		List<DcObject> objects = DcConfig.getInstance().getConnector().getItems(df, fields);

		for (DcObject dco : objects)
			items.add(new Item(su, dco, fields));
		
		return items;
	}
	
	public void saveItem(Map<Object, Object> data, DcObject dco) throws ValidationException {
		Object value;
		String key;
		int fieldIdx;
		
    	for (Object o : data.keySet()) {
    		key = (String) o; 
    		value = data.get(key);
    		fieldIdx = Integer.parseInt(
    				key.substring(key.lastIndexOf("-") + 1));
    		
    		System.out.println("field found: " + fieldIdx + ", value: " + value);
    	}

	}
	
}
