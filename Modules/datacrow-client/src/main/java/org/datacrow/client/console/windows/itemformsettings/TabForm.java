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

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcIconSelectField;
import org.datacrow.client.console.components.DcShortTextField;
import org.datacrow.client.console.windows.DcFrame;
import org.datacrow.client.tabs.Tab;
import org.datacrow.client.tabs.Tabs;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;

public class TabForm extends DcFrame implements ActionListener {
    
    private final MaintainTabsDialog dlg;
    private final int module;
    private final Tab tab;
    
    private final DcShortTextField txtName = ComponentFactory.getShortTextField(255);
    private final DcIconSelectField fldIcon = ComponentFactory.getIconField();
    
    public TabForm(MaintainTabsDialog dlg, int module) {
    	this(dlg, module, null);
    }
    
    public TabForm(MaintainTabsDialog dlg, int module, Tab tab) {
    	
        super(	tab == null ? DcResources.getText("lblCreateTab") : DcResources.getText("lblEditTab"),
        		tab == null ? IconLibrary._icoAdd : IconLibrary._icoInformation);
        
        this.dlg = dlg;
        this.module = module;
        this.tab = tab;

        build();
        
        setData();
        
        setSize(DcSettings.getDimension(DcRepository.Settings.stCreateTabDialogSize));
    }    
    
    private void setData() {
        if (tab == null) {
        	fldIcon.setValue(new DcImageIcon(IconLibrary._icoInformation.getBytes()));
        } else {
        	fldIcon.setValue(tab.getIcon());
        	txtName.setValue(tab.getName());
        }    	
    }
    
    private void save() {
    	
    	if (tab == null) {
	        Tab newTab = new Tab(module, txtName.getText(), (DcImageIcon) fldIcon.getValue());
	        Tabs.getInstance().addTab(newTab);
    	} else {
    		tab.setName(txtName.getText());
    		tab.setIcon((DcImageIcon) fldIcon.getValue());
    	}

    	dlg.refresh();
        setVisible(false);
        close();
    }
    
    private void build() {
        // Actions panel
        JPanel panelActions = new JPanel();
        
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        JButton buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
        
        buttonClose.setActionCommand("close");
        buttonClose.addActionListener(this);
        
        buttonSave.setActionCommand("save");
        buttonSave.addActionListener(this);
        
        panelActions.add(buttonSave);
        panelActions.add(buttonClose);
        
        // info panel
        JPanel panelFields = new JPanel();
        panelFields.setLayout(Layout.getGBL());
        
        panelFields.add(ComponentFactory.getLabel(DcResources.getText("lblName")), 
                 Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
        panelFields.add(txtName, Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
        
        panelFields.add(ComponentFactory.getLabel(DcResources.getText("lblIcon")), 
                Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));
        panelFields.add(fldIcon, Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));

        // main panel
        getContentPane().setLayout(Layout.getGBL());
        getContentPane().add(panelFields, Layout.getGBC( 0, 0, 1, 1, 50.0, 50.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                             new Insets(0, 0, 0, 0), 0, 0));
        getContentPane().add(panelActions, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                            ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                             new Insets(0, 0, 0, 10), 0, 0));
        pack();
        setCenteredLocation();
    }
    
    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stCreateTabDialogSize, getSize());
        super.close();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("close"))
            close();
        else if (e.getActionCommand().equals("save"))
            save();
    }
}
