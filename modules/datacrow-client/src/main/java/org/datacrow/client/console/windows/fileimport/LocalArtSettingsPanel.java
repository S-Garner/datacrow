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

package org.datacrow.client.console.windows.fileimport;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcCheckBox;
import org.datacrow.client.console.components.DcLongTextField;
import org.datacrow.core.DcRepository;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.Settings;

public class LocalArtSettingsPanel extends JPanel {

    private final int module;
    
    private final DcCheckBox cbRecurse = ComponentFactory.getCheckBox(DcResources.getText("lblRecursiveDir"));
    private final DcCheckBox cbEnabled = ComponentFactory.getCheckBox(DcResources.getText("lblEnabled"));
    
    private final DcLongTextField txtWords = ComponentFactory.getLongTextField();
    
    public LocalArtSettingsPanel(int module) {
        this.module = module;
        build();
    }

    public void save() {
        Settings settings = DcModules.get(module).getSettings();
        settings.set(DcRepository.ModuleSettings.stImportLocalArt, cbEnabled.isSelected());
        settings.set(DcRepository.ModuleSettings.stImportLocalArtRecurse, cbRecurse.isSelected());
        settings.set(DcRepository.ModuleSettings.stImportLocalArtKeywords, txtWords.getText());
    }
    
    public void clear() {
        if (txtWords.getText().length() == 0) {
            GUI.getInstance().displayWarningMessage("msgPleaseEnterKeywords");
            return;
        } else {
            save();
        }
    }
    
    protected void build() {
        
        //**********************************************************
        //Help
        //**********************************************************
        DcLongTextField explanation = ComponentFactory.getLongTextField();
        explanation.setText(DcResources.getText("msgImportSettingsExplanation"));
        ComponentFactory.setUneditable(explanation);
        
        //**********************************************************
        //Input panel
        //**********************************************************
        JPanel pnlPatterns = new JPanel();
        pnlPatterns.setLayout(Layout.getGBL());
        pnlPatterns.setBorder(ComponentFactory.getTitleBorder(""));

        JScrollPane spWords = new JScrollPane(txtWords);
        
        pnlPatterns.add(spWords, Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));
        
        pnlPatterns.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblArtKeywords")));
        

        //**********************************************************
        //Main Panel
        //**********************************************************
        setLayout(Layout.getGBL());
        
        add(    explanation,   Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(15, 5, 5, 5), 0, 0));
        add(    cbEnabled,     Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets( 5, 5, 5, 5), 0, 0));
        add(    cbRecurse,     Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets( 5, 5, 5, 5), 0, 0));
        add(    pnlPatterns,  Layout.getGBC( 0, 4, 1, 1, 1.0, 20.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets( 5, 5, 5, 5), 0, 0));
        
        Settings settings = DcModules.get(module).getSettings();
        txtWords.setText(settings.getString(DcRepository.ModuleSettings.stImportLocalArtKeywords));
        cbEnabled.setSelected(settings.getBoolean(DcRepository.ModuleSettings.stImportLocalArt));
        cbRecurse.setSelected(settings.getBoolean(DcRepository.ModuleSettings.stImportLocalArtRecurse));
    }
}