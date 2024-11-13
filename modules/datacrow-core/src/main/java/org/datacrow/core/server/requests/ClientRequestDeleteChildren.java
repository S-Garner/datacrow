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

package org.datacrow.core.server.requests;

import org.datacrow.core.security.SecuredUser;

public class ClientRequestDeleteChildren extends ClientRequest {
    
	private static final long serialVersionUID = 1L;

	private final String parentID;
	private final int moduleIdx;
    
    public ClientRequestDeleteChildren(SecuredUser su, int moduleIdx, String parentID) {
        super(ClientRequest._REQUEST_DELETE_CHILDREN, su);
        
        this.parentID = parentID;
        this.moduleIdx = moduleIdx;
    }

    public String getParentID() {
    	return parentID;
    }
    
    public int getModuleIdx() {
    	return moduleIdx;
    }
}