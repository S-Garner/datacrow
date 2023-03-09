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

package org.datacrow.synch.request;

import java.io.Serializable;

import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.server.requests.IClientRequest;

public class SynchClientRequest implements IClientRequest, Serializable {
	
	public static final int _REQUEST_LOGIN = 0;
	public static final int _REQUEST_MODULES = 1;

	private int type;
	
	private String clientKey;
	protected String username;
	protected String password;
	
	public SynchClientRequest(int type, SecuredUser su) {
		this.type = type;
		
		if (su != null) {
			this.clientKey = su.getUser().getID();
			this.username = su.getUsername();
			this.password = su.getPassword();
		}
	}
	
	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getClientKey() {
		return clientKey;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public void close() {}
}
