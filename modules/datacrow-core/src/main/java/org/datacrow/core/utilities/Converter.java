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

package org.datacrow.core.utilities;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;

public class Converter {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(Converter.class.getName());

    public static String getValidXmlTag(String s) {
        String tag = s == null ? "" : s;
        tag = tag.replaceAll("[()'\"`\\!@#$%^&*:;|\\_<>\\[\\]{}?/]", " ");
        
        while (tag.indexOf("  ") > -1)
        	tag = tag.replaceAll("  ", " ");	
        
        tag = tag.replaceAll(" ", "-");
        tag = tag.toLowerCase();

        tag = tag.endsWith("-") ? tag.substring(0, tag.lastIndexOf("-")) : tag;
        
        try {
        	tag = tag.length() > 0 && Character.isDigit(tag.charAt(0)) ? "fld-" + tag : tag;
        } catch (Exception e) {
        	logger.error(e, e);
        }
        	
        return tag;        
    }
    
    public static String databaseValueConverter(String s) {
        String converted = s;
        converted = converted.replaceAll("\u0000", " ");
        converted = converted.replaceAll(">", " ");
        return converted;
    }
}
