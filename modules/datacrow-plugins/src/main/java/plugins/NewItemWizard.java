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

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.datacrow.client.console.windows.itemforms.ItemForm;
import org.datacrow.client.console.windows.security.UserForm;
import org.datacrow.client.console.wizards.item.ItemWizard;
import org.datacrow.core.DcConfig;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcTemplate;
import org.datacrow.core.plugin.Plugin;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;

public class NewItemWizard extends Plugin {

	private static final long serialVersionUID = 1L;

	public NewItemWizard(DcObject dco, DcTemplate template, int viewIdx, int moduleIdx, int viewType) {
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
    public void actionPerformed(ActionEvent e) {
        if (DcModules.getCurrent().getIndex() == DcModules._USER) {
            DcObject dco = getModule().getItem();
            ItemForm itemForm = new UserForm(false, dco, false, true);
            itemForm.setVisible(true);
        } else {
            ItemWizard wizard = new ItemWizard();
            wizard.setVisible(true);
        }
    }
    
    @Override
	public boolean isEnabled() {
    	Connector connector = DcConfig.getInstance().getConnector();
		return connector.getUser().isEditingAllowed(getModule());
	}    
    
    @Override
    public KeyStroke getKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK);
    }
    
    @Override
    public ImageIcon getIcon() {
        return IconLibrary._icoWizard;
    }
    
    @Override
    public boolean isSystemPlugin() {
        return true;
    }

    @Override
    public String getLabelShort() {
        return DcResources.getText("lblItemWizard");
    }
    
    @Override
    public String getLabel() {
        return DcResources.getText("lblItemWizard");
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("tpNewItemWizard");
    }     
}
