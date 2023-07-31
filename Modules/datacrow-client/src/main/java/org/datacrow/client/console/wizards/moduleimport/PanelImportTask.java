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

package org.datacrow.client.console.wizards.moduleimport;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.panels.TaskPanel;
import org.datacrow.client.console.wizards.WizardException;
import org.datacrow.client.console.wizards.itemimport.ItemImporterTaskPanel;
import org.datacrow.core.clients.IModuleWizardClient;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.resources.DcResources;
import org.datacrow.server.migration.modules.ModuleImporter;

public class PanelImportTask extends ModuleImportWizardPanel implements IModuleWizardClient {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ItemImporterTaskPanel.class.getName());
    
    private final TaskPanel tp = new TaskPanel(TaskPanel._DUPLICATE_PROGRESSBAR);
    
    private ModuleImporter importer;
    
    public PanelImportTask() {
        build();
    }
    
    @Override
    public Object apply() throws WizardException {
        return getDefinition();
    }

    @Override
    public void cleanup() {
        if (importer != null) 
            importer.cancel();
        
        importer = null;
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgModuleImportHelp");
    }
    
    @Override
    public void onActivation() {
        if (getDefinition() != null && getDefinition().getFile() != null)
            start();
    }

    @Override
    public void onDeactivation() {
        cancel();
    }

    private void start() {
        ImportDefinition def = getDefinition();
        
        if (importer != null)
            importer.cancel();
        
        importer = new ModuleImporter(def.getFile());
        
        try { 
            importer.start(this);
        } catch (Exception e ) {
            notify(e.getMessage());
            logger.error(e, e);
        }
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        add(tp, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets( 5, 5, 5, 5), 0, 0));
    }
    
    private void cancel() {
        if (importer != null) 
            importer.cancel();
        
        notifyTaskCompleted(false, null);
    }    
    
    @Override
    public void notifyNewTask() {
        tp.clear();
    }
    
    @Override
    public void notifyStartedSubProcess(int count) {
        tp.initializeSubTask(count);
    }

    @Override
    public void notifyProcessed() {
        tp.updateProgressTask();
    }

    @Override
    public void notifySubProcessed() {
        tp.updateProgressSubTask();
    }

    @Override
    public void notify(String msg) {
    	tp.addMessage(msg);
    }

    @Override
    public void notifyWarning(String msg) {
    	notify(DcResources.getText("msgModuleImportError", msg));
    }

    @Override
    public void notifyError(Throwable e) {
        logger.error(e, e);
        notify(DcResources.getText("msgModuleImportError", e.toString()));
    }
    
    @Override
    public boolean askQuestion(String msg) {
        return GUI.getInstance().displayQuestion(msg);
    }

    @Override
    public void notifyTaskCompleted(boolean success, String taskID) {
    	notify(DcResources.getText("msgModuleImportFinished"));
    }

    @Override
    public void notifyTaskStarted(int taskSize) {
    	tp.initializeTask(taskSize);   
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
