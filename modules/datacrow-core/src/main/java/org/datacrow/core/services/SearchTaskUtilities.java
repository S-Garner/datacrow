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

package org.datacrow.core.services;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Book;
import org.datacrow.core.utilities.isbn.ISBN;
import org.datacrow.core.utilities.isbn.InvalidBarCodeException;

public abstract class SearchTaskUtilities {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(SearchTaskUtilities.class.getName());
    
    public static final void checkForIsbn(SearchTask task) {

        // clean search query (removed non digits)
        if (task.getMode() instanceof IsbnSearchMode) {
            String query = task.getQuery();
            query = query.replaceAll("x|X", "0");
            task.setQuery(String.valueOf(query));
        }

        // already using a very specific search mode or the online service does not support 
        // search modes.
        if (task.getMode() == null || task.getMode().singleIsPerfect()) return;
        
        // check whether an ISBN is available.
        DcObject dco = task.getClient();
        
        if (dco == null) return;
        
        
        ISBN isbn = null;
        
        
        if (    dco.getModule().getIndex() == DcModules._BOOK && 
                (dco.isFilled(Book._N_ISBN13) || dco.isFilled(Book._J_ISBN10))) {
        
            String s = dco.isFilled(Book._N_ISBN13) ? 
                    (String) dco.getValue(Book._N_ISBN13) : (String) dco.getValue(Book._J_ISBN10);
            try {
                isbn = new ISBN(s);
            } catch (InvalidBarCodeException ibce) {
                logger.debug("Currently set ISBN/EAN is invalid: [" + s + "]");
            }
        }
        
        if (isbn == null) {
            isbn = new ISBN();
            isbn.parse(task.getQuery());
        }
        
        // If so, set the appropriate search mode
        if (isbn.isValid()) {
            if (task.getServer().getSearchModes() != null) {
                for (SearchMode m : task.getServer().getSearchModes()) {
                    if (m instanceof IsbnSearchMode) {
                        task.setMode(m);
                        task.setQuery(isbn.getIsbn13());
                        break;
                    }
                }
            }
        }
    }
}
