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

package org.datacrow.core.log;

public class DcLogManager {
	
	private static final DcLogManager instance;
	
	private DcLogSystem logSys;
	
	static {
		instance = new DcLogManager();
	}
	
	private DcLogManager() {}
	
	public static DcLogManager getInstance() {
		return instance;
	}
	
	public void setLogSystem(DcLogSystem logSys) {
		this.logSys = logSys;
	}
	
	public DcLogger getLogger(String className) {
		return logSys.getLogger(className); 
	}
	
	public DcLogger getLogger(Class<?> clazz) {
		return logSys.getLogger(clazz.getName());
	}
	
	public void initialize(boolean debug) {
		logSys.initialize(debug);
	}
}
