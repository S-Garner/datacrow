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
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import org.datacrow.client.console.GUI;
import org.datacrow.client.console.views.View;
import org.datacrow.client.console.windows.itemforms.ItemForm;
import org.datacrow.core.DcConfig;
import org.datacrow.core.console.IView;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcTemplate;
import org.datacrow.core.plugin.Plugin;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;

public class AddChild extends Plugin {

	private static final long serialVersionUID = 1647446110383897403L;

	public AddChild(DcObject dco, DcTemplate template, int viewIdx, int moduleIdx, int viewType) {
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
	public boolean isEnabled() {
    	Connector connector = DcConfig.getInstance().getConnector();
		return connector.getUser().isEditingAllowed(getModule());
	}    
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        DcModule module = getModule().getChild();
        IView view = 
                getViewType() == _VIEWTYPE_SEARCH ? 
                		GUI.getInstance().getSearchView(module.getIndex()).getCurrent() : 
                		GUI.getInstance().getInsertView(module.getIndex()).getCurrent();
            
        String parentID = view.getParentID();
        if (parentID != null) {
            DcObject dco = module.getItem();
            dco.setIDs();
            dco.setValue(dco.getParentReferenceFieldIndex(), parentID);
            if (view.getType() == View._TYPE_SEARCH) {
                ItemForm frm = new ItemForm(false, false, dco, true);
                frm.setVisible(true);
            } else {
                List<DcObject> children = new ArrayList<DcObject>();
                children.add(dco);
                view.add(children);
                view.loadChildren();
            }
        } else {
            GUI.getInstance().displayWarningMessage("msgAddSelectParent");
        }
    }
    
    @Override
    public boolean isSystemPlugin() {
        return true;
    }

    @Override
    public ImageIcon getIcon() {
        return getModule().getIcon16();        
    }

    @Override
    public String getLabel() {
        return DcResources.getText("lblAddChild", getModule().getObjectName());
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("tpAddChild");
    }
}
