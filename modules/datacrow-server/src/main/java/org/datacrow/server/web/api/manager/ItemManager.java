package org.datacrow.server.web.api.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.data.DataFilter;
import org.datacrow.core.data.DataFilters;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.ValidationException;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.server.Connector;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.server.web.api.model.Item;

public class ItemManager {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ItemManager.class.getName());
	
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
		
		Connector conn = DcConfig.getInstance().getConnector();
		
		Object newValue;
		Object oldValue;
		String key;
		int fieldIdx;
		
		// use a shadow copy as this allows setting strings as values, etc.		
		DcObject cpy = dco.clone();
		cpy.markAsUnchanged();
		
    	for (Object o : data.keySet()) {
    		key = (String) o; 
    		newValue = data.get(key);
    		fieldIdx = Integer.parseInt(
    				key.substring(key.lastIndexOf("-") + 1));
    	
    		oldValue = dco.getValue(fieldIdx);
    		applyValue(dco, cpy, fieldIdx, oldValue, newValue);
    	}

    	if (dco.isChanged())
    		conn.saveItem(dco);
    	
    	cpy.cleanup();
	}
	
	private void applyValue(DcObject dco, DcObject cpy, int fieldIdx, Object oldValue, Object newValue) {
		
		// skip these fields as they will be different regardless
		if (	fieldIdx == DcObject._SYS_AVAILABLE || 
				fieldIdx == DcObject._SYS_CREATED || 
				fieldIdx == DcObject._SYS_MODIFIED || 
				fieldIdx == DcObject._SYS_EXTERNAL_REFERENCES)
			return;
		
		// both are empty - skip
		if ((CoreUtilities.isEmpty(newValue) && CoreUtilities.isEmpty(oldValue)) ||
			(CoreUtilities.isEmpty(oldValue) && (newValue instanceof Boolean && newValue.equals(Boolean.FALSE))))
			return;
		
		DcField field = dco.getField(fieldIdx);
		
		// handle all to empty here
		if (CoreUtilities.isEmpty(newValue)) {
			dco.setValue(fieldIdx, "");
			
		// apply all others, here
		} else {
			
			if (field.getValueType() == DcRepository.ValueTypes._DATE) {
				
				try {
					newValue = CoreUtilities.toDate((String) newValue, "yyyy-dd-MM");
				} catch (Exception e) {
					logger.error("Could not set value for field [" + fieldIdx + "]", e);
				}
				
			}
			
			if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
				
				System.out.println();
				
			} else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
				
				System.out.println();
				
			} else {
				
				cpy.setValue(fieldIdx, newValue);
				
				newValue = cpy.getValue(fieldIdx);
				
				if (!CoreUtilities.getComparableString(oldValue).
						equals(CoreUtilities.getComparableString(newValue))) {
					
					logger.debug(field.getLabel() + 
							" has been changed. Old [" + (oldValue == null ? "" : oldValue) + "] new value [" + newValue + "]");
					
					dco.setValue(fieldIdx, newValue);
				}
			}
		}
	}
}
