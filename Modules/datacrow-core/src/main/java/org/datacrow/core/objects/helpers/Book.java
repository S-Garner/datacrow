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

package org.datacrow.core.objects.helpers;

import org.apache.logging.log4j.Logger;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcMediaObject;
import org.datacrow.core.objects.ValidationException;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.isbn.ISBN;
import org.datacrow.core.utilities.isbn.InvalidBarCodeException;

public class Book extends DcMediaObject {

    private static final long serialVersionUID = 8019536746874888487L;

    private transient static Logger logger = DcLogManager.getLogger(Book.class.getName());
    
    public static final int _F_PUBLISHER  = 1;
    public static final int _G_AUTHOR = 2;
    public static final int _H_WEBPAGE = 3;
    public static final int _I_CATEGORY = 4;
    public static final int _J_ISBN10 = 5;
    public static final int _K_PICTUREFRONT = 6;
    public static final int _L_STATE = 7;
    public static final int _N_ISBN13 = 9;
    public static final int _O_SERIES = 10;
    public static final int _P_VOLUME_NR = 11;
    public static final int _Q_VOLUME_TITLE = 12;
    public static final int _R_STORAGE_MEDIUM = 13;
    public static final int _T_NROFPAGES = 15;
    public static final int _U_BINDING = 16;
    public static final int _V_EDITION_TYPE = 17;
    public static final int _W_EDITION_COMMENT = 18;
    public static final int _X_ORIGINAL_TITLE = 19;
    public static final int _Y_TRANSLATED_FROM = 20;
    
    public Book() {
       super(DcModules._BOOK);
    }

    @Override
    public void beforeSave() throws ValidationException {
        super.beforeSave();
        
        String s10 = (String) getValue(_J_ISBN10);
        String s13 = (String) getValue(_N_ISBN13);
        
        if ((CoreUtilities.isEmpty(s10) && !CoreUtilities.isEmpty(s13)) ||
            (CoreUtilities.isEmpty(s13) && !CoreUtilities.isEmpty(s10))) {
            
            try {
                ISBN isbn = new ISBN(CoreUtilities.isEmpty(s10) ? s13 : s10);
        
                setValue(Book._J_ISBN10, isbn.getIsbn10());
                setValue(Book._N_ISBN13, isbn.getIsbn13());
            } catch (InvalidBarCodeException ibce) {
                logger.error("Supplied barcodes are invalid", ibce);
            }            
        }  
    }
}
