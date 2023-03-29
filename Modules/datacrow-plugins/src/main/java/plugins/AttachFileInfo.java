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

package plugins;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.apache.logging.log4j.Logger;

import org.datacrow.client.console.GUI;
import org.datacrow.client.console.windows.BrowserDialog;
import org.datacrow.client.console.windows.itemforms.ItemForm;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.UserMode;
import org.datacrow.core.clients.IFileImportClient;
import org.datacrow.core.console.UIComponents;
import org.datacrow.core.fileimporter.FileImporter;
import org.datacrow.core.fileimporter.FileImporters;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcTemplate;
import org.datacrow.core.objects.Picture;
import org.datacrow.core.objects.ValidationException;
import org.datacrow.core.plugin.Plugin;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;
import org.datacrow.core.services.Region;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.utilities.filefilters.DcFileFilter;

public class AttachFileInfo extends Plugin implements IFileImportClient {
    
	private static final long serialVersionUID = 1L;

	private static final Logger logger = DcLogManager.getLogger(AttachFileInfo.class.getName());
    
    public AttachFileInfo(DcObject dco, DcTemplate template, int viewIdx, int moduleIdx, int viewType) {
        super(dco, template, viewIdx, moduleIdx, viewType);
    }
    
    @Override
    public boolean isAdminOnly() {
        return false;
    }
    
    @Override
    public boolean isAuthorizable() {
        return true;
    }   
    
    @Override
    public int getXpLevel() {
        return UserMode._XP_EXPERT;
    }
    
    @Override
	public boolean isEnabled() {
    	Connector connector = DcConfig.getInstance().getConnector();
		return connector.getUser().isEditingAllowed(getModule());
	}    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        DcObject dco;
        ItemForm form = null;
        if (GUI.getInstance().getRootFrame() instanceof ItemForm) {
            form = (ItemForm) GUI.getInstance().getRootFrame();
            dco = form.getItem();
        } else {
            DcModule module = getModule().getChild() != null ? getModule().getChild() : getModule();
            dco = GUI.getInstance().getSearchView(module.getIndex()).getCurrent().getSelectedItem();            
        }
        
        FileImporters importers = FileImporters.getInstance();
        if (!importers.hasImporter(dco.getModuleIdx())) return;
        FileImporter importer = importers.getFileImporter(dco.getModuleIdx());
        
        String[] extensions = importer.getSupportedFileTypes();
        DcFileFilter filter = extensions.length > 0 ? new DcFileFilter(extensions) : null;
        BrowserDialog dlg = new BrowserDialog(DcResources.getText("lblSelectFile"), filter);
        File file = dlg.showOpenFileDialog(form != null ? form : GUI.getInstance().getMainFrame(),
                dco.getFilename() != null ? new File(dco.getFilename()) : null );
        String filename = file != null ? file.toString() : null;
        
        if (filename != null && filename.length() > 0) {
            // overwrite empty information
            importer.setClient(this);
            DcObject dcoNew = importer.parse(filename, 0);
            if (dcoNew.getModule().getIndex() != dco.getModule().getIndex() && dcoNew.getChildren() != null)
                for (DcObject child : dcoNew.getChildren()) dcoNew = child;
            
            dco.copy(dcoNew, false, false);
            
            // overwrite parsed technical information, images and the file information
            for (DcField field : dcoNew.getFields()) {
                if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                    Picture picture = (Picture) dcoNew.getValue(field.getIndex());
                    if (picture != null)
                        dco.setValue(field.getIndex(), new DcImageIcon(picture.getImage()));
                } else if (field.getFieldType() == UIComponents._FILEFIELD ||
                           field.getFieldType() == UIComponents._FILELAUNCHFIELD) {
                    dco.setValue(field.getIndex(), filename);
                }
            }
            
            if (form != null) {
                form.setData(dco, true, false);
            } else {
                if (dco.isChanged()) {
                    try {
                    	DcConfig.getInstance().getConnector().saveItem(dco);
                    } catch (ValidationException ve) {
                        GUI.getInstance().displayWarningMessage(ve.getMessage());
                    }
                }
            }
                
        }
    }
    
    @Override
    public boolean isSystemPlugin() {
        return true;
    }
   
    @Override
    public KeyStroke getKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK);
    }
    
    @Override
    public ImageIcon getIcon() {
        return IconLibrary._icoImport;
    }
    
    @Override
    public String getLabelShort() {
        return DcResources.getText("lblReadFileInfo");
    }

    @Override
    public String getLabel() {
        return DcResources.getText("lblReadFileInfo");
    }

    @Override
    public void notifyError(Throwable e) {
        logger.error(e, e);
    }

    @Override
    public void notify(String message) {
        logger.info(message);
    }
    
    @Override
    public void notifyWarning(String msg) {
        logger.warn(msg);
    }

    @Override
    public DcObject getContainer() {
        return null;
    }

    @Override
    public int getDirectoryUsage() {
        return -1;
    }

    @Override
    public Region getRegion() {
        return null;
    }

    @Override
    public SearchMode getSearchMode() {
        return null;
    }

    @Override
    public IServer getServer() {
        return null;
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("tpAttachFileInfo");
    }

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public void notifyProcessed() {}

    @Override
    public void notifyTaskCompleted(boolean success, String taskID) {}

    @Override
    public void notifyTaskStarted(int taskSize) {}

    @Override
    public boolean useOnlineServices() {
        return false;
    }

    @Override
    public DcObject getStorageMedium() {
        return null;
    }
}