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

package org.datacrow.onlinesearch.mobygames;

import java.util.ArrayList;
import java.util.Collection;

import org.datacrow.core.objects.DcObject;

public class MobyGamesResult {
    
    private DcObject dco;
    
    private String cover;
    
    private Collection<String> screenshots = new ArrayList<>();
    
    public MobyGamesResult(DcObject dco) {
        this.dco = dco;
    }
    
    public DcObject getDco() {
        return dco;
    }
    
    public Collection<String> getScreenshotLinks() {
        return screenshots;
    }
    
    public void addScreenshot(String link) {
        screenshots.add(link);
    }
    
    public String getCover() {
    	return cover;
    }
    
    public void addCover(String link) {
    	this.cover = link;
    }
}
