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

package org.datacrow.client.console.windows.security;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcNumberField;
import org.datacrow.client.console.components.DcPasswordField;
import org.datacrow.client.console.components.DcShortTextField;
import org.datacrow.client.console.windows.DcDialog;
import org.datacrow.core.ClientSettings;
import org.datacrow.core.DcConfig;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;
import org.datacrow.core.utilities.CoreUtilities;

public class LoginDialog extends DcDialog implements ActionListener, KeyListener {
    
    private final DcShortTextField fldLoginName = ComponentFactory.getShortTextField(255);
    private final DcPasswordField fldPassword = ComponentFactory.getPasswordField();
    private final DcShortTextField fldServerAddress = ComponentFactory.getShortTextField(255);
    private final DcNumberField fldApplicationServerPort = ComponentFactory.getNumberField();
    private final DcShortTextField fldImageServerAddress = ComponentFactory.getShortTextField(255);
    private final DcNumberField fldImageServerPort = ComponentFactory.getNumberField();
    
    private boolean canceled = false;
    
    public LoginDialog() {
        super((JFrame) null);
        build();
        pack();
        
        setTitle(DcResources.getText("lblLogin"));
        
        setIconImage(new DcImageIcon(new File(DcConfig.getInstance().getInstallationDir(), "icons/login.png")).getImage());
        
        toFront();
        setCenteredLocation();
        fldLoginName.requestFocusInWindow();
    }
    
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void close() {
        setVisible(false);
    }
    
    public String getLoginName() {
        return fldLoginName.getText();
    }

    public String getPassword() {
        return String.valueOf(fldPassword.getPassword());
    }

    private void login() {
        if (fldLoginName.getText().length() == 0) {
            GUI.getInstance().displayMessage("msgPleaseEnterUsername");
        } else {
            Connector conn = DcConfig.getInstance().getConnector();
            
            Long applicationServerPort = (Long) fldApplicationServerPort.getValue();
            if (applicationServerPort != null)
                conn.setApplicationServerPort(applicationServerPort.intValue());
            
            Long imageServerPort = (Long) fldImageServerPort.getValue();
            if (imageServerPort != null)
                conn.setImageServerPort(imageServerPort.intValue());
            
            String serverAddress = fldServerAddress.getText();
            conn.setServerAddress(serverAddress);

            String imageServerAddress = fldImageServerAddress.getText();
            conn.setImageServerAddress(imageServerAddress);

            DcConfig.getInstance().getClientSettings().setServerDetails(
            		serverAddress, 
            		applicationServerPort == null ? 0 : applicationServerPort.intValue(), 
            		(CoreUtilities.isEmpty(imageServerAddress) ? serverAddress : imageServerAddress),
            		imageServerPort == null ? 0 : imageServerPort.intValue());
            
            close();
        }
    }

    private void build() {
    	int y = 0;
    	
        getContentPane().setLayout(Layout.getGBL());
        getContentPane().add(ComponentFactory.getLabel(DcResources.getText("lblLoginname")),   
                 Layout.getGBC(0, y, 1, 1, 1.0, 1.0,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(10, 5, 5, 5), 0, 0));
        getContentPane().add(fldLoginName, Layout.getGBC(1, y++, 1, 1, 5.0, 1.0,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(10, 5, 5, 5), 0, 0));
        getContentPane().add(ComponentFactory.getLabel(DcResources.getText("lblPassword")),   
                 Layout.getGBC(0, y, 1, 1, 1.0, 1.0,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(fldPassword, Layout.getGBC(1, y++, 1, 1, 5.0, 1.0,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
         
        Connector connector = DcConfig.getInstance().getConnector();
        
        if (DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_CLIENT) {
             getContentPane().add(ComponentFactory.getLabel(DcResources.getText("lblServerAddress")),   
                     Layout.getGBC(0, y, 1, 1, 1.0, 1.0,
                     GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5, 5), 0, 0));
             getContentPane().add(fldServerAddress, Layout.getGBC(1, y++, 1, 1, 5.0, 1.0,
                     GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5, 5), 0, 0));
             getContentPane().add(ComponentFactory.getLabel(DcResources.getText("lblApplicationServerPort")),   
                     Layout.getGBC(0, y, 1, 1, 1.0, 1.0,
                     GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5, 5), 0, 0));
             getContentPane().add(fldApplicationServerPort, Layout.getGBC(1, y++, 1, 1, 5.0, 1.0,
                     GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5, 5), 0, 0));
             getContentPane().add(ComponentFactory.getLabel(DcResources.getText("lblImageServerAddress")),   
                     Layout.getGBC(0, y, 1, 1, 1.0, 1.0,
                     GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5, 5), 0, 0));
             getContentPane().add(fldImageServerAddress, Layout.getGBC(1, y++, 1, 1, 5.0, 1.0,
                     GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5, 5), 0, 0));
             getContentPane().add(ComponentFactory.getLabel(DcResources.getText("lblImageServerPort")),   
                     Layout.getGBC(0, y, 1, 1, 1.0, 1.0,
                     GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5, 5), 0, 0));
             getContentPane().add(fldImageServerPort, Layout.getGBC(1, y++, 1, 1, 5.0, 1.0,
                     GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5, 5), 0, 0));
             
             ClientSettings cs = DcConfig.getInstance().getClientSettings();
             
             String serverAddress = connector.getServerAddress();
             if (CoreUtilities.isEmpty(serverAddress))
            	 serverAddress = cs.getServerAddress();
             fldServerAddress.setText(serverAddress);
             
             String imageServerAddress = connector.getImageServerAddress();
             if (CoreUtilities.isEmpty(imageServerAddress))
            	 imageServerAddress = cs.getImageServerAddress();
             if (CoreUtilities.isEmpty(imageServerAddress))
            	 imageServerAddress = serverAddress;
             fldImageServerAddress.setText(imageServerAddress);             
             
             int applicationServerPort = connector.getApplicationServerPort();
             if (applicationServerPort <= 0)
            	 applicationServerPort = cs.getServerPort();
             fldApplicationServerPort.setValue(applicationServerPort);
             
             int imageServerPort = connector.getImageServerPort();
             if (imageServerPort <= 0)
            	 imageServerPort = cs.getImageServerPort();
             fldImageServerPort.setValue(imageServerPort);
         }
         
         JPanel panelActions = new JPanel();
         
         JButton btOk = ComponentFactory.getButton(DcResources.getText("lblOK"));
         JButton btCancel = ComponentFactory.getButton(DcResources.getText("lblCancel"));
         
         btOk.setActionCommand("ok");
         btCancel.setActionCommand("cancel");
         btOk.addActionListener(this);
         btCancel.addActionListener(this);
         
         fldPassword.addKeyListener(this);
         fldLoginName.addKeyListener(this);
         
         panelActions.add(btOk);
         panelActions.add(btCancel);
         
         getContentPane().add(panelActions, Layout.getGBC(0, y, 2, 1, 1.0, 1.0,
                 GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets(10, 5, 5, 0), 0, 0));
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("ok")) {
            login();
        } else if (ae.getActionCommand().equals("cancel")) {
            canceled = true;
            close();
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            login();
    }

    @Override
    public void keyPressed(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
}
