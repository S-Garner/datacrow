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

package org.datacrow.client.console.components;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.border.Border;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.settings.DcSettings;

public class DcViewDivider extends JSplitPane {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DcViewDivider.class.getName());
    
    private final String settingKey;
    
    public DcViewDivider(JComponent left, JComponent right, String settingKey) {
        super(JSplitPane.HORIZONTAL_SPLIT, left, right);
        this.settingKey = settingKey;
        
        Border border = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        setBorder(border);
    }
    
    public void deactivate() {
        DcSettings.set(settingKey, getDividerLocation());
    }
    
    public void applyDividerLocation() {
        if (isEnabled()) {

        	setResizeWeight(0.2);
        	
        	try {
        		int loc = DcSettings.getInt(settingKey);
        		
        		if ((getWidth() > 0 && loc > getWidth()) || loc <= 0) {
        			setDividerLocation(0.5d);
        		} else {
        			setDividerLocation(loc);	
        		}
        		
        	} catch (Exception e) {
        		logger.error(e, e);
        	}
        }
    }
}
