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

public class ClientRequestRemoveReferenceTo extends ClientRequest {

	private static final long serialVersionUID = 1L;
	
	private final String id;
	private final int moduleIdx;

    public ClientRequestRemoveReferenceTo(SecuredUser su, int moduleIdx, String id) {
        super(ClientRequest._REQUEST_REMOVE_REFERENCES_TO, su);
        
        this.moduleIdx = moduleIdx;
        this.id = id;
    }
    
	public String getId() {
		return id;
	}

	public int getModuleIdx() {
		return moduleIdx;
	}

	@Override
	public void close() {}
}
