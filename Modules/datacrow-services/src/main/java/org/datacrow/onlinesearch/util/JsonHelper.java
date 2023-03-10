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

package org.datacrow.onlinesearch.util;

import java.util.Map;

import org.datacrow.core.objects.DcMediaObject;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.StringUtils;

public class JsonHelper {
	
    public static void setString(Map<?, ?> map, String tag, DcObject dco, int fieldIdx) {
    	
    	Object o = map.get(tag);
    	
    	if (!CoreUtilities.isEmpty(o)) {
    		String s = o instanceof String ? (String) o : o.toString();
    		dco.setValue(fieldIdx, s);
    	}
    }
    
    public static void setYear(Map<?, ?> map, String tag, DcObject dco) {
    	Object value = map.get(tag);
    	
    	if (value instanceof Number) {
    		int year = ((Number) value).intValue();
    		dco.setValue(DcMediaObject._C_YEAR, Integer.valueOf(year));
    	} else if (value instanceof String) {
    		String year = (String) map.get(tag);
			year = year.length() == 10 ? year.substring(0, 4) :
				year.length() == 4 ? year : null;

			if (year != null && StringUtils.getContainedNumber(year).length() == 4)
				dco.setValue(DcMediaObject._C_YEAR, Integer.valueOf(year));
    	}
	}
}
