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

package org.datacrow.client.console.windows.help;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.net.URLClassLoader;

import javax.help.DefaultHelpBroker;
import javax.help.HelpSet;
import javax.swing.JFrame;

import org.apache.logging.log4j.Logger;
import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.util.Utilities;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;

public class HelpDialog {

    private static String helpIndex = "dc.general.introduction";

    private static Logger logger = DcLogManager.getLogger(HelpDialog.class.getName());
    
    public static void setHelpIndex(String helpIndex) {
    	HelpDialog.helpIndex = helpIndex;
    }

	public HelpDialog(Window window) {
		try {
			ClassLoader cl = new URLClassLoader(
			    new URL[] {new URL(
				    "file:///" + 
					DcConfig.getInstance().getInstallationDir().replace('\\', '/') + 
					"help/")});
			
			String language = DcSettings.getSetting(DcRepository.Settings.stLanguage).getValueAsString();
			
			java.net.URL hsURL = javax.help.HelpSet.findHelpSet(cl, "en/jHelpSet.hs");
			if ("Portuguese".equals(language))
			    hsURL = javax.help.HelpSet.findHelpSet(cl, "pt/jHelpSet.hs"); 
            
            HelpSet hs = new HelpSet(cl, hsURL);
            hs.setTitle(DcResources.getText("lblHelp"));
            
            final DefaultHelpBroker hb = new DefaultHelpBroker();
            hb.setHelpSet(hs);
            hb.setActivationWindow(window);
            hb.setSize(DcSettings.getDimension(DcRepository.Settings.stHelpFormSize));
            hb.setLocation(Utilities.getCenteredWindowLocation(hb.getSize(), false));
            hb.setFont(ComponentFactory.getSystemFont());
            hb.setCurrentID("dc");
            hb.setDisplayed(true);
            
            Window helpWindow = hb.getWindowPresentation().getHelpWindow();
            if (helpWindow instanceof JFrame) {
                JFrame helpFrame = (JFrame) helpWindow;
                helpFrame.setIconImage(IconLibrary._icoMain.getImage());
                helpFrame.setTitle(DcResources.getText("lblDataCrowHelp"));
                
                helpWindow.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        if (hb != null)
                            DcSettings.set(DcRepository.Settings.stHelpFormSize, hb.getSize());
                    }
                });
            }
            
            hb.setCurrentID(helpIndex);

        } catch (Exception e) {
            logger.error(DcResources.getText("msgErrorOpeningHelp"), e);
            GUI.getInstance().displayErrorMessage("msgErrorOpeningHelp");
            return;
		}
	}
}