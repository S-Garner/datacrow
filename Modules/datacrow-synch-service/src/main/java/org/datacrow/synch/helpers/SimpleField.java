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

import org.datacrow.core.objects.DcField;

public class SimpleField {

	private int index;
	private String name;
	private String columnName;
	private int valueType;
	private int maxLength;
	
	public SimpleField(DcField f) {
		this(f.getIndex(),
			 f.getLabel(), 
			 f.getDatabaseFieldName(), 
			 f.getValueType(), 
			 f.getMaximumLength());
	}
	
	public SimpleField(
			int index,
			String name,
			String columnName,
			int valueType,
			int maxLength) {
		
		this.index = index;
		this.name = name;
		this.columnName = columnName;
		this.valueType = valueType;
		this.maxLength = maxLength;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public String getColumnName() {
		return columnName;
	}

	public int getValueType() {
		return valueType;
	}

	public int getMaxLength() {
		return maxLength;
	}
}
