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

package org.datacrow.web.util;

import org.apache.logging.log4j.Level;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.utilities.StringUtils;
import org.datacrow.web.model.Field;

import jakarta.el.ELContext;
import jakarta.faces.context.FacesContext;

public abstract class WebUtilities {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(WebUtilities.class.getName());
    
    public static String getValue(DcObject dco, Field f, Object value) {
        return getValue(dco, f.getIndex(), f.getMaxTextLength(), value);
    }

    public static Object getBean(String name) throws ClassNotFoundException {
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Object bean = elContext.getELResolver().getValue(elContext, null, name);
        
        if (bean == null)
            throw new ClassNotFoundException("Bean " + name + " could not be found");
        
        return bean;
    }

    private static String getValue(DcObject dco, int fieldIdx, int maxTextLength, Object value) {
        DcField field = dco.getField(fieldIdx);
        String s = "";
        s = dco.getDisplayString(field.getIndex());

        if (maxTextLength != 0)
            s = StringUtils.concatUserFriendly(s, maxTextLength);
        
        return s;
    }
    
    public static void log(Level level, String msg) {
        log(level, msg, null);
    }
    
    public static void log(Level level, Exception e) {
        log(level, e.getMessage(), e);
    }
    
    public static void log(Level level, String msg, Exception e) {
        if (level == Level.DEBUG)
            if (e == null)
                logger.debug(msg);
            else
                logger.debug(e, e);
        if (level == Level.ERROR)
            if (e == null)
                logger.error(msg);
            else
                logger.error(e, e);
        if (level == Level.WARN)
            if (e == null)
                logger.warn(msg);
            else
                logger.warn(e, e);
        if (level == Level.INFO)
            if (e == null)
                logger.info(msg);
            else
                logger.info(e, e);    
    }
}
