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

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.datacrow.client.console.GUI;
import org.datacrow.client.console.windows.DcDialog;
import org.datacrow.client.console.windows.DcFrame;
import org.datacrow.client.util.launcher.URLLauncher;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcTemplate;
import org.datacrow.core.plugin.Plugin;
import org.datacrow.core.resources.DcResources;

public class Help extends Plugin {

	private static final long serialVersionUID = -1064150561616268562L;

	public Help(DcObject dco, DcTemplate template, int viewIdx, int moduleIdx, int viewType) {
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
        Object o = e.getSource();
        
        while (!(o instanceof Window) && o != null)
            o = ((Component) o).getParent();
        
        if (o != null) {
        	String helpIndex = "https://datacrow.org/docs";
        	
            if (o instanceof DcFrame)
                helpIndex = ((DcFrame) o).getHelpIndex();
            else if (o instanceof DcDialog)
            	helpIndex = ((DcDialog) o).getHelpIndex();
            
            try {
                URL url = new URL(helpIndex);
                if (url != null) {
                	URLLauncher launcher = new URLLauncher(url);
                	launcher.launch();
                }
            } catch (Exception exp) {
                GUI.getInstance().displayErrorMessage(exp.toString());
            }
        }
    }
    
    @Override
    public ImageIcon getIcon() {
        return IconLibrary._icoHelp;
    }
    
    @Override
    public boolean isSystemPlugin() {
        return true;
    }

    @Override
    public KeyStroke getKeyStroke() {
        return KeyStroke.getKeyStroke("F1");
    }
    
    @Override
    public String getLabel() {
        return DcResources.getText("lblHelp");
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("tpHelp");
    }
}
