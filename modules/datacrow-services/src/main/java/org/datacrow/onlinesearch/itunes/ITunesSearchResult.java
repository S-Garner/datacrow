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

package org.datacrow.onlinesearch.itunes;

import org.datacrow.core.objects.DcObject;

public class ITunesSearchResult {
    
    private final DcObject dco;
    private final String id;
    
    private String coverUrl;
    
    public ITunesSearchResult(DcObject dco, String id) {
        this.dco = dco;
        this.id = id;
    }
    
    public DcObject getDco() {
        return dco;
    }
    
    public String getId() {
        return id;
    }
    
    public void setCoverUrl(String url) {
        this.coverUrl = url;
    }
    
    public String getCoverUrl() {
        return coverUrl;
    }
}
