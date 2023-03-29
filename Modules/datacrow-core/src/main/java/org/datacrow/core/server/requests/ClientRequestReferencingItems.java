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

public class ClientRequestReferencingItems extends ClientRequest {

	private static final long serialVersionUID = 1L;

	private int moduleIdx;
	private String ID;

	public ClientRequestReferencingItems(SecuredUser su, int moduleIdx, String ID) {
		super(ClientRequest._REQUEST_REFERENCING_ITEMS, su);
		
		this.moduleIdx = moduleIdx;
		this.ID = ID;
	}
	
	public int getModuleIdx() {
        return moduleIdx;
    }

    public String getID() {
        return ID;
    }

    @Override
	public void close() {}
}
