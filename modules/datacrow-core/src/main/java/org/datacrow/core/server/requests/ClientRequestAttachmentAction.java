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

import org.datacrow.core.attachments.Attachment;
import org.datacrow.core.security.SecuredUser;

public class ClientRequestAttachmentAction extends ClientRequest {
    
	private static final long serialVersionUID = 1L;
	
	public static final int _ACTION_DELETE_ATTACHMENT = 1;
	public static final int _ACTION_SAVE_ATTACHMENT = 2;
	public static final int _ACTION_LOAD_ATTACHMENT = 3;

	private final Attachment attachment;
	private final int actionType;
    
    public ClientRequestAttachmentAction(SecuredUser su, int actionType, Attachment attachment) {
        super(_REQUEST_ATTACHMENT_ACTION, su);
        
        this.attachment = attachment;
        this.actionType = actionType;
    }

    public Attachment getAttachment() {
    	return attachment;
    }
    
    public int getActionType() {
    	return actionType;
    }
}