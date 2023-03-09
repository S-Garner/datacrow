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

import org.datacrow.client.console.GUI;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.console.IView;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcTemplate;
import org.datacrow.core.plugin.Plugin;
import org.datacrow.core.resources.DcResources;

public class FileLauncher extends Plugin {
	
	private static final long serialVersionUID = 3811899575126914470L;

	public FileLauncher(DcObject dco, DcTemplate template, int viewIdx, int moduleIdx, int viewType) {
        super(dco, template, viewIdx, moduleIdx, viewType);
    }    
    
    @Override
    public boolean isAdminOnly() {
        return false;
    }
    
    @Override
    public boolean isAuthorizable() {
        return false;
    }    
    
    @Override
    public void actionPerformed(ActionEvent e) {
    	DcModule module = getModule().getChild() != null ? getModule().getChild() : getModule();
    	
    	IView view = GUI.getInstance().getSearchView(module.getIndex()).getCurrent();
    	
    	if (view != null) {
            DcObject dco = view.getSelectedItem();
            if (dco == null) 
                return;
            else if (getModule().getIndex() == DcModules._MEDIA) 
            	dco.load(new int[] {DcObject._ID, dco.getFileField().getIndex()});
            
            new org.datacrow.client.util.launcher.FileLauncher(dco.getFilename()).launch();
    	}
    }
    
    @Override
    public boolean isSystemPlugin() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        DcModule module = getModule().getChild() != null ? getModule().getChild() : getModule();
        
        boolean fileExists = false;
        IView view = GUI.getInstance().getSearchView(module.getIndex()).getCurrent();
        if (view != null) {
            DcObject dco = view.getSelectedItem();
            if (dco != null)
                fileExists = dco.getFilename() != null && new File(dco.getFilename()).exists();
        }
        
        return fileExists;
    }

    @Override
    public KeyStroke getKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK);
    }
    
    @Override
    public ImageIcon getIcon() {
        return IconLibrary._icoOpenApplication;
    }

    @Override
    public String getLabel() {
        return DcResources.getText("lblStartProgram");
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("tpStartProgram");
    }     
}
