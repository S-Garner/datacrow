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

package org.datacrow.client.console.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.components.DcMenuItem;
import org.datacrow.client.console.components.DcPopupMenu;
import org.datacrow.client.console.views.View;
import org.datacrow.client.console.windows.BrowserDialog;
import org.datacrow.client.console.windows.IMergeItemsListener;
import org.datacrow.client.console.windows.MergeItemsDialog;
import org.datacrow.client.console.windows.drivemanager.DriveManagerSingleItemMatcher;
import org.datacrow.client.plugins.PluginHelper;
import org.datacrow.client.util.Utilities;
import org.datacrow.core.DcConfig;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.UserMode;
import org.datacrow.core.console.IView;
import org.datacrow.core.drivemanager.DriveManager;
import org.datacrow.core.drivemanager.FileInfo;
import org.datacrow.core.fileimporter.FileImporter;
import org.datacrow.core.fileimporter.FileImporters;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.modules.DcPropertyModule;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.ValidationException;
import org.datacrow.core.plugin.InvalidPluginException;
import org.datacrow.core.plugin.Plugin;
import org.datacrow.core.plugin.Plugins;
import org.datacrow.core.reporting.Reports;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.definitions.DcFieldDefinition;

public class ViewPopupMenu extends DcPopupMenu implements ActionListener, IMergeItemsListener {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ViewPopupMenu.class.getName());
    
    private DcObject dco;
    private final int viewIdx;
    
    public ViewPopupMenu(DcObject dco, int viewType, int viewIdx, int moduleIdx) {
        
    	this.dco = dco;
        this.viewIdx = viewIdx;
        
        DcModule current = DcModules.getCurrent();
        DcModule module = dco.getModule();
        if (viewType == View._TYPE_SEARCH && 
            !module.isChildModule() &&
            !(current.getIndex() == DcModules._CONTAINER && 
              dco.getModule().getIndex() != DcModules._CONTAINER)) {

            if (module.isAbstract())
                PluginHelper.add(this, "SaveSelected");
            
            PluginHelper.add(this, "OpenItem", module.getIndex(), viewType);
            PluginHelper.add(this, "EditItem", module.getIndex(), viewType);
            
            if (module.getIndex() != DcModules._USER)
                PluginHelper.add(this, "EditAsNew", null, dco, null, -1, module.getIndex(), viewType);
        }
        
        String filename = dco.getFilename();
        File file = !CoreUtilities.isEmpty(filename) ? new File(filename) : null;
        
        Connector connector = DcConfig.getInstance().getConnector();
        if (viewType == View._TYPE_SEARCH) {
            
            if (!current.isAbstract()) {
                if (dco.getModule().getParent() != null) {
                    // in case a child is selected, make sure its the child which is going to be deleted
                    // and not the parent (via the DcModules.getCurrent(), which returns the parent).
                    PluginHelper.add(this, "Delete", module.getIndex());
                } else if (current.getIndex() == DcModules._CONTAINER && dco.getModule().getIndex() != DcModules._CONTAINER) {
                    PluginHelper.add(this, "Delete", DcModules._ITEM);                
                } else {
                    // make sure the actual SELECTED module is used for deleting the item. otherwise, if
                    // the media module is selected, the item from the, for example, software module view
                    // is deleted.
                    PluginHelper.add(this, "Delete", DcModules.getCurrent().getIndex());
                }
            }
            
            if (!module.isSelectableInUI())  {
            	addSeparator();
				DcMenuItem miMerge = new DcMenuItem(
						DcResources.getText("lblMergeItems", current.getObjectNamePlural()));
				miMerge.setIcon(IconLibrary._icoMerge);
				miMerge.setActionCommand("merge");
				miMerge.addActionListener(this);
				add(miMerge);
            }
            
            if (file != null && connector.getUser().isAdmin() && dco.getModule().isFileBacked()) {
                
                JMenu menuFile = ComponentFactory.getMenu(IconLibrary._icoDriveManager, DcResources.getText("lblFile"));
                
                JMenuItem miDelete = ComponentFactory.getMenuItem(IconLibrary._icoDelete, DcResources.getText("lblDeleteFile"));
                miDelete.addActionListener(this);
                miDelete.setActionCommand("deleteFile");
                miDelete.setEnabled(file.exists());

                JMenuItem miMove = ComponentFactory.getMenuItem(DcResources.getText("lblMoveFile"));
                miMove.addActionListener(this);
                miMove.setActionCommand("moveFile");
                miMove.setEnabled(file.exists());

                JMenuItem miLocateHP = ComponentFactory.getMenuItem(IconLibrary._icoDriveScanner, DcResources.getText("lblLocateFile", DcResources.getText("lblMatchOnHashAndSize")));
                miLocateHP.addActionListener(this);
                miLocateHP.setActionCommand("locateFileHP");
                miLocateHP.setEnabled(!file.exists() && dco.isFilled(DcObject._SYS_FILEHASH) && dco.isFilled(DcObject._SYS_FILESIZE));
                
                JMenuItem miLocateMP = ComponentFactory.getMenuItem(IconLibrary._icoDriveScanner, DcResources.getText("lblLocateFile", DcResources.getText("lblMatchOnFilenameAndSize")));
                miLocateMP.addActionListener(this);
                miLocateMP.setActionCommand("locateFileMP");
                miLocateMP.setEnabled(!file.exists() && dco.isFilled(DcObject._SYS_FILESIZE));            

                JMenuItem miLocateLP = ComponentFactory.getMenuItem(IconLibrary._icoDriveScanner, DcResources.getText("lblLocateFile", DcResources.getText("lblMatchOnFilename")));
                miLocateLP.addActionListener(this);
                miLocateLP.setActionCommand("locateFileLP");
                miLocateLP.setEnabled(!file.exists());        
                
                menuFile.add(miDelete);
                menuFile.add(miMove);
                menuFile.add(miLocateHP);
                menuFile.add(miLocateMP);
                menuFile.add(miLocateLP);
                
                addSeparator();
                add(menuFile);
            }            
            
        } else {
            PluginHelper.add(this, "RemoveRow", current.getIndex());
            PluginHelper.add(this, "AddRow", current.getIndex());
        }   
        
        if (viewType == View._TYPE_SEARCH && 
            module.getIndex() == DcModules._USER &&
            connector.getUser().isAuthorized("SetPassword")) {

            addSeparator();
            PluginHelper.add(this, "SetPassword", "", dco, null, viewType, DcModules.getCurrent().getIndex(), viewType);
        }

        if (viewType == View._TYPE_SEARCH && !current.isAbstract()) {
            addSeparator();
            PluginHelper.add(this, "ItemExporterWizard", "", dco, null, viewIdx, dco.getModule().getIndex(), viewType);
        }
        
        if (viewType == View._TYPE_SEARCH) {
            Reports templates = new Reports();
            if (templates.hasReports(current.getIndex()))
                PluginHelper.add(this, "Report", "", dco, null, viewIdx, current.getIndex(), viewType);
        }
        
        // Music track is always sorted by track nr.
        if (viewType == View._TYPE_SEARCH && moduleIdx != DcModules._MUSIC_TRACK) {
            addSeparator();
            PluginHelper.add(this, "Sort", moduleIdx, viewType);
        }
        
        if (	viewType == View._TYPE_SEARCH && 
        		module.canBeLend() &&
        		connector.getUser().isAuthorized("Loan")) {
        	
            addSeparator();
            PluginHelper.add(this, "Loan");
        }

        addSeparator();

        JMenu menuAdmin = ComponentFactory.getMenu(IconLibrary._icoModuleTypeProperty16, DcResources.getText("lblAdministration"));
        
        Collection<DcPropertyModule> modules = new ArrayList<DcPropertyModule>(); 
        DcField field;
        DcPropertyModule mod;
        for (DcFieldDefinition definition : module.getFieldDefinitions().getDefinitions()) {
            field = module.getField(definition.getIndex());
            mod = DcModules.getPropertyModule(field);
            if (mod != null && !modules.contains(mod))
                modules.add(mod);
        }

        for (DcModule pm : modules) {
            try {
                Plugin plugin = Plugins.getInstance().get("ManageItem", dco, null, viewIdx,  pm.getIndex(), Plugin._VIEWTYPE_SEARCH);
                if (    plugin != null && connector.getUser().isAuthorized(plugin) &&
                        UserMode.isCorrectXpLevel(plugin.getXpLevel())) {
                    
                    JMenuItem item = ComponentFactory.getMenuItem(plugin);
                    item.setEnabled(plugin.isEnabled());
                    item.setIcon(plugin.getIcon());
                    
                    menuAdmin.add(item);
                }
            } catch (InvalidPluginException e) {
                logger.error(e, e);
            }
        }
        
        if (menuAdmin.getItemCount() > 0)
            add(menuAdmin);
        
        addSeparator();
        PluginHelper.add(this, "ViewSettings");
        
        if (viewType == View._TYPE_SEARCH) {
        	
            FileImporters importers = FileImporters.getInstance();
            FileImporter importer = importers.getFileImporter(module.getIndex());
            
            if (    importer != null && 
            		importer.allowReparsing() && 
            		module.getFileField() != null) { 
            	
                addSeparator();
                PluginHelper.add(this, "AttachFileInfo", moduleIdx, viewType);
            }
        }
        
        if (viewType == View._TYPE_SEARCH && !current.isAbstract()) {
            addSeparator();
            PluginHelper.add(this, "UpdateAll", moduleIdx, viewType);
            PluginHelper.add(this, "FindReplace", moduleIdx, viewType);
        }
        
        if (viewType == View._TYPE_SEARCH &&  file != null && dco.getModule().isFileBacked())
            PluginHelper.add(this, "FileLauncher", moduleIdx, viewType);
        
        Collection<Plugin> plugins = Plugins.getInstance().getUserPlugins(dco, viewIdx, module.getIndex(), viewType);
        for (Plugin plugin : plugins) {
            if (plugin.isShowInPopupMenu()) {
                addSeparator();
                add(ComponentFactory.getMenuItem(plugin));
            }
        }
    }
    
    private void locateFile(final int precision) {
        new Thread(new Runnable() { 
            @Override
            public void run() {
                DriveManagerSingleItemMatcher matcher = 
                    new DriveManagerSingleItemMatcher(dco, precision);
                matcher.start();
                try {
                    matcher.join();
                } catch (InterruptedException e) {
                    logger.error(e, e);
                }
                
                FileInfo info = matcher.getResult();
                if (info != null) {
                    Connector connector = DcConfig.getInstance().getConnector();
                    dco.setValue(dco.getFileField().getIndex(), info.getFilename());
                    
                    try {
                        connector.saveItem(dco);
                    } catch (ValidationException ve) {
                        logger.debug(ve, ve);
                    }
                }
            }
        }).start();        
    }
    
    public void mergeItems() {
    	IView view = GUI.getInstance().getSearchView(dco.getModuleIdx()).get(viewIdx);
        List<? extends DcObject> items = view.getSelectedItems();
        if (items.size() == 0) {
            GUI.getInstance().displayWarningMessage(DcResources.getText("msgMergeNoItemsSelected"));
        } else {
            MergeItemsDialog dlg = new MergeItemsDialog(items, dco.getModule());
            dlg.addListener(this);
            dlg.setVisible(true);
        }
    }
        

    @Override
    public void actionPerformed(ActionEvent e) {
        
        String filename = dco.getFilename();
        File file = !CoreUtilities.isEmpty(filename) ? new File(filename) : null;
        
        if (e.getActionCommand().equals("deleteFile")) {
            file.delete();
            dco.setValue(dco.getFileField().getIndex(), null);
            try {
                DcConfig.getInstance().getConnector().saveItem(dco);
            } catch (ValidationException ve) {
                logger.debug(ve, ve);
            }
        } else if (e.getActionCommand().equals("merge")) {
            mergeItems();
        } else if (e.getActionCommand().equals("locateFileHP")) {
            locateFile(DriveManager._PRECISION_HIGHEST);
        } else if (e.getActionCommand().equals("locateFileMP")) {
            locateFile(DriveManager._PRECISION_MEDIUM);
        } else if (e.getActionCommand().equals("locateFileLP")) {
            locateFile(DriveManager._PRECISION_LOWEST);
        } else if (e.getActionCommand().equals("moveFile")) {
            BrowserDialog dialog = new BrowserDialog(DcResources.getText("msgSelectnewLocation"), null);
            File newDir = dialog.showSelectDirectoryDialog(this, null);
        
            if (newDir != null) {
                try {
                    File newFile = new File(newDir, file.getName());
                    Utilities.rename(file, newFile, true);
                    dco.setValue(dco.getFileField().getIndex(), newFile.toString());
                    
                    try {
                        DcConfig.getInstance().getConnector().saveItem(dco);
                    } catch (ValidationException ve) {
                        logger.debug(ve, ve);
                    }
                } catch (IOException e1) {
                    logger.error(e1, e1);
                }
            }
        }
    }

	@Override
	public void notifyItemsMerged() {
		GUI.getInstance().getSearchView(dco.getModuleIdx()).refresh();
	}
}
