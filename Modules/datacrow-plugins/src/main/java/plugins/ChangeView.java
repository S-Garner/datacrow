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
import javax.swing.KeyStroke;

import org.datacrow.client.console.GUI;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.console.IMasterView;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcTemplate;
import org.datacrow.core.plugin.Plugin;
import org.datacrow.core.resources.DcResources;

public class ChangeView extends Plugin {

	private static final long serialVersionUID = -8157658800465547107L;

	public ChangeView(DcObject dco, DcTemplate template, int viewIdx, int moduleIdx, int viewType) {
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
    	IMasterView searchView = GUI.getInstance().getSearchView(getModule().getIndex());
    	if (searchView.getCurrent().getIndex() != getViewIdx()) {
            if (searchView.get(getViewIdx()) != null)
            	searchView.setView(getViewIdx());
        }
    }
    
    @Override
    public boolean isEnabled() {
        int module = getModule().getIndex();
        IMasterView mv = GUI.getInstance().getSearchView(module);
        return mv.getCurrent().getIndex() != getViewIdx();
    }
    
    @Override
    public boolean isSystemPlugin() {
        return true;
    }

    @Override
    public KeyStroke getKeyStroke() {
        if (getViewIdx() == IMasterView._TABLE_VIEW)
            return KeyStroke.getKeyStroke("F2");
        
        return KeyStroke.getKeyStroke("F3");
    }
    
    @Override
    public ImageIcon getIcon() {
        if (getViewIdx() == IMasterView._TABLE_VIEW)
            return IconLibrary._icoTableView;

        return IconLibrary._icoCardView;
    }

    @Override
    public String getLabel() {
        if (getViewIdx() == IMasterView._TABLE_VIEW)
            return DcResources.getText("lblTableView");

        return DcResources.getText("lblCardView");
    } 
    
    @Override
    public String getHelpText() {
        if (getViewIdx() == IMasterView._TABLE_VIEW)
            return DcResources.getText("tpTableView");
        
        return DcResources.getText("tpCardView");
    }     
}
