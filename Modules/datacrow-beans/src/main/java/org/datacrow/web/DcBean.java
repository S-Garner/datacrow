/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.net                             *
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

package org.datacrow.web;

import java.io.Serializable;

import org.datacrow.web.bean.LoginBean;

import jakarta.inject.Inject;

public abstract class DcBean implements Serializable  {

	@Inject
	private LoginBean loginBean;
	
	private LoginBean getLoginBean() {
        return loginBean;
	}
	
	protected boolean isUserLoggedOn() {
	    LoginBean lb = getLoginBean();
	    return lb == null ? false : lb.isLoggedIn();
	}
	
	protected boolean isAuthorized(Object o) {
        LoginBean lb = getLoginBean();
        return lb == null ? false : lb.isAuthorized(o);
	}
	
	protected boolean isEditingAllowed(Object o) {
        LoginBean lb = getLoginBean();
        return lb == null ? false : lb.isEditingAllowed(o);
	}
}
