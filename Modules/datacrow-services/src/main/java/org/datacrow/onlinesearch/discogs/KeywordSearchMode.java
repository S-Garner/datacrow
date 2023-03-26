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

import org.datacrow.core.objects.helpers.MusicAlbum;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.services.SearchMode;

public class KeywordSearchMode extends SearchMode {

    public KeywordSearchMode() {
        super(MusicAlbum._A_TITLE);
    }

    @Override
    public String getDisplayName() {
        return DcResources.getText("lblKeywordSearchMode");
    }

    @Override
    public boolean singleIsPerfect() {
        return false;
    }

    @Override
    public boolean keywordSearch() {
        return true;
    }
}
