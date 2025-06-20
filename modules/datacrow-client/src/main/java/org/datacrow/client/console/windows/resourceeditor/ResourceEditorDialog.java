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

package org.datacrow.client.console.windows.resourceeditor;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.windows.DcFrame;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;

public class ResourceEditorDialog extends DcFrame implements ActionListener {
    
    private final JTabbedPane tp = ComponentFactory.getTabbedPane();
    private final  Collection<LanguageResourcePanel> panels = new ArrayList<LanguageResourcePanel>();
    
    public ResourceEditorDialog() {
        super(DcResources.getText("lblResourceEditor"), IconLibrary._icoResourceEditor);
        build();
        
        pack();
        setSize(DcSettings.getDimension(DcRepository.Settings.stResourcesEditorViewSize));
        setCenteredLocation();
    }
    
    @Override
    public String getHelpIndex() {
        return "dc.tools.resourceeditor";
    }

    private void save() {
        for (LanguageResourcePanel panel : panels)
            panel.save();
        
        GUI.getInstance().getMainFrame().applySettings(true);
    }
    
    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stResourcesEditorViewSize, getSize());
        
        tp.removeAll();

        for (LanguageResourcePanel panel : panels)
            panel.clear();

        panels.clear();

        super.close();
    }
    
    private void addLanguage() {
        CreateLanguageDialog dlg = new CreateLanguageDialog(this);
        dlg.setVisible(true);
        String language = dlg.getSelectedLanguage();
        if (language != null) {
            LanguageResourcePanel lrp = new LanguageResourcePanel(language);
            tp.add(language, lrp);
            panels.add(lrp);
        }
    }
    
    private void installMenu() {
        JMenuBar mb = ComponentFactory.getMenuBar();
        JMenu menu = ComponentFactory.getMenu(DcResources.getText("lblEdit"));
        
        JMenuItem miCreate = ComponentFactory.getMenuItem(IconLibrary._icoAdd, DcResources.getText("lblAddLanguage"));
        miCreate.setActionCommand("addLanguage");
        miCreate.addActionListener(this);
        
        menu.add(miCreate);
        mb.add(menu);
        
        setJMenuBar(mb);
    }
    
    private void build() {
        
        getContentPane().setLayout(Layout.getGBL());

        //**********************************************************
        //Language tabs
        //**********************************************************
        LanguageResourcePanel lrp;
        for (String language : DcResources.getLanguages()) {
            lrp = new LanguageResourcePanel(language);
            tp.addTab(language, lrp);
            panels.add(lrp);
        }
        
        //**********************************************************
        //Action panel
        //**********************************************************
        JPanel panelActions = new JPanel();
        
        JButton buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        
        buttonSave.addActionListener(this);
        buttonSave.setActionCommand("save");
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        
        panelActions.add(buttonSave);
        panelActions.add(buttonClose);

        getContentPane().add(tp, Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        getContentPane().add(panelActions, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));
        
        installMenu();
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("save"))
            save();
        else if (ae.getActionCommand().equals("addLanguage"))
            addLanguage();
    } 
}
