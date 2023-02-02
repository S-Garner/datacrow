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
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.logging.log4j.Logger;
import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcPictureField;
import org.datacrow.core.DcConfig;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.log.DcLogManager;

public class AboutDialog extends DcDialog {

    private static Logger logger = DcLogManager.getLogger(AboutDialog.class.getName());
    
    public AboutDialog() {
        super(GUI.getInstance().getRootFrame());

        buildDialog();

        setTitle("");
        Image img =IconLibrary._icoMain.getImage();
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

        DcPictureField about = ComponentFactory.getPictureField(false, false);
        try {
            about.setValue(new URL("file://" + DcConfig.getInstance().getInstallationDir() + "icons/credits.png"));
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
