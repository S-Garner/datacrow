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

import org.datacrow.core.DcConfig;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.Loan;
import org.datacrow.core.server.Connector;

public class ContactPerson extends DcObject {

	private static final long serialVersionUID = 1L;
    
    public static final int _A_NAME = 1;
    public static final int _B_DESCRIPTION = 2;
    public static final int _C_PHOTO = 3;
    public static final int _D_CATEGORY = 4;
    public static final int _E_EMAIL = 5;
    public static final int _F_ADDRESS = 6;
    public static final int _G_PHONE_HOME = 7;
    public static final int _H_PHONE_WORK = 8;
    public static final int _J_CITY = 9;
    public static final int _K_COUNTRY = 10;
    
    public ContactPerson() {
        super(DcModules._CONTACTPERSON);
    }
    
    @Override
    public int getDefaultSortFieldIdx() {
        return ContactPerson._A_NAME;
    } 
    
    @Override
	public void afterDelete() {
		super.afterDelete();
		
		DcModule loanMod = DcModules.get(DcModules._LOAN);
		Connector conn = DcConfig.getInstance().getConnector();
		conn.executeSQL(
		        "DELETE FROM " + loanMod.getTableName() + " WHERE " + 
		        loanMod.getField(Loan._C_CONTACTPERSONID).getDatabaseFieldName() + " = '" + getID() + "'");
    }
}

