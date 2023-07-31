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

import org.datacrow.client.console.Layout;
import org.datacrow.client.console.windows.itemforms.ItemForm;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;

public class ItemDetailsWizardPanel extends ItemWizardPanel {

    private final ItemForm itemForm;

    public ItemDetailsWizardPanel(DcObject dco) {
    	itemForm = new ItemForm(null, false,  false, dco, true);
    	
        build(dco);
    }

    @Override
    public Object apply() {
        return itemForm.getItem();
    }

    @Override
    public void setObject(DcObject dco) {
        itemForm.setData(dco, true, false);
    }

    @Override
    public String getHelpText() {
        return DcResources.getText("msgEditDetails");
    }

    @Override
    public void cleanup() {
        itemForm.close(true);
    }
    
    private void build(DcObject dco) {
        setLayout(Layout.getGBL());
        add(itemForm.getTabbedPane(), Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                       ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                        new Insets(5, 5, 5, 5), 0, 0));
    }
}
