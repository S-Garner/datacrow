package org.datacrow.core.modules.upgrade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ModuleUpgradeResult {

	private final Map<Integer, Collection<Integer>> addedFields = new HashMap<>();
	private final Map<Integer, Collection<Integer>> removedFields = new HashMap<>();
	
	public ModuleUpgradeResult() {}
	
	public void addAddedField(int moduleIdx, int fieldIdx) {
		
		Collection<Integer> fields = addedFields.get(Integer.valueOf(moduleIdx));
		
		fields = fields == null ? new ArrayList<>() : fields;
		if (!fields.contains(Integer.valueOf(moduleIdx)))
			fields.add(Integer.valueOf(fieldIdx));
		
		addedFields.put(Integer.valueOf(moduleIdx), fields);
	}
	
	public boolean isAdded(int moduleIdx, int fieldIdx) {
		Collection<Integer> fields = addedFields.get(Integer.valueOf(moduleIdx));
		return fields != null && fields.contains(Integer.valueOf(fieldIdx));
	}
	
	public Collection<Integer> getAddedFields(int moduleIdx) {
		return addedFields.get(Integer.valueOf(moduleIdx));
	}

	public Collection<Integer> getRemovedFields(int moduleIdx) {
		return removedFields.get(Integer.valueOf(moduleIdx));
	}
	
	public void addRemovedField(int moduleIdx, int fieldIdx) {
		
		Collection<Integer> fields = removedFields.get(Integer.valueOf(moduleIdx));
		
		fields = fields == null ? new ArrayList<>() : fields;
		if (!fields.contains(Integer.valueOf(moduleIdx)))
			fields.add(Integer.valueOf(fieldIdx));
		
		removedFields.put(Integer.valueOf(moduleIdx), fields);
	}
	
	public boolean isRemoved(int moduleIdx, int fieldIdx) {
		Collection<Integer> fields = removedFields.get(Integer.valueOf(moduleIdx));
		return fields != null && fields.contains(Integer.valueOf(fieldIdx));
	}	
}
