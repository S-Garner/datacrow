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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.panels.FieldDefinitionPanel;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.Settings;

public class FieldDefinitionsDialog extends DcDialog implements ActionListener {


    private final FieldDefinitionPanel panelDefinitionsParent;
    private final FieldDefinitionPanel panelDefinitionsChild;

    private final DcModule module;
    
    public FieldDefinitionsDialog(DcModule module) {
        super(GUI.getInstance().getMainFrame());

        this.module = module;
        
        setIconImage(IconLibrary._icoFieldSettings.getImage());
        setHelpIndex("dc.settings.fields");

        panelDefinitionsParent = new FieldDefinitionPanel(module);
        panelDefinitionsChild = module.getChild() != null ? new FieldDefinitionPanel(module.getChild()) : null;
        
        buildDialog();

        setTitle(DcResources.getText("lblFieldSettings"));
        setModal(true);
    }

    @Override
    public void close() {
        Settings settings = module.getSettings();
        settings.set(DcRepository.ModuleSettings.stFieldSettingsDialogSize, getSize());

        panelDefinitionsParent.clear();
        
        if (panelDefinitionsChild != null)
        	panelDefinitionsChild.clear();

        super.close();
    }

    private void save() {
        panelDefinitionsParent.save();
        if (panelDefinitionsChild != null)
            panelDefinitionsChild.save();
    }

    private void buildDialog() {
        getContentPane().setLayout(Layout.getGBL());

        /***********************************************************************
         * ACTIONS PANEL
         **********************************************************************/
        JPanel panelActions = new JPanel();
        panelActions.setLayout(Layout.getGBL());

        JButton buttonSave = ComponentFactory.getButton(DcResources
                .getText("lblSave"));
        JButton buttonClose = ComponentFactory.getButton(DcResources
                .getText("lblClose"));

        buttonSave.addActionListener(this);
        buttonSave.setActionCommand("save");

        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");

        panelActions.add(buttonSave, Layout.getGBC(0, 0, 1, 4, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 5), 0, 0));
        panelActions.add(buttonClose, Layout.getGBC(1, 0, 1, 4, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        /***********************************************************************
         * MAIN PANEL
         **********************************************************************/

        JTabbedPane tp = ComponentFactory.getTabbedPane();
        tp.addTab(DcResources.getText("lblXFields", module.getLabel()),
                panelDefinitionsParent);

        if (module.getChild() != null) {
            tp.addTab(DcResources.getText("lblXFields", module.getChild()
                    .getLabel()), panelDefinitionsChild);
        }

        getContentPane().add(tp, Layout.getGBC(0, 0, 1, 1, 10.0, 10.0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                        new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(panelActions, Layout.getGBC(0, 1, 1, 1, 1.0, 1.0,
                        GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                        new Insets(5, 5, 5, 5), 0, 0));

        pack();

        Settings settings = module.getSettings();
        setSize(settings.getDimension(DcRepository.ModuleSettings.stFieldSettingsDialogSize));
        setCenteredLocation();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("save"))
            save();
    }


}
