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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.undo.UndoManager;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcLongTextField;
import org.datacrow.client.console.menu.TextEditMenu;
import org.datacrow.core.DcRepository;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;

public class TextDialog extends DcDialog implements ActionListener {

    private final DcLongTextField textArea = ComponentFactory.getTextArea();
    private final JPanel panelText = new JPanel();
    private final JPanel panelAction = new JPanel();

    protected final UndoManager undo = new UndoManager();
    
    private boolean success = true;
    private String text;
    
    public TextDialog(String s, boolean edit) {
        super(GUI.getInstance().getRootFrame());
        buildDialog(edit);

        textArea.setEditable(edit);
        textArea.setText(s);
        setSize(DcSettings.getDimension(DcRepository.Settings.stTextViewerSize));
        setCenteredLocation();

        if (edit) {
            JMenu editMenu = new TextEditMenu(textArea);
            JMenuBar mb = ComponentFactory.getMenuBar();
            mb.add(editMenu);
            setJMenuBar(mb);
        }

        setModal(true);
        setVisible(true);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getText() {
        return text;
    }
    
    public void clear() {
        text = null;
    }
    
    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stTextViewerSize, getSize());
        text = textArea.getText();
        super.close();
    }

    protected void buildDialog(boolean edit) {

        //**********************************************************
        //Text panel
        //**********************************************************
        panelText.setLayout(Layout.getGBL());

        JScrollPane pane = new JScrollPane(textArea);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        panelText.add(pane, Layout.getGBC( 0, 0, 1, 1, 40.0, 40.0
                     ,GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                      new Insets( 5, 5, 5, 5), 0, 0));

        //**********************************************************
        //Action panel
        //**********************************************************
        panelAction.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        JButton buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));

        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        buttonSave.addActionListener(this);
        buttonSave.setActionCommand("save");

        if (edit)
            panelAction.add(buttonSave);
        
        panelAction.add(buttonClose);

        //**********************************************************
        //Main panel
        //**********************************************************
        this.getContentPane().setLayout(Layout.getGBL());
        setTitle(DcResources.getText("lblTextViewer"));

        this.getContentPane().add(panelText,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                                 ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                  new Insets( 0, 0, 0, 0), 0, 0));
        this.getContentPane().add(panelAction,Layout.getGBC( 0, 1, 1, 1, 0.0, 0.0
                                 ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                                  new Insets( 0, 0, 0, 0), 0, 0));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            success = false;
        else if (ae.getActionCommand().equals("save"))
            success = true;

        close();
    }
}
