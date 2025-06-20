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

package org.datacrow.client.console.windows.messageboxes;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcHtmlEditorPane;
import org.datacrow.client.console.windows.DcDialog;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.resources.DcResources;

public class MessageBox extends DcDialog implements ActionListener, KeyListener {

    public static final int _ERROR = 1;
    public static final int _WARNING = 2;
    public static final int _INFORMATION = 3;
	
    private final JPanel panel = new JPanel();
    
    private final DcHtmlEditorPane textMessage = ComponentFactory.getHtmlEditorPane();
    private final JButton buttonOk;

    public MessageBox(String message, int type) {
        this(GUI.getInstance().getRootFrame(), message, type);
    }
    
    public MessageBox(JFrame frame, String message, int type) {
        super(frame);
        
        buttonOk = ComponentFactory.getButton(DcResources.isInitialized() ? DcResources.getText("lblOK") : "Ok");
        
        build();
        setModal(true);

        textMessage.setText(message);
        textMessage.setCaretPosition(0);

        if (DcResources.isInitialized()) {
            setTitle(
                    type == _ERROR ? DcResources.getText("lblError") : 
                    type == _WARNING ? DcResources.getText("lblWarning") : 
                    DcResources.getText("lblNotification"));
        }
        
        setIconImage(
                type == _ERROR ? IconLibrary._icoError.getImage() : 
                type == _WARNING ? IconLibrary._icoWarning.getImage() : 
                IconLibrary._icoAbout.getImage());

        this.pack();
        this.toFront();

        setCenteredLocation();
        buttonOk.requestFocus();
    }

    private void build() {
        this.getContentPane().add(panel);

        this.setResizable(false);
        this.getContentPane().setLayout(Layout.getGBL());

        textMessage.setEditable(false);
        textMessage.setBackground(panel.getBackground());
        JScrollPane scrollIn = new JScrollPane(textMessage);
        scrollIn.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollIn.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollIn.setPreferredSize(new Dimension(400,120));
        scrollIn.setMinimumSize(new Dimension(400,120));
        scrollIn.setBorder(null);
        
        buttonOk.addActionListener(this);

        panel.setLayout(Layout.getGBL());
        
        this.getContentPane().add(scrollIn,    Layout.getGBC( 0, 0, 1, 1, 90.0, 90.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        this.getContentPane().add(buttonOk,    Layout.getGBC( 0, 1, 1, 1, 0.0, 0.0
               ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));
        
        this.getContentPane().add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        close();
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            close();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            close();

    }
    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            close();

    }
}
