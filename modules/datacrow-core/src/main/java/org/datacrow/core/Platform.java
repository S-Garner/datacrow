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

package org.datacrow.core;

/**
 * Describes the platform on which Data Crow is running. 
 * 
 * @author Robert Jan van der Waals
 */
public final class Platform {
	
	private boolean isWin = false;
	private boolean isMac = false; 
	private boolean isLinux = false; 
	
	/**
	 * Creates a new instance and will gather all the necessary information 
	 * from the current Java VM and the Operation System.
	 */
	public Platform() {
		String os = System.getProperty("os.name");
		
		if (os != null) {
			isWin = os.startsWith("Windows");
			isMac = !isWin && os.startsWith("Mac");
			isLinux = os.startsWith("Linux");
		}
	}
	
    public final boolean isWin() {
    	return isWin;
    }
    
    public final boolean isMac() {
    	return isMac;
    }
    
    public final boolean isLinux() {
    	return isLinux;
    }      
}
