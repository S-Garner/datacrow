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

package org.datacrow.client.console.windows.itemformsettings;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.windows.DcFrame;
import org.datacrow.client.tabs.Tab;
import org.datacrow.client.tabs.Tabs;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.definitions.DcFieldDefinition;
import org.datacrow.core.utilities.definitions.DcFieldDefinitions;

public class ItemFormSettingsDialog extends DcFrame implements ActionListener, ChangeListener {

	private final JTabbedPane tp = ComponentFactory.getTabbedPane();
	
    private final List<TabDesignPanel> panels = new ArrayList<TabDesignPanel>();
    private final DcModule module;
    
    public ItemFormSettingsDialog(DcModule module) {
        super(DcResources.getText("lblItemFormSettings"), IconLibrary._icoFormSettings);
        
        this.module = module;
        
        setHelpIndex("dc.settings.itemformsettings");
        setResizable(true);
        build();
    }
    
    public int getModule() {
        return module.getIndex();
    }
    
    public void save() {
        DcFieldDefinitions definitions = new DcFieldDefinitions(module.getIndex());
        
        for (TabDesignPanel panel : panels)
            panel.save(definitions);

        // takes care for any missing field definition
        for (DcFieldDefinition def : module.getFieldDefinitions().getDefinitions()) {
            if (!definitions.exists(def)) 
                definitions.add(def);
        }
        
        module.setSetting(DcRepository.ModuleSettings.stFieldDefinitions, definitions);
    }
    
    private void maintainTabs() {
        MaintainTabsDialog dlg = new MaintainTabsDialog(this);
        dlg.setVisible(true);
    }
    
    public void refresh() {
        tp.removeChangeListener(this);
        tp.removeAll();
        
        List<String> tabNames = new ArrayList<String>();
        List<Tab> tabs = Tabs.getInstance().getTabs(module.getIndex());
        for (Tab tab : tabs) 
            tabNames.add(tab.getName());
        
        TabDesignPanel panel;
        panels.clear();
        for (Tab tab : tabs) {
            panel = new TabDesignPanel(module, tab, tabs);
            panels.add(panel);
            tp.addTab(tab.getName(), tab.getIcon(), panel);
        }
        
        tp.addChangeListener(this); 
    }
    
    private void build() {
        getContentPane().setLayout(Layout.getGBL());
        
        //**********************************************************
        //Tab Pane
        //**********************************************************
        refresh();
        
        //**********************************************************
        //Action panel
        //**********************************************************
        JButton buttonTabs = ComponentFactory.getButton(DcResources.getText("lblTabs"));
        buttonTabs.addActionListener(this);
        buttonTabs.setActionCommand("maintainTabs");
        
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");

        JButton buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
        
        buttonSave.addActionListener(this);
        buttonSave.setActionCommand("save");
        
        JPanel panelActions = new JPanel();
        panelActions.add(buttonTabs);
        panelActions.add(buttonSave);
        panelActions.add(buttonClose);

        
        //**********************************************************
        //Main panel
        //**********************************************************
        getContentPane().add(tp,  Layout.getGBC( 0, 0, 1, 1, 20.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        getContentPane().add(panelActions,  Layout.getGBC( 0, 1, 1, 1, 0.0, 0.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 12), 0, 0));

        pack();
        setSize(DcSettings.getDimension(DcRepository.Settings.stItemFormSettingsDialogSize));
        setCenteredLocation();
    }

    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stItemFormSettingsDialogSize, getSize());
        panels.clear();
        super.close();
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("save"))
            save();
        else if (ae.getActionCommand().equals("maintainTabs"))
            maintainTabs();
        
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        JTabbedPane tp = (JTabbedPane) e.getSource();
        if (tp.getSelectedIndex() > -1) {
            TabDesignPanel panel = panels.get(tp.getSelectedIndex());
            panel.refresh();
        }
    }
}
