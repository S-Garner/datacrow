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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcPluginField;
import org.datacrow.client.console.wizards.Wizard;
import org.datacrow.client.console.wizards.WizardException;
import org.datacrow.core.DcConfig;
import org.datacrow.core.fileimporter.FileImporters;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.plugin.InvalidPluginException;
import org.datacrow.core.plugin.Plugin;
import org.datacrow.core.plugin.Plugins;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;

public class ToolSelectPanel extends ToolSelectBasePanel implements ActionListener {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ToolSelectPanel.class.getName());
    
    public ToolSelectPanel(Wizard wizard) {
        super(wizard);
        build();
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgSelectTheToolOfYourChoice");
    }
    
    @Override
    public void onActivation() {
        super.onActivation();

        removeAll();
        build();
        revalidate();
        repaint();
        
        getWizard().repaint();
    }

    @Override
    public Object apply() throws WizardException {
        Tool tool = getTool();
        return tool;
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }  
    
    private void addPlugin(Collection<Plugin> plugins, String key) {
        try {
            plugins.add(Plugins.getInstance().get(key, getTool().getModule()));
        } catch (InvalidPluginException ipe) {
            logger.error(ipe, ipe);
        }
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        
        Tool tool = getTool();
        DcModule module = DcModules.get(tool.getModule());
        
        if (module == null) return;
        
        int y = 0;
        int x = 0;

        Collection<Plugin> plugins = new ArrayList<Plugin>();
        addPlugin(plugins, "NewItemWizard");
        addPlugin(plugins, "CreateNew");
        
        if (FileImporters.getInstance().hasImporter(module.getIndex()))
            addPlugin(plugins, "FileImport");
        
        if (module.hasOnlineServices()) {
            addPlugin(plugins, "OnlineSearch");
            addPlugin(plugins, "MassUpdate");
        }
        
        DcPluginField fld;
        Connector connector = DcConfig.getInstance().getConnector();
        for (Plugin plugin : plugins) {
            if (connector.getUser().isAuthorized(plugin)) {
                fld = new DcPluginField(plugin);
                fld.addActionListener(this);
                add(fld, Layout.getGBC( x, y++, 1, 1, 1.0, 1.0
                   ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                    new Insets( 0, 5, 5, 5), 0, 0));
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        try {
            getWizard().finish();
        } catch (Exception e) {
            logger.debug("Error while finishing the tool select wizard", e);
        }
    }
}
