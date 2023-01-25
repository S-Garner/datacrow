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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.logging.log4j.Logger;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcLongTextField;
import org.datacrow.client.util.launcher.URLLauncher;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;

/**
 * Donation dialog which can be used to ask the user for a donation.
 * 
 * @author Robert Jan van der Waals
 */
public class DonateDialog extends DcFrame implements ActionListener {

    private static Logger logger = DcLogManager.getLogger(DonateDialog.class.getName());
    
    public DonateDialog() {
        super(DcResources.getText("lblDonate"), IconLibrary._icoDonate);
        buildDialog();

        setCenteredLocation();
    }

    private void buildDialog() {
        //**********************************************************
        //Donate panel
        //**********************************************************
        JPanel panelDonate = new JPanel();
        panelDonate.setLayout(Layout.getGBL());

        DcLongTextField donate = ComponentFactory.getLongTextField();
        donate.setText(DcResources.getText("msgDonateLargeText"));
        panelDonate.add(new JScrollPane(donate), Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));

        //**********************************************************
        //Actions
        //**********************************************************
        JPanel panelActions = new JPanel();
        
        JButton btNeedMoreTime = ComponentFactory.getButton(DcResources.getText("lblNeedMoreTime"), IconLibrary._icoCalendar);
        JButton btLeaveMeAlone = ComponentFactory.getButton(DcResources.getText("lblLeaveMeAlone"), IconLibrary._icoError);
        JButton btDonate = ComponentFactory.getButton(DcResources.getText("lblDonation"), IconLibrary._icoDonate);
        
        btNeedMoreTime.addActionListener(this);
        btLeaveMeAlone.addActionListener(this);
        btDonate.addActionListener(this);
        
        btNeedMoreTime.setActionCommand("needmoretime");
        btLeaveMeAlone.setActionCommand("leavemealone");
        btDonate.setActionCommand("donate");
        
        panelActions.add(btNeedMoreTime);
        panelActions.add(btLeaveMeAlone);
        panelActions.add(btDonate);

        //**********************************************************
        //Main panel
        //**********************************************************
        getContentPane().setLayout(Layout.getGBL());
        getContentPane().add(panelDonate,  Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(panelActions, Layout.getGBC(0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));

        pack();
        setSize(new Dimension(500,350));
        setResizable(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("needmoretime")) {
            close();
        } else if (ae.getActionCommand().equals("leavemealone")) {
            DcSettings.set(DcRepository.Settings.stAskForDonation, Boolean.FALSE);
            close();
        } else if (ae.getActionCommand().equals("donate")) {
            try {
                DcSettings.set(DcRepository.Settings.stAskForDonation, Boolean.FALSE);
                
                URLLauncher launcher = new URLLauncher(new URL("https://www.patreon.com/user?u=84210754"));
                launcher.launch();
                
                close();
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }
}
