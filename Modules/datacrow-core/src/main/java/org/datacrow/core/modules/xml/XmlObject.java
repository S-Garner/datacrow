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

package org.datacrow.core.modules.xml;

import java.io.Serializable;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.InvalidValueException;
import org.datacrow.core.utilities.XMLParser;
import org.w3c.dom.Element;

/**
 * Representation of a XML structure.
 * Delivers additional parsing methods. 
 * 
 * @see XmlModule
 * 
 * @author Robert Jan van der Waals
 */
public abstract class XmlObject implements Serializable {
    
    private transient static DcLogger logger = DcLogManager.getInstance().getLogger(XmlObject.class.getName());
    
    private static final long serialVersionUID = 1L;

    public Class<?> getClass(String tag, String className) throws InvalidValueException {
        String s = className;

        if (s.startsWith("net.datacrow")) {
            logger.debug("Old class filename encounter [" + s + "], updating to new name.");
            s = s.replace("net.datacrow", "org.datacrow");
        }
        
        try {
            logger.debug("Trying to locate class " + s);
            Class<?> cl = s != null && s.trim().length() > 0 ? Class.forName(s) : null;
            return cl;
        } catch (Exception e) {
            throw new InvalidValueException("Could not instantiate [" + s + "] for " + tag);
        }
    }
    
    public Class<?> getClass(Element element, String tag) throws InvalidValueException {
        String s = XMLParser.getString(element, tag);
        
        if (s.startsWith("net.datacrow")) {
            logger.debug("Old class filename encounter [" + s + "], updating to new name.");
            s = s.replace("net.datacrow", "org.datacrow");
        }
        
        try {
            logger.debug("Trying to locate class " + s);
            Class<?> cl = s != null && s.trim().length() > 0 ? Class.forName(s) : null;
            return cl;
        } catch (ClassNotFoundException e) {
            throw new InvalidValueException(tag, s);
        } catch (Exception e) {
            throw new InvalidValueException("Could not instantiate [" + s + "] for " + tag);
        }
    }
}
