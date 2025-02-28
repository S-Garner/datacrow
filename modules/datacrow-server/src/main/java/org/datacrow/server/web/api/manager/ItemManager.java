package org.datacrow.server.web.api.manager;

import java.util.ArrayList;
import java.util.Collection;
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
import org.datacrow.core.objects.DcMapping;
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

    public void delete(SecuredUser su, int moduleIdx, String id) throws ValidationException {
    	Connector conn = DcConfig.getInstance().getConnector();
    	DcObject dco = DcConfig.getInstance().getConnector().getItem(moduleIdx, id);
    	
    	if (dco != null)
    		conn.deleteItem(dco);
    }
    
	public Item getItem(SecuredUser su, int moduleIdx, String id, boolean full) {
		DcObject dco = DcConfig.getInstance().getConnector().getItem(moduleIdx, id);
		
		if (dco != null)
			return new Item(su, dco, full);
		
		return null;
	}
	
	public List<Item> getChildren(SecuredUser su, int childModuleIdx, String id) {
		
		List<Item> children = new ArrayList<Item>(); 
		
		DcModule cm = DcModules.get(childModuleIdx);
		Connector conn = DcConfig.getInstance().getConnector();
		
		for (DcObject child : conn.getChildren(id, childModuleIdx, cm.getMinimalFields(null))) {
			children.add(new Item(su, child, false));
		}
	
		return children;
	}
    
	public List<Item> getItems(SecuredUser su, int moduleIdx) {
		List<Item> items = new ArrayList<Item>();
		
		DcModule module = DcModules.get(moduleIdx);
		int[] fields = module.getMinimalFields(null);
		
		List<DcObject> objects = DcConfig.getInstance().getConnector().getItems(new DataFilter(moduleIdx), fields);
		
		for (DcObject dco : objects)
			items.add(new Item(su, dco, fields, false));
		
		return items;
	}
	
	public List<Item> getItems(SecuredUser su, int moduleIdx, String search) {
		List<Item> items = new ArrayList<Item>();
		
		DcModule module = DcModules.get(moduleIdx);
		int[] fields = module.getMinimalFields(null);
		DataFilter df = DataFilters.createSearchAllFilter(moduleIdx, search);
		List<DcObject> objects = DcConfig.getInstance().getConnector().getItems(df, fields);

		for (DcObject dco : objects)
			items.add(new Item(su, dco, fields, false));
		
		return items;
	}
	
	public void saveItem(Map<Object, Object> data, DcObject dco, boolean isNew) throws ValidationException {
		
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
    		
    		// skip these fields as they will be different regardless
    		if (fieldIdx != DcObject._SYS_AVAILABLE && 
    			fieldIdx != DcObject._SYS_CREATED && 
    			fieldIdx != DcObject._SYS_MODIFIED && 
    			fieldIdx != DcObject._SYS_EXTERNAL_REFERENCES) {
    			
    			applyValue(dco, cpy, fieldIdx, oldValue, newValue);
    		}
    	}

    	if (dco.isChanged() || isNew) {
    		dco.setNew(isNew);
    		conn.saveItem(dco);
    	}
    	
    	cpy.cleanup();
	}
	
	private void applyValue(DcObject dco, DcObject cpy, int fieldIdx, Object oldValue, Object newValue) {

		// both are empty - skip
		if ((CoreUtilities.isEmpty(newValue) && CoreUtilities.isEmpty(oldValue)) ||
			(CoreUtilities.isEmpty(oldValue) && (newValue instanceof Boolean && newValue.equals(Boolean.FALSE))))
			return;
		
		DcField field = dco.getField(fieldIdx);
		
		// handle all to empty here
		if (CoreUtilities.isEmpty(newValue)) {
			dco.setValueLowLevel(fieldIdx, null);
			
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
				applySingleReference(dco, fieldIdx, oldValue, newValue);
			} else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
				applyMultiReferences(dco, fieldIdx, oldValue, newValue);
			} else {
				// set the value on the shadow copy and retrieve it back.
				cpy.setValue(fieldIdx, newValue);
				newValue = cpy.getValue(fieldIdx);
				
				if (!CoreUtilities.getComparableString(oldValue).
						equals(CoreUtilities.getComparableString(newValue))) {
					
					dco.setValue(fieldIdx, newValue);
				}
			}
		}
	}
	
	private void applySingleReference(DcObject dco, int fieldIdx, Object oldValue, Object newValue) {
		int moduleIdx = DcModules.getReferencedModule(dco.getField(fieldIdx)).getIndex();
		
		Connector conn = DcConfig.getInstance().getConnector();
		DcObject ref;
		if (newValue instanceof Map) {
			@SuppressWarnings("rawtypes")
			String id = (String) ((Map) newValue).get("value");
			
			// clearing fields is something we've done right at the start, no need to check for this here
			if (oldValue == null || !((DcObject) oldValue).getID().equals(id)) {
				ref = conn.getItem(moduleIdx, id);
				dco.createReference(fieldIdx, ref);
			}
		}
	}
	
	private void applyMultiReferences(DcObject dco, int fieldIdx, Object oldValue, Object newValue) {
		removeMultiReferences(dco, fieldIdx, oldValue, newValue);
		addMultiReferences(dco, fieldIdx, oldValue, newValue);
	}	
	
	private void removeMultiReferences(DcObject dco, int fieldIdx, Object oldValue, Object newValue) {
		@SuppressWarnings("unchecked")
		Collection<DcMapping> mappings = (Collection<DcMapping>) dco.getValue(fieldIdx);
		
		// if there are no mappings we do not have to do anything
		if (mappings == null || mappings.size() == 0)
			return;

		// create a list of the new ids for easy reference
		Collection<String> newIds = new ArrayList<String>();
		
		String id;
		Map<?, ?> values;
		
		for (Object e : (Collection<?>) newValue) {
			values = (Map<?, ?>) e;
			id = (String) values.get("value");
			newIds.add(id);
		}

		// check if the existing mappings are part of the new ids list
		Collection<DcMapping> remove = new ArrayList<DcMapping>();
		for (DcMapping mapping : mappings) {
			id = (String) mapping.getValue(DcMapping._B_REFERENCED_ID);

			if (!newIds.contains(id))
				remove.add(mapping);
		}
		
		if (remove.size() > 0) {
			mappings.removeAll(remove);
			dco.setValueLowLevel(fieldIdx, mappings);
		}
	}
	
	private void addMultiReferences(DcObject dco, int fieldIdx, Object oldValue, Object newValue) {
		Connector conn = DcConfig.getInstance().getConnector();
		int moduleIdx = DcModules.getReferencedModule(dco.getField(fieldIdx)).getIndex();
		
		String id;
		String name;
		Map<?, ?> values;
		
		DcObject ref;
		
		@SuppressWarnings("unchecked")
		Collection<DcMapping> mappings = (Collection<DcMapping>) dco.getValue(fieldIdx);
		boolean create;
		
		for (Object e : (Collection<?>) newValue) {
			
			create = false;
			
			values = (Map<?, ?>) e;
			id = (String) values.get("value");
			name = (String) values.get("label");
			
			create = CoreUtilities.isEmpty(id) || id.equals(name) || mappings == null || mappings.size() == 0;
			
			if (!create) {
				
				// next phase; check if the value exists - if not found, create is set to true
				create = true;
				
				for (DcMapping mapping : mappings) {
					if (mapping.getValue(DcMapping._B_REFERENCED_ID).equals(id)) {
						create = false;
						break;
					}
				}
			}
			
			if (create) {
				// check if id is set, else create by label - this is used for create list fields & tags
				if (CoreUtilities.isEmpty(id) || id.equals(name)) {
					dco.createReference(fieldIdx, name);
				} else {
					ref = conn.getItem(moduleIdx, id);
					dco.createReference(fieldIdx, ref);
				}
			}
		}		
	}
}