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

package org.datacrow.onlinesearch.discogs;

import org.datacrow.core.objects.DcObject;

public class DiscogsSearchResult {

	private DcObject dco;
    private String coverUrl;
    private String detailsUrl;
    
    public DiscogsSearchResult(DcObject dco) {
        this.dco = dco;
    }
    
    public DcObject getDco() {
        return dco;
    }
    
    public void setDetailsUrl(String url) {
        this.detailsUrl = url;
    }
    
    public String getDetailsUrl() {
        return detailsUrl;
    }
    
    public void setCoverUrl(String url) {
        this.coverUrl = url;
    }
    
    public String getCoverUrl() {
        return coverUrl;
    }
}
