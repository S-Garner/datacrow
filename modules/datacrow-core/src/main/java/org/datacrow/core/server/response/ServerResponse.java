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

import java.io.Serializable;

public class ServerResponse implements Serializable, IServerResponse {

    public static final int _RESPONSE_DEFAULT = 0;
    public static final int _RESPONSE_ACTION = 1;
    public static final int _RESPONSE_APPLICATION_SETTINGS = 2;
    public static final int _RESPONSE_ERROR = 3;
    public static final int _RESPONSE_ITEM_KEYS = 4;
    public static final int _RESPONSE_ITEM_REQUEST = 5;
    public static final int _RESPONSE_ITEMS_REQUEST = 6;
    public static final int _RESPONSE_LOGIN = 7;
    public static final int _RESPONSE_MODULES = 8;
    public static final int _RESPONSE_SIMPLE_VALUES = 9;
    public static final int _RESPONSE_SQL = 10;
    public static final int _RESPONSE_VALUE_ENHANCERS = 11;
    public static final int _RESPONSE_MODULE_SETTINGS = 12;
	public static final int _RESPONSE_ATTACHMENT_ACTION = 13;
	public static final int _RESPONSE_ATTACHMENTS_LIST = 14;
	public static final int _RESPONSE_PICTURE_ACTION = 15;
	public static final int _RESPONSE_PICTURES_LIST = 16;
	public static final int _RESPONSE_PICTURE_SAVE_ACTION = 17;
	
    private final int type;
    
    public ServerResponse(int type) {
        this.type = type;
    }
    
    public int getType() {
        return type;
    }
}
