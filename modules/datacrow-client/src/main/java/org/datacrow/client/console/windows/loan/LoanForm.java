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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Collection;

import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.panels.LoanPanel;
import org.datacrow.client.console.windows.DcFrame;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;

public class LoanForm extends DcFrame {

    public LoanForm(Collection<? extends DcObject> objects) throws Exception {
        super(DcResources.getText("lblLoan"), IconLibrary._icoLoan);
        setHelpIndex("dc.loans");
        buildForm(objects); 
    }    
    
    @Override
    public void close() {
        DcModules.getCurrent().setSetting(DcRepository.ModuleSettings.stLoanFormSize, getSize());
        super.close();
    }
    
    private void buildForm(Collection<? extends DcObject> objects) throws Exception {
        getContentPane().setLayout(Layout.getGBL());

        LoanPanel loanPanel = new LoanPanel(objects, this);
        getContentPane().add( loanPanel, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                             ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                              new Insets(5, 5, 5, 5), 0, 0));

        pack();
        setSize(DcModules.getCurrent().getSettings().getDimension(DcRepository.ModuleSettings.stLoanFormSize));
        setCenteredLocation();        
    }
}
