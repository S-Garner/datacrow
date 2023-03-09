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
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.datacrow.client.console.GUI;
import org.datacrow.client.console.windows.reporting.ReportingDialog;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.console.IView;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcTemplate;
import org.datacrow.core.plugin.Plugin;
import org.datacrow.core.resources.DcResources;

public class Report extends Plugin {

	private static final long serialVersionUID = -5287981476453811542L;

	public Report(DcObject dco, DcTemplate template, int viewIdx, int moduleIdx, int viewType) {
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
    	List<String> items = new ArrayList<String>();
    	
    	if (getItem() == null || getViewIdx() == -1) {
    		IView view = GUI.getInstance().getSearchView(getModule().getIndex()).getCurrent();
	    	items.addAll(view.getItemKeys());
	    	ReportingDialog dialog = new ReportingDialog(items);
	    	dialog.setVisible(true);
    	} else {
    		IView view = GUI.getInstance().getSearchView(getModule().getIndex()).getCurrent();
	    	items.addAll(view.getSelectedItemKeys());
	    	ReportingDialog dialog = new ReportingDialog(items);
	    	dialog.setVisible(true);
    	}
    }  

    @Override
    public KeyStroke getKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
    }
    
    @Override
    public boolean isSystemPlugin() {
        return true;
    }

    @Override
    public ImageIcon getIcon() {
        return IconLibrary._icoReport;
    }

    @Override
    public String getLabel() {
        return DcResources.getText("lblCreateReport");
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("tpCreateReport");
    }      
}
