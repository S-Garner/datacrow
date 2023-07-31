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

package org.datacrow.client.console.windows.loan;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenuItem;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.components.DcPopupMenu;
import org.datacrow.client.console.windows.itemforms.ItemForm;
import org.datacrow.core.DcConfig;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.server.Connector;

public class LoanInformationPanelPopupMenu extends DcPopupMenu implements ActionListener {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(LoanInformationPanelPopupMenu.class.getName());
    
    private final DcObject dco;
    private final List<DcObject> items;
    
    public LoanInformationPanelPopupMenu(DcObject dco, List<DcObject> items) {
        
        this.dco = dco;
        this.items = items;
        
        JMenuItem menuOpen = ComponentFactory.getMenuItem(IconLibrary._icoOpen, DcResources.getText("lblOpenItem", dco.getModule().getObjectName()));
        JMenuItem menuEdit = ComponentFactory.getMenuItem(IconLibrary._icoOpen, DcResources.getText("lblEditItem", dco.getModule().getObjectName()));
        JMenuItem menuLoan = ComponentFactory.getMenuItem(IconLibrary._icoLoan, DcResources.getText("lblLoanAdministration"));
             
        menuLoan.setActionCommand("loan");
        menuOpen.setActionCommand("openItem");
        menuEdit.setActionCommand("editItem");
        
        menuOpen.addActionListener(this);
        menuEdit.addActionListener(this);
        menuLoan.addActionListener(this);
        
        this.add(menuLoan);
        this.addSeparator();
        this.add(menuOpen);

        Connector connector = DcConfig.getInstance().getConnector();
        SecuredUser su = connector.getUser();
        if (su.isAuthorized(dco.getModule())) {
            this.add(menuEdit);
            if (su.isEditingAllowed(dco.getModule()))
                this.add(menuEdit);
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        dco.markAsUnchanged();
        dco.getModule();
        
        if (ae.getActionCommand().equals("openItem")) {
            ItemForm form = new ItemForm(true, true, dco, false);
            form.setVisible(true);
        } else if (ae.getActionCommand().equals("editItem")) {
            ItemForm form = new ItemForm(false, true, dco, false);
            form.setVisible(true);
        } else if (ae.getActionCommand().equals("loan")) {
            try {
                LoanForm form = new LoanForm(items);
                form.setVisible(true);
            } catch (Exception e) {
                logger.warn(e, e);
                GUI.getInstance().displayWarningMessage(e.getMessage());
            }
        }
    }
}
