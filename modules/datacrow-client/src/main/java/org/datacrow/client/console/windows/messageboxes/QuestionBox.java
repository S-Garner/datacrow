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

import javax.swing.JButton;
import javax.swing.JDialog;
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

public class QuestionBox extends DcDialog implements ActionListener {

    private final DcHtmlEditorPane textMessage = ComponentFactory.getHtmlEditorPane();;
    private final JButton buttonYes = ComponentFactory.getButton(DcResources.getText("lblYes"));
    private final JButton buttonNo = ComponentFactory.getButton(DcResources.getText("lblNo"));
    
    private boolean affirmative = false;
    
    public QuestionBox(String message) {
        super(GUI.getInstance().getRootFrame());
        init(message);
    }

    public QuestionBox(String message, JFrame parent) {
        super(parent);
        init(message);
    }

    public boolean isAffirmative() {
        return affirmative;
    }
    
    private void init(String message) {
        buildDialog();
        textMessage.setText(message);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        this.pack();
        this.setModal(true);

        setCenteredLocation();

        buttonYes.requestFocus();
    }

    private void buildDialog() {
        this.setResizable(false);
        this.getContentPane().setLayout(Layout.getGBL());
        
        textMessage.setEditable(false);
                
        JScrollPane scrollIn = new JScrollPane(textMessage);
        scrollIn.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollIn.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollIn.setPreferredSize(new Dimension(400,120));
        scrollIn.setMinimumSize(new Dimension(400,120));
        scrollIn.setBorder(null);

        textMessage.setBackground(scrollIn.getBackground());
        
        buttonYes.addActionListener(this);
        buttonYes.setActionCommand("confirm");
        buttonNo.addActionListener(this);
        buttonNo.setActionCommand("cancel");
        
        JPanel panelAction = new JPanel();
        panelAction.add(buttonYes);
        panelAction.add(buttonNo);

        setIconImage(IconLibrary._icoQuestion.getImage());
        
        if (DcResources.isInitialized())
            setTitle(DcResources.getText("lblQuestion"));

        this.getContentPane().setLayout(Layout.getGBL());

        this.getContentPane().add(scrollIn,    Layout.getGBC( 0, 0, 1, 1, 90.0, 90.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelAction, Layout.getGBC( 0, 1, 1, 1, 0.0, 0.0
               ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));

        this.pack();
        this.setModal(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("confirm")) {
            affirmative = true;
            close();
        } else if (e.getActionCommand().equals("cancel")) {
            affirmative = false;
            close();
        }
    }
}