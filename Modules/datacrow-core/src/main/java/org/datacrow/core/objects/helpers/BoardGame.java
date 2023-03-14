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

public class BoardGame extends DcMediaObject {

    private static final long serialVersionUID = 1L;
    
    public static final int _F_PUBLISHERS = 1;
    public static final int _G_ARTISTS = 2;
    public static final int _H_DESIGNERS = 3;
    public static final int _I_CATEGORIES = 4;
    public static final int _J_NR_OF_PLAYERS = 5;
    public static final int _K_PLAYTIME = 6;
    public static final int _L_MINIMUM_AGE = 7;
    public static final int _M_PICTURE = 8;
    public static final int _N_WEBPAGE = 9;
    public static final int _O_RULES_URL = 10;
    public static final int _P_EAN = 11;
    public static final int _Q_STATE = 12;
    
    public BoardGame() {
       super(DcModules._BOARDGAME);
    }
}

