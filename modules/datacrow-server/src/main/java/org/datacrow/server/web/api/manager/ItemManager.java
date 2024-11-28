package org.datacrow.server.web.api.manager;

import java.util.ArrayList;
import java.util.List;

import org.datacrow.core.DcConfig;
import org.datacrow.core.data.DataFilter;
import org.datacrow.core.data.DataFilters;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.server.web.api.model.Item;

public class ItemManager {
	
	private static ItemManager instance = new ItemManager();
	 
    public static ItemManager getInstance() {
        return instance;
    }

	public Item getItem(int moduleIdx, String id) {
		DcModule module = DcModules.get(moduleIdx);
		DcObject dco = DcConfig.getInstance().getConnector().getItem(moduleIdx, id);
		
		if (dco != null)
			return new Item(dco, module.getFieldIndices());
		
		return null;
	}
    
	public List<Item> getItems(int moduleIdx) {
		List<Item> items = new ArrayList<Item>();
		
		DcModule module = DcModules.get(moduleIdx);
		int[] fields = module.getMinimalFields(null);
		
		List<DcObject> objects = DcConfig.getInstance().getConnector().getItems(new DataFilter(moduleIdx), fields);
		
		for (DcObject dco : objects)
			items.add(new Item(dco, fields));
		
		return items;
	}
	
	public List<Item> getItems(int moduleIdx, String search) {
		List<Item> items = new ArrayList<Item>();
		
		DcModule module = DcModules.get(moduleIdx);
		int[] fields = module.getMinimalFields(null);
		DataFilter df = DataFilters.createSearchAllFilter(moduleIdx, search);
		List<DcObject> objects = DcConfig.getInstance().getConnector().getItems(df, fields);

		for (DcObject dco : objects)
			items.add(new Item(dco, fields));
		
		return items;
	}	
	
}
