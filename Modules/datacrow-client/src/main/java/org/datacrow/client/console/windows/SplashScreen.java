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

package org.datacrow.client.console.windows;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JWindow;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcPictureField;
import org.datacrow.client.util.Utilities;
import org.datacrow.core.DcConfig;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.resources.DcResources;

public final class SplashScreen extends JWindow {

    private final JLabel status = ComponentFactory.getLabel("");

    public SplashScreen() {
        buildPanel();
    }

    public void setStatusMsg(String message) {
    	status.setText(message);
    }

	public void splash() {
		setVisible(true);
	}

    private void buildPanel() {
        //**********************************************************
        //Logo
        //**********************************************************
    	
    	// TODO: decideon architecture
        DcPictureField logo = new DcPictureField();
//        logo.setValue(new DcImageIcon(
//                new File(DcConfig.getInstance().getInstallationDir(), "icons/splashscreen.png")));
        logo.setPreferredSize(new Dimension(459,295));
        
        //**********************************************************
        //Status 
        //**********************************************************
        status.setText(DcResources.getText(
                "msgStartingDataCrow", DcConfig.getInstance().getVersion().toString()));
        status.setHorizontalAlignment(JLabel.CENTER);
        status.setVerticalAlignment(JLabel.CENTER);
        status.setFont(new Font("Tahoma", Font.BOLD, 13));
        status.setForeground(Color.WHITE);
        
        getContentPane().setBackground(Color.BLACK);
        status.setBackground(Color.BLACK);
        status.setMinimumSize(new Dimension(459, 25));
        status.setPreferredSize(new Dimension(459, 25));

        //**********************************************************
        //Main panel
        //**********************************************************
        getContentPane().setLayout(Layout.getGBL());

        // Build the panel
        this.getContentPane().add(   logo,   Layout.getGBC( 0, 0, 1, 1, 0.0, 0.0
                                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                                             new Insets( 2, 2, 2, 2), 0, 0));
        this.getContentPane().add(   status, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
        		                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
											 new Insets( 2, 0, 0, 0), 0, 0));
        pack();
        setLocation(Utilities.getCenteredWindowLocation(getSize(), true));
    }
}

