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

import javax.swing.ImageIcon;

import org.datacrow.client.console.windows.itemforms.ItemForm;
import org.datacrow.core.DcConfig;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.UserMode;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcTemplate;
import org.datacrow.core.objects.Picture;
import org.datacrow.core.plugin.Plugin;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;

public class EditAsNew extends Plugin {

	private static final long serialVersionUID = -3595178825935243669L;

	public EditAsNew(DcObject dco, DcTemplate template, int viewIdx, int moduleIdx, int viewType) {
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
	public boolean isEnabled() {
        Connector connector = DcConfig.getInstance().getConnector();
		return connector.getUser().isEditingAllowed(getModule());
	} 
    
    @Override
    public int getXpLevel() {
        return UserMode._XP_EXPERT;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        DcObject dco = getItem();
        DcModule module = dco.getModule();
        DcObject dcoCopy = module.getItem();
        dcoCopy.copy(dco, true, true);
        
        // mark images as new
        int[] fields = dco.getModule().getFieldIndices();
        for (int i = 0; i < fields.length; i++) {
            Object value = dcoCopy.getValue(fields[i]);
            if (value instanceof Picture) 
                ((Picture) value).isEdited(true);
        }
        
        if (dco.getChildren() != null) {
            for (DcObject child : dco.getChildren()) {
                DcObject childCopy = child.getModule().getItem();
                childCopy.copy(child, true, true);
                dcoCopy.addChild(childCopy);
            }
        }

        ItemForm form = new ItemForm(false, false, dcoCopy, false);
        form.setVisible(true);
        
        clear();
    }
    
    @Override
    public ImageIcon getIcon() {
        return IconLibrary._icoAdd;
    }
    
    @Override
    public boolean isSystemPlugin() {
        return true;
    }

    @Override
    public String getLabel() {
        return DcResources.getText("lblEditItemAsNew");
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("tpEditItemAsNew");
    }      
}
