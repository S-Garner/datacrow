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

package org.datacrow.synch.helpers;

import java.util.ArrayList;
import java.util.Collection;

import org.datacrow.core.modules.DcModule;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcImageIcon;

public class SimpleModule {
	
	private int index;
	private String name;
	private DcImageIcon icon;
	
	private Collection<SimpleField> fields = new ArrayList<>();

	public SimpleModule(DcModule m) {
		this(m.getIndex(), m.getName(), m.getIcon32());
		
		for (DcField f : m.getFields()) {
			fields.add(new SimpleField(f));
		}
	}
	
	public SimpleModule(
			int index,
			String name,
			DcImageIcon icon) {
		
		this.index = index;
		this.name = name;
		this.icon = icon;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public DcImageIcon getIcon() {
		return icon;
	}
	
	public Collection<SimpleField> getFields() {
		return fields;
	}
}