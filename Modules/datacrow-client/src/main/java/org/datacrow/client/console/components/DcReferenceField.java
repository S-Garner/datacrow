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

package org.datacrow.client.console.components;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.windows.itemforms.IItemFormListener;
import org.datacrow.client.console.windows.itemforms.ItemForm;
import org.datacrow.core.DcConfig;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.server.Connector;

public class DcReferenceField extends JComponent implements IComponent, ActionListener, IItemFormListener {

    private final DcObjectComboBox cb;

    private final JButton btCreate = ComponentFactory.getIconButton(IconLibrary._icoOpenNew);
    private final int referenceModIdx;
    private final boolean allowCreate;
    
    public DcReferenceField(int referenceModIdx) {
        super();
        
        setFont(ComponentFactory.getStandardFont());
        
        this.referenceModIdx = referenceModIdx;
        Connector connector = DcConfig.getInstance().getConnector();
        this.allowCreate = connector.getUser().isEditingAllowed(DcModules.get(referenceModIdx));
        
        cb = ComponentFactory.getObjectCombo(referenceModIdx);
        
        setLayout(Layout.getGBL());
        add(cb, Layout.getGBC( 0, 0, 1, 1, 100.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));        
        
        if (connector.getUser().isEditingAllowed(DcModules.get(referenceModIdx)))
            add(btCreate, Layout.getGBC( 1, 0, 1, 1, 0.0, 0.0
                    ,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                     new Insets( 0, 2, 0, 0), 0, 0));        
        
        btCreate.addActionListener(this);
        btCreate.setActionCommand("create");
        btCreate.setEnabled(allowCreate);
    }

    @Override
    public void setEditable(boolean b) {
        btCreate.setVisible(b);
        btCreate.setEnabled(b && allowCreate);
        cb.setEditable(b);
    }
    
    @Override
    public void reset() {
    	cb.reset();
    }   
    
    @Override
    public void setEnabled(boolean b) {
    	setEditable(b);
    }
    
    @Override
    public Object getValue() {
        return cb.getSelectedItem();
    }
    
    public JComboBox<Object> getComboBox() {
        return cb;
    }
    
    @Override
    public void setValue(Object o) {
        cb.setValue(o);
    }
    
    @Override
    public void clear() {
        removeAll();
        cb.clear();
    }
    
    @Override
    public void refresh() {
        cb.refresh();
    }

    @Override
    public void notifyItemSaved(DcObject dco) {
        setValue(dco);
    }

    private void create() {
        DcObject dco = DcModules.get(referenceModIdx).getItem();
        ItemForm itemForm = new ItemForm(false, false, dco, true);
        itemForm.setListener(this);
        itemForm.setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("create"))
            create();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(GUI.getInstance().setRenderingHint(g));
    }  
}