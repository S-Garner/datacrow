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

package org.datacrow.client.console.wizards.module;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JRadioButton;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.wizards.Wizard;
import org.datacrow.client.console.wizards.WizardException;
import org.datacrow.core.modules.DcMediaModule;
import org.datacrow.core.modules.DcMediaParentModule;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.modules.DcParentModule;
import org.datacrow.core.modules.xml.XmlModule;
import org.datacrow.core.resources.DcResources;

public class PanelChildModule extends ModuleWizardPanel {

    private final Map<Integer, JComponent> fields = new HashMap<Integer, JComponent>();
    
    private int selectedModule = -1;
    
    public PanelChildModule(Wizard wizard) {
        super(wizard);
        build();
    }

    @Override
    public Object apply() {
        XmlModule module = getModule();
        
        if (selectedModule == -1) {
            GUI.getInstance().displayMessage("msgSelectChildModuleFirst");
            return null;
        }
        
        module.setChildIndex(selectedModule);
        
        if (  !(module.getModuleClass().equals(DcMediaParentModule.class) || 
                module.getModuleClass().equals(DcParentModule.class))) {
            
            if (module.getModuleClass().equals(DcMediaModule.class))
                module.setModuleClass(DcMediaParentModule.class);
            else 
                module.setModuleClass(DcParentModule.class);
        }
        
        return module;
    }
    
    @Override
    public void setModule(XmlModule module) {
        super.setModule(module);
        
        JRadioButton rb = (JRadioButton) fields.get(module.getIndex());
        rb.setVisible(false);
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgSelectChildModule");
    }
    
    @Override
    public void cleanup() {
        fields.clear();
    }     
    
    private void build() {
        setLayout(Layout.getGBL());
        
        final ButtonGroup bg = new ButtonGroup();
        class ModuleSelectionListener implements ItemListener {
            @Override
            public void itemStateChanged(ItemEvent ev) {
                String command = bg.getSelection().getActionCommand();
                selectedModule = Integer.parseInt(command);
                try {
                    getWizard().finish();
                } catch (WizardException wi) {
                    GUI.getInstance().displayWarningMessage(wi.getMessage());
                }
            }
        } 

        int y = 0;
        int x = 0;
        
        JRadioButton rb;
        for (DcModule module : DcModules.getAllModules()) {
            
            if (!module.isAbstract() &&
                 module.isTopModule() && 
                !module.isParentModule() && !module.isChildModule()) {

                rb = ComponentFactory.getRadioButton(
                        module.getName(), module.getIcon16(), "" + module.getIndex());

                fields.put(module.getIndex(), rb);
                
                rb.addItemListener(new ModuleSelectionListener());
                bg.add(rb);
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