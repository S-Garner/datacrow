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
import org.datacrow.core.objects.DcObject;

public class ComicCharacter extends DcObject {

	private static final long serialVersionUID = 1L;
    
    public static final int _A_NAME = 1;
    public static final int _B_ALIASES = 2;
    public static final int _C_REALNAME = 3;
    public static final int _D_DESCRIPTION = 4;
    public static final int _E_URL = 5;
    public static final int _F_ENEMIES = 6;
    public static final int _G_FRIENDS = 7;
    public static final int _H_MOVIES = 8;
    public static final int _I_TEAMS = 9;
    public static final int _J_POWERS = 10;
    
    public ComicCharacter() {
       super(DcModules._COMICCHARACTER);
    }
}

