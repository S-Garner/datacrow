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
