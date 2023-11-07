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

package org.datacrow.client.console.components.panels;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.windows.FieldSelectorDialog;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.Settings;

public class OnlineServiceSettingsPanel extends JPanel implements ActionListener {
    
    private static final FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
    
    private final JButton buttonSettings = ComponentFactory.getIconButton(IconLibrary._icoSettings);
    private final JCheckBox checkOverwrite = ComponentFactory.getCheckBox(DcResources.getText("lblOverwriteExistingValues"));
    private final JCheckBox checkAutoAdd = ComponentFactory.getCheckBox(DcResources.getText("lblAutoAddPerfectMatch"));
    private final JCheckBox checkUseOriginalSettings = ComponentFactory.getCheckBox(DcResources.getText("lblMassUpdateUseOriginalSettings"));
    private final JCheckBox checkAlwaysUseFirstResult = ComponentFactory.getCheckBox(DcResources.getText("lblMassUpdateAlwaysUseFirst"));
    
    private final int module;

    private boolean updateMode = false;
    private final boolean allowAutoAddSelection;
    private final boolean allowOriginalSettingsSelection;
    private final boolean allowFirstIsBestMode;

    private JFrame parent;
    
    public OnlineServiceSettingsPanel(JFrame parent,
                                      boolean allowAutoAddSelection,
                                      boolean updateMode,
                                      boolean allowOriginalSettingsSelection,
                                      boolean allowFirstIsBestMode,
                                      int module) {
        this.module = module;
        this.parent = parent;
        this.updateMode = updateMode;
        
        this.allowOriginalSettingsSelection = allowOriginalSettingsSelection;
        this.allowAutoAddSelection = allowAutoAddSelection;
        this.allowFirstIsBestMode = allowFirstIsBestMode;
        
        build(updateMode);
        
        Settings settings = DcModules.get(module).getSettings();
        setOverwrite(settings.getBoolean(DcRepository.ModuleSettings.stOnlineSearchOverwrite));
        setUseOriginalSettings(settings.getBoolean(DcRepository.ModuleSettings.stMassUpdateUseOriginalServiceSettings));
        setAlwaysUseFirstSettings(settings.getBoolean(DcRepository.ModuleSettings.stMassUpdateAlwaysUseFirst));
        
        if (allowAutoAddSelection)
            setAutoAdd(settings.getBoolean(DcRepository.ModuleSettings.stAutoAddPerfectMatch));
        
        if (updateMode)
            buttonSettings.setEnabled(isOverwriteAllowed());
    }

    public void save() {
        Settings settings = DcModules.get(module).getSettings();
        settings.set(DcRepository.ModuleSettings.stOnlineSearchOverwrite, checkOverwrite.isSelected());
        settings.set(DcRepository.ModuleSettings.stAutoAddPerfectMatch, checkAutoAdd.isSelected());
        settings.set(DcRepository.ModuleSettings.stMassUpdateUseOriginalServiceSettings, checkUseOriginalSettings.isSelected());
        settings.set(DcRepository.ModuleSettings.stMassUpdateAlwaysUseFirst, checkAlwaysUseFirstResult.isSelected());
    }
    
    private void setOverwrite(boolean b) {
        checkOverwrite.setSelected(b);
    }
    
    public boolean isOverwriteAllowed() {
        return checkOverwrite.isSelected();
    }
    
    private void setAutoAdd(boolean b) {
        checkAutoAdd.setSelected(b);
    }
    
    public boolean isAutoAddAllowed() {
        return checkAutoAdd.isSelected();
    }   
    
    private void setUseOriginalSettings(boolean b) {
        checkUseOriginalSettings.setSelected(b);
    }

    private void setAlwaysUseFirstSettings(boolean b) {
        checkAlwaysUseFirstResult.setSelected(b);
    }
    
    public void clear() {
        parent = null;
        removeAll();
    }
    
    private void build(boolean updateMode) {
        JPanel panel = new JPanel();
        panel.setLayout(layout);

        setLayout(Layout.getGBL());

        checkAutoAdd.setToolTipText(DcResources.getText("tpAutoAddPerfectMatch"));
        checkOverwrite.setToolTipText(DcResources.getText("tpOverwriteExistingValues"));
        checkUseOriginalSettings.setToolTipText(DcResources.getText("tpMassUpdateUseOriginalSettings"));
        checkAlwaysUseFirstResult.setToolTipText(DcResources.getText("tpMassUpdateAlwaysUseFirst"));
        
        if (updateMode) {
            checkOverwrite.addActionListener(this);
            checkOverwrite.setActionCommand("toggleSettings");
        }
        
        buttonSettings.setMnemonic('S');
        buttonSettings.addActionListener(this);
        buttonSettings.setActionCommand("openOverwriteSettingsDialog");
        
        if (updateMode)
            panel.add(checkOverwrite);
        else
            panel.add(ComponentFactory.getLabel(DcResources.getText("lblRetrieveValues")));
        
        panel.add(buttonSettings);
        
        add(panel, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
           ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0), 0, 0));

        if (allowAutoAddSelection)
            add(checkAutoAdd, Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 5, 0, 0), 0, 0));  
        
        if (allowOriginalSettingsSelection) {
            add(checkUseOriginalSettings, Layout.getGBC( 0, 4, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(0, 5, 0, 0), 0, 0));
        }
        
        if (allowFirstIsBestMode) {
            add(checkAlwaysUseFirstResult, Layout.getGBC( 0, 5, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(0, 5, 0, 0), 0, 0));
        }
        
        setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblOnlineServiceSettings")));        
    }
    
    private void openOverwriteSettingsDlg() {
        String settingsKey = updateMode ?
                             DcRepository.ModuleSettings.stOnlineSearchFieldOverwriteSettings :
                             DcRepository.ModuleSettings.stOnlineSearchRetrievedFields;
        
        FieldSelectorDialog dlg = new FieldSelectorDialog(parent, module, settingsKey);
        dlg.setVisible(true);
    }    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("openOverwriteSettingsDialog")) {
            openOverwriteSettingsDlg();
        } else if (e.getActionCommand().equals("toggleSettings")) {
            if (updateMode)
                buttonSettings.setEnabled(checkOverwrite.isSelected());
            else
                buttonSettings.setEnabled(true);            
        }
    }
}
