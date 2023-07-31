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

package org.datacrow.client.util.launcher;

import java.awt.Desktop;
import java.net.URL;

import org.datacrow.client.console.GUI;
import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.CoreUtilities;

public class URLLauncher extends Launcher {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(URLLauncher.class.getName());
	
	private final URL url;
	
	public URLLauncher(URL url) {
		super();
		this.url = url;
	}

	@Override
	public void launch() {

		boolean launched = false;
		
		String browserPath = DcSettings.getString(DcRepository.Settings.stBrowserPath);
		
		if (!CoreUtilities.isEmpty(browserPath)) {
			try {
				runCmd(new String[] {browserPath, url.toString()});
				launched = true;
			} catch (Exception e) {
			    logger.debug("Failed to launch "  + browserPath + " " + url.toString(), e);
			}
		}
		
		if (!launched) {
			Desktop desktop = getDesktop();
	        if (desktop != null) {
	            try {
	            	desktop.browse(url.toURI());
	            	launched = true;
	            } catch (Exception exp) {
	            	logger.debug("Could not launch URL using the Dekstop class [" + url + "]", exp);
	            	
	            	if (!CoreUtilities.isEmpty(browserPath))
	            	    GUI.getInstance().displayWarningMessage(DcResources.getText("msgCannotLaunchURLNoBrowserPath", url.toString()));
	            	else 
	            	    GUI.getInstance().displayWarningMessage(DcResources.getText("msgCannotLaunchURL", url.toString()));
	            }
	        }
		}
	}
}
