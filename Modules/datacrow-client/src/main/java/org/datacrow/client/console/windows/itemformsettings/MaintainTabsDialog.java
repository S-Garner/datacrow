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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.lists.DcTabList;
import org.datacrow.client.console.components.panels.NavigationPanel;
import org.datacrow.client.console.windows.DcDialog;
import org.datacrow.client.tabs.Tab;
import org.datacrow.client.tabs.Tabs;
import org.datacrow.core.DcRepository;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.definitions.DcFieldDefinition;

public class MaintainTabsDialog extends DcDialog implements ActionListener {
	
    private ItemFormSettingsDialog dlg;
    private DcTabList tabList;
    
    public MaintainTabsDialog(ItemFormSettingsDialog dlg) {
        super(dlg);
        setTitle(DcResources.getText("lblEditTabs"));
        
        this.dlg = dlg;
        this.tabList = new DcTabList();
        
        build();
        
        setModal(false);
        setSize(DcSettings.getDimension(DcRepository.Settings.stMaintainTabsDialogSize));
        setCenteredLocation();
    }
    
    protected void clear() {
        dlg = null;
        
        if (tabList != null) {
            tabList.clear();
            tabList = null;
        }
    }
    
    public void refresh() {
        tabList.clear();
        
        for (Tab tab : Tabs.getInstance().getTabs(dlg.getModule())) {
            tabList.add(tab);
        }
        
        dlg.refresh();
    }
    
    public void save() {
        
        List<Tab> tabs = tabList.getTabs();
        int order = 1;
        for (Tab tab : tabs) {
            tab.setOrder(order++);
        }
        
        Tabs.getInstance().setTabs(dlg.getModule(), tabs);        
        dlg.refresh();
        close();
    }
    
    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stMaintainTabsDialogSize, getSize());

        Tabs.getInstance().save();        

        super.close();
    }
    
    private void addTab() {
        CreateTabForm frm = new CreateTabForm(this, dlg.getModule());
        frm.setVisible(true);
    }
    
    private void deleteTab() {
        if (tabList.getSelectedIndex() == -1) {
            GUI.getInstance().displayMessage("msgTabDeleteNoRowSelected");
            return;
        }
        
        for (Tab tab : tabList.getSelectedTabs()) {
            
            // remove tab assignments
            DcModule module = DcModules.get(dlg.getModule());
            
            for (DcFieldDefinition def : module.getFieldDefinitions().getDefinitions()) {
                String fieldTab = def.getTab();
                fieldTab = fieldTab != null && fieldTab.startsWith("lbl") ? 
                        DcResources.getText(fieldTab) : fieldTab;

                if (tab.getName().equals(fieldTab)) {
                    def.setTab(null);
                }
            }
            
            // remove the tab
            Tabs.getInstance().remove(tab);
        }
        
        refresh();
    }
    
    protected List<Tab> getTabs() {
        return tabList.getTabs();
    }
    
    private void build() {
        
        //**********************************************************
        //Table Panel
        //**********************************************************
        JPanel panelTabList = new JPanel();
        panelTabList.setLayout(Layout.getGBL());
        
        JScrollPane sp = new JScrollPane(tabList);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        NavigationPanel panelNav = new NavigationPanel(tabList);
        panelTabList.add(sp,  Layout.getGBC( 0, 0, 1, 1, 20.0, 20.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        panelTabList.add(panelNav, Layout.getGBC(1, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets( 5, 5, 5, 5), 0, 0));
        
        //**********************************************************
        //Create Action Panel
        //**********************************************************
        JPanel panelActions = new JPanel();
        
        JButton btAdd = ComponentFactory.getButton(DcResources.getText("lblAdd"));
        JButton btDelete = ComponentFactory.getButton(DcResources.getText("lblDelete"));
        JButton btSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
        JButton btClose = ComponentFactory.getButton(DcResources.getText("lblClose"));

        btAdd.addActionListener(this);
        btAdd.setActionCommand("addTab");
        btDelete.addActionListener(this);
        btDelete.setActionCommand("deleteTab");
        btSave.addActionListener(this);
        btSave.setActionCommand("save");
        btClose.addActionListener(this);
        btClose.setActionCommand("close");

        panelActions.add(btAdd);
        panelActions.add(btDelete);
        panelActions.add(btSave);
        panelActions.add(btClose);
        
        //**********************************************************
        //Main Panel
        //**********************************************************
        getContentPane().setLayout(Layout.getGBL());
        
        getContentPane().add(panelTabList,  Layout.getGBC( 0, 0, 1, 1, 30.0, 30.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        getContentPane().add(panelActions,  Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));
        
        refresh();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("addTab"))
            addTab();
        else if (e.getActionCommand().equals("deleteTab"))
            deleteTab();
        else if (e.getActionCommand().equals("close"))
            close();
        else if (e.getActionCommand().equals("save"))
            save();
    } 
}
