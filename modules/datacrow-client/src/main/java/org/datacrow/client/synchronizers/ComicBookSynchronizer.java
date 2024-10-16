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

package org.datacrow.client.synchronizers;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Comic;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.synchronizers.DefaultSynchronizer;
import org.datacrow.core.synchronizers.Synchronizer;
import org.datacrow.core.utilities.isbn.ISBN;
import org.datacrow.core.utilities.isbn.InvalidBarCodeException;

public class ComicBookSynchronizer extends DefaultSynchronizer {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ComicBookSynchronizer.class.getName());
    
    public ComicBookSynchronizer() {
        super(DcResources.getText("lblMassItemUpdate", DcModules.get(DcModules._COMIC).getObjectName()),
              DcModules._COMIC);
    }
    
    @Override
	public Synchronizer getInstance() {
		return new ComicBookSynchronizer();
	}
    
    @Override
    public String getHelpText() {
        return DcResources.getText("msgComicBookMassUpdateHelp");
    }

    @Override
    protected boolean matches(DcObject result, String searchString, int fieldIdx) {
        boolean matches = false;
        
        try {
            
            ISBN isbnInput = null;
            ISBN isbnSearch = null;
            try {
                // check if the search string is an ISBN
            	isbnInput = new ISBN(searchString);
            } catch (InvalidBarCodeException ibce) {
                logger.debug("Search term is not a valid ISBN/EAN [" + searchString + "]");
            }
            
            try {
                // check if the book contains a valid ISBN
                if (dco.isFilled(Comic._I_ISBN)) {
                	isbnSearch = new ISBN((String) dco.getValue(Comic._I_ISBN));
                }
            } catch (InvalidBarCodeException ibce) {
                logger.debug("The existing ISBN/EAN is invalid [" + dco.getValue(Comic._I_ISBN) + "]");
            }
            
            if (isbnInput != null && isbnSearch != null) {
                matches = isbnInput.getIsbn13().equals(isbnSearch.getIsbn13());
            }
        } catch (Exception e) {
            logger.error(e, e);
        }
        
        return matches ? true : super.matches(result, searchString, fieldIdx); 
    }
}
