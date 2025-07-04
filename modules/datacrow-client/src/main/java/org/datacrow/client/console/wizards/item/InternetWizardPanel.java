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

package org.datacrow.client.console.wizards.item;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ListSelectionModel;

import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.windows.onlinesearch.OnlineSearchForm;
import org.datacrow.client.console.wizards.WizardException;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.services.Servers;

public class InternetWizardPanel extends ItemWizardPanel implements MouseListener {

	private final ItemWizard wizard;
	
    private OnlineSearchForm internetSearchForm = null;

    public InternetWizardPanel(ItemWizard wizard, DcModule module) {
        build(module);
        this.wizard = wizard;
    }

    @Override
    public Object apply() throws WizardException {
        DcObject result = internetSearchForm.getSelectedObject();
        
        if (result == null) 
            throw new WizardException(DcResources.getText("msgWizardSelectItem"));

        internetSearchForm.stop();
        return result;
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgInternetSearch");
    }

    @Override
    public void cleanup() {
        if (internetSearchForm != null)
            internetSearchForm.close(false);
    }
    
    @Override
    public void setObject(DcObject dco) {}

    public void setFocus() {
        internetSearchForm.setFocus();
    }
    
    @Override
    public void onActivation() {
        if (internetSearchForm != null) 
            internetSearchForm.setFocus();
    }     
    
    private void build(DcModule module) {
        if (module.hasOnlineServices())
            internetSearchForm = GUI.getInstance().getOnlineSearchForm(
                    Servers.getInstance().getOnlineServices(module.getIndex()), null, null, false);

        if (internetSearchForm != null) {
            internetSearchForm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            internetSearchForm.addDoubleClickListener(this);
            internetSearchForm.disablePerfectMatch();
            setLayout(Layout.getGBL());
            add(internetSearchForm.getContentPanel(), Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                           ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                            new Insets(5, 5, 5, 5), 0, 0));
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getClickCount() == 2) {
            internetSearchForm.stop();
            wizard.next();
        }
    }
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
}