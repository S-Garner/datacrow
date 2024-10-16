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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcPicturePane;
import org.datacrow.core.DcConfig;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.utilities.CoreUtilities;

public class AboutDialog extends DcDialog {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(AboutDialog.class.getName());
    
    public AboutDialog() {
        super(GUI.getInstance().getRootFrame());

        buildDialog();

        setTitle("");
        Image img = IconLibrary._icoMain.getImage();
        img.flush();

        setCenteredLocation();
        setResizable(false);
    }

    private void buildDialog() {
        JTabbedPane tabbedPane = ComponentFactory.getTabbedPane();

        //**********************************************************
        //About panel
        //**********************************************************
        JPanel panelAbout = new JPanel();
        panelAbout.setLayout(Layout.getGBL());

        DcPicturePane about = new DcPicturePane(false);
        try {
        	BufferedImage icon = CoreUtilities.getScaledImage(
        			new DcImageIcon(DcConfig.getInstance().getInstallationDir() + "icons/credits.png"), 430, 430);
            about.setImageIcon(new DcImageIcon(icon));
            panelAbout.add(about, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
            new Insets(5, 5, 5, 5), 0, 0));
        } catch (Exception e) {
            logger.error("Could not load the about image", e);
        }

        //**********************************************************
        //Main panel
        //**********************************************************
        tabbedPane.addTab(DcConfig.getInstance().getVersion().getFullString(), IconLibrary._icoAbout, panelAbout);

        getContentPane().setLayout(Layout.getGBL());
        getContentPane().add(tabbedPane,   Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                                           GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                           new Insets(5, 5, 5, 5), 0, 0));

        pack();
        setSize(new Dimension(510,580));
    }
}
