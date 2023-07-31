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

import javax.swing.JButton;
import javax.swing.JPanel;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcPasswordField;
import org.datacrow.client.console.windows.DcDialog;
import org.datacrow.core.DcConfig;
import org.datacrow.core.objects.helpers.User;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;

public class SetPasswordDialog extends DcDialog implements ActionListener, KeyListener {
    
	private final DcPasswordField fldNewPassword = ComponentFactory.getPasswordField();
    private final User user;
    
    private boolean canceled = false;
    
    public SetPasswordDialog(User user) {
        super(GUI.getInstance().getMainFrame());
    
        this.user = user;
        
        build();
        pack();
        toFront();
        setCenteredLocation();
    }
    
    public boolean isCanceled() {
        return canceled;
    }

    private void changePassword() {
        Connector connector = DcConfig.getInstance().getConnector();
        connector.changePassword(user, String.valueOf(fldNewPassword.getPassword()));
        close();
    }

    private void build() {
         getContentPane().setLayout(Layout.getGBL());
         getContentPane().add(ComponentFactory.getLabel(DcResources.getText("lblNewPassword")),   
                 Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
         getContentPane().add(fldNewPassword, Layout.getGBC(1, 0, 1, 1, 1.0, 1.0,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
         
         JPanel panelActions = new JPanel();
         
         JButton btOk = ComponentFactory.getButton(DcResources.getText("lblOK"));
         JButton btCancel = ComponentFactory.getButton(DcResources.getText("lblCancel"));
         
         btOk.setActionCommand("ok");
         btCancel.setActionCommand("cancel");
         btOk.addActionListener(this);
         btCancel.addActionListener(this);
         
         fldNewPassword.addKeyListener(this);
         
         panelActions.add(btOk);
         panelActions.add(btCancel);
         
         getContentPane().add(panelActions, Layout.getGBC(0, 3, 2, 1, 1.0, 1.0,
                 GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets(0, 0, 0, 0), 0, 0));
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("ok")) {
            changePassword();
        } else if (ae.getActionCommand().equals("cancel")) {
            canceled = true;
            close();
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            changePassword();
    }

    @Override
    public void keyPressed(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
}