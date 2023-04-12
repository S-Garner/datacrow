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

package org.datacrow.synch.service.request;

import org.datacrow.core.server.requests.IClientRequest;

/**
 * @author RJ
 *
 */
public class ServiceRequest implements IClientRequest {
	
	private static final long serialVersionUID = 1L;

	private final int type;
	
	private final String username;
	private final String password;
	
	private String clientKey;
	
	public ServiceRequest(ServiceRequestType type,  String username, String password) {
		this.type = type.getValue();
		this.username = username;
		this.password = password;
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
