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

import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcMediaObject;

public class Comic extends DcMediaObject {

	private static final long serialVersionUID = 1L;
    
    public static final int _F_ISSUE_NUMBER = 1;
    public static final int _G_ISSUE_TYPE = 2;
    public static final int _H_SERIES = 3;
    public static final int _I_ISBN = 4;
    public static final int _J_BARCODE = 5;
    public static final int _K_FORMAT = 6;
    public static final int _L_URL1 = 7;
    public static final int _M_URL2 = 8;
    public static final int _N_URL3 = 9;
    public static final int _O_CHARACTERS = 10;
    public static final int _P_TEAMS = 11;
    public static final int _Q_PUBLISHERS = 12;
    public static final int _R_ARTISTS = 13;
    public static final int _S_AUTHOR = 14;
    public static final int _T_CATEGORIES = 15;
    public static final int _U_PUBLISHEDON = 16;
    
    public Comic() {
       super(DcModules._COMIC);
    }
}

