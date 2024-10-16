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

package org.datacrow.core.server.response;

import java.util.ArrayList;
import java.util.List;

import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.modules.DcPropertyModule;

public class ServerModulesRequestResponse extends ServerResponse {

	private static final long serialVersionUID = 1L;
	
	private final List<DcModule> modules;
	private final List<DcPropertyModule> propertyBaseModules;

	public ServerModulesRequestResponse() {
	    super(_RESPONSE_MODULES);
	    
		propertyBaseModules = new ArrayList<DcPropertyModule>(DcModules.getPropertyBaseModules());
		modules = new ArrayList<DcModule>(DcModules.getAllModules());
	}

	public List<DcModule> getModules() {
		return modules;
	}

	public List<DcPropertyModule> getPropertyBaseModules() {
		return propertyBaseModules;
	}
}
