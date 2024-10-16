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

public class Software extends DcMediaObject {

	private static final long serialVersionUID = 1L;
    
    public static final int _F_DEVELOPER = 1;
    public static final int _G_PUBLISHER  = 2;
    public static final int _H_PLATFORM = 3;
    public static final int _I_WEBPAGE = 4;
    public static final int _K_CATEGORIES = 6;
    public static final int _S_SERIALKEY = 14;
    public static final int _V_STATE = 17;
    public static final int _W_STORAGEMEDIUM = 18;
    public static final int _X_EAN = 19;
    public static final int _Y_VERSION = 20;
    public static final int _Z_LICENSE = 21;
    public static final int _AA_COOP = 22;
    public static final int _AB_MULTI = 23;
    
    public Software() {
       super(DcModules._SOFTWARE);
    }
}
