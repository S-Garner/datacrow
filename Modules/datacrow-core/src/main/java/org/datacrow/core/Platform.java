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
	
	private boolean isWin;
	private boolean isMac; 
	private boolean isLinux; 
	
	private boolean isJavaSun;
	private boolean isJavaOracle;
	
	/**
	 * Creates a new instance and will gather all the necessary information 
	 * from the current Java VM and the Operation System.
	 */
	public Platform() {
		String os = System.getProperty("os.name");
		isWin = os.startsWith("Windows");
		isMac = !isWin && os.startsWith("Mac");
		isLinux = os.startsWith("Linux");
		
		isJavaSun = System.getProperty("java.vendor").toLowerCase().indexOf("sun") > -1;
		isJavaOracle = System.getProperty("java.vendor").toLowerCase().indexOf("oracle") > -1;
	}
	
	/**
	 * Indicates if the Java version is from Sun
     * @return  is Java Sun y/n
	 */
    public final boolean isJavaSun() {
        return isJavaSun;
    }
    
    /**
     * Indicates if the Java version is from Oracle
     * @return  is Java Oracle y/n
     */
    public final boolean isJavaOracle() {
        return isJavaOracle;
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
