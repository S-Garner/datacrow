/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.org                             *
 *                                                                            *
 *                       This file is part of Data Crow.                      *
 *       Data Crow is free software; you can redistribute it and/or           *
 *        modify it under the terms of the GNU General Public                 *
 *       License as published by the Free Software Foundation; either         *
 *              version 3 of the License, or any later version.               *
 *                                                                            *
 *        Data Crow is distributed in the hope that it will be useful,        *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *           MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.             *
 *           See the GNU General Public License for more details.             *
 *                                                                            *
 *        You should have received a copy of the GNU General Public           *
 *  License along with this program. If not, see http://www.gnu.org/licenses  *
 *                                                                            *
 ******************************************************************************/

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
