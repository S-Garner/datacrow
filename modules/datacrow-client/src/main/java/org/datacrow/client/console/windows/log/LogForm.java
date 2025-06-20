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

package org.datacrow.client.console.windows.log;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.windows.DcFrame;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;

public class LogForm extends DcFrame {

    public LogForm() {
        super(DcResources.getText("lblLog"), IconLibrary._icoEventLog);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        build();

        setHelpIndex("dc.general.log");
    }
    
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b)
            toFront();
    }   
    
    private void build() {
        getContentPane().setLayout(Layout.getGBL());

        SwingUtilities.updateComponentTreeUI(LogPanel.getInstance());
        
        JTabbedPane tabbedPane = ComponentFactory.getTabbedPane();
        tabbedPane.addTab(DcResources.getText("lblLog"), IconLibrary._icoEventLog, LogPanel.getInstance());
        tabbedPane.addTab(DcResources.getText("lblSystemInformation"), IconLibrary._icoAbout, new SystemInformationPanel());
        
        add(tabbedPane, Layout.getGBC( 0, 1, 1, 1, 20.0, 50.0
           ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
            new Insets( 5, 0, 0, 0), 0, 0));        
        
        pack();

        setSize(DcSettings.getDimension(DcRepository.Settings.stLogFormSize));
        setResizable(true);
        
        setCenteredLocation();
    }
    
    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stLogFormSize, getSize());
        removeAll();
        dispose();
    }    
}
