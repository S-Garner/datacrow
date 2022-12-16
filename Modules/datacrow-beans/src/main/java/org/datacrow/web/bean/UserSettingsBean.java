/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.net                             *
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

package org.datacrow.web.bean;

import org.apache.logging.log4j.Level;
import org.datacrow.web.DcBean;
import org.datacrow.web.model.Module;
import org.datacrow.web.model.UserSettings;
import org.datacrow.web.util.WebUtilities;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

/**
 * This is the user settings manager bean.
 */
@Named
@SessionScoped
public class UserSettingsBean extends DcBean {
    
    private UserSettings globalSettings = new UserSettings();
    
    private static final String _GLOBAL_SCREENSIZE_X = "GLOBAL_SCREENSIZE_X";
    private static final String _GLOBAL_SCREENSIZE_Y = "GLOBAL_SCREENSIZE_Y";
    
    public UserSettingsBean() {}
    
    public int getOverviewNumberOfItemsPerRow() {
        int rows = 10;
        
        try {
            Module module = ((ModulesBean) WebUtilities.getBean("modulesBean")).getSelectedModule();
            rows = module.isAdvancedView() ? 15 : 100;

        } catch (Exception e) {
            WebUtilities.log(Level.ERROR, e);
        }
            
        return rows;
    }
    
    public int getOverviewNumberOfColumns() {
        
        int cols = 5;
        
        try {
            Module module = ((ModulesBean) WebUtilities.getBean("modulesBean")).getSelectedModule();
            
            if (getScreenSizeX() > 0)
                cols = module.isAdvancedView() ? (getScreenSizeX() - 300) / 300 : (getScreenSizeX() - 300) / 160;
            
        } catch (Exception e) {
            WebUtilities.log(Level.ERROR, e);
        }
        
        return cols;
    }
    
    public int getScreenSizeX() {
        return globalSettings.getInt(UserSettingsBean._GLOBAL_SCREENSIZE_X);
    }
    
    public int getScreenSizeY() {
        return  globalSettings.getInt(UserSettingsBean._GLOBAL_SCREENSIZE_Y);
    }
    
    public void setScreenSizeX(int x) {
        globalSettings.setValue(UserSettingsBean._GLOBAL_SCREENSIZE_X, x);
    }
    
    public void setScreenSizeY(int y) {
        globalSettings.setValue(UserSettingsBean._GLOBAL_SCREENSIZE_Y, y);
    }
}
