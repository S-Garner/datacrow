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

package org.datacrow.synch;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import org.datacrow.core.security.SecuredUser;

/**
 * @author RJ
 *
 */
public class SynchServerSessions {
	
	private static SynchServerSessions instance;
	
	private final Map<String, SecuredUser> tokenizedUsers = new HashMap<>();
    private final LinkedBlockingDeque<SynchServerSession> sessions = 
    		new  LinkedBlockingDeque<SynchServerSession>();
    
    static {
    	instance = new SynchServerSessions();
    }

	private SynchServerSessions() {}

    public static SynchServerSessions getInstance() {
    	return instance;
    }
    
    public SecuredUser getUser(String token) {
    	return tokenizedUsers.get(token);
    }
	
	public void addSession(SynchServerSession session) {
		sessions.add(session);
	}
	
	protected void close() {
		for (SynchServerSession session : sessions)
			session.closeSession();
		
		tokenizedUsers.clear();
		sessions.clear();
	}
}
