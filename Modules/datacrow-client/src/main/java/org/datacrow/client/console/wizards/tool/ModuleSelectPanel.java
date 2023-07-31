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

package org.datacrow.client.console.wizards.tool;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.wizards.Wizard;
import org.datacrow.client.console.wizards.WizardException;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.resources.DcResources;

public class ModuleSelectPanel extends ToolSelectBasePanel {

    private int module = -1;
    
    private Map<Integer, JRadioButton> components = new HashMap<Integer, JRadioButton>();
    
    public ModuleSelectPanel(Wizard wizard) {
        super(wizard);
        build();
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgToolSelectModuleSelect");
    }

    @Override
    public Object apply() {
        if (module == -1) {
            GUI.getInstance().displayMessage("msgSelectModuleFirst");
            return null;
        }

        GUI.getInstance().getMainFrame().changeModule(module);
        
        Tool tool = getTool();
        tool.setModule(module);
        return tool;
    }

    public int getSelectedModule() {
        return module;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        
        if (components != null) {
            components.clear();
            components = null;
        }        
    }  
    
    protected boolean isModuleAllowed(DcModule module) {
        return module.isTopModule();
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        
        final ButtonGroup bg = new ButtonGroup();
        class ModuleSelectionListener implements MouseListener {
            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {
                String command = bg.getSelection().getActionCommand();
                module = Integer.parseInt(command);
                try {
                    
                    if (getWizard().isAtTheEnd())
                        getWizard().finish();
                    else
                        getWizard().next();
                    
                } catch (WizardException wi) {
                    GUI.getInstance().displayWarningMessage(wi.getMessage());
                }
            }
        } 

        int y = 0;
        int x = 0;
        
        JRadioButton rb;
        for (DcModule module : DcModules.getModules()) {
            if (module.isSelectableInUI() && module.isTopModule()) {
                rb = ComponentFactory.getRadioButton(module.getLabel(), module.getIcon32(), "" + module.getIndex());
                rb.addMouseListener(new ModuleSelectionListener());
                bg.add(rb);
                
                components.put(module.getIndex(), rb);
                
                add(rb, Layout.getGBC( x, y++, 1, 1, 1.0, 1.0
                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                    new Insets( 0, 5, 5, 5), 0, 0));
                
                if (y == 7) {
                    ++x;
                    y = 0;
                }
            }
        }
    }
}
