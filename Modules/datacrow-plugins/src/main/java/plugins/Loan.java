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

import org.apache.logging.log4j.Logger;

import org.datacrow.client.console.GUI;
import org.datacrow.client.console.windows.loan.LoanForm;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.console.IView;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcTemplate;
import org.datacrow.core.plugin.Plugin;
import org.datacrow.core.resources.DcResources;

public class Loan extends Plugin {

	private static final long serialVersionUID = 1L;

	private static Logger logger = DcLogManager.getLogger(Loan.class.getName());
    
    public Loan(DcObject dco, DcTemplate template, int viewIdx, int moduleIdx, int viewType) {
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
        try {
            int moduleIdx = DcModules.getCurrent().getIndex();
            IView view = GUI.getInstance().getSearchView(moduleIdx).getCurrent();
            LoanForm form = new LoanForm(view.getSelectedItems());
            form.setVisible(true);
        } catch (Exception exp) {
            logger.warn(exp, exp);
        }
    }
    
    @Override
    public ImageIcon getIcon() {
        return IconLibrary._icoLoan;
    }
    
    @Override
    public boolean isSystemPlugin() {
        return true;
    }

    @Override
    public String getLabel() {
        return DcResources.getText("lblLoanAdministration");
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("tpLoan");
    }        
}
