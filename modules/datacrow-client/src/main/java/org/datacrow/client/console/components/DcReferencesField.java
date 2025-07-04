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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.windows.DcReferencesDialog;
import org.datacrow.client.console.windows.itemforms.IItemFormListener;
import org.datacrow.client.console.windows.itemforms.ItemForm;
import org.datacrow.client.util.Utilities;
import org.datacrow.core.DcConfig;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.modules.MappingModule;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.server.Connector;

public class DcReferencesField extends JComponent implements IComponent, ActionListener, IItemFormListener, MouseListener {

    private DcHtmlEditorPane fld = new HtmlField();

    private final List<DcObject> references = new ArrayList<DcObject>();
    private final JButton btOpen = ComponentFactory.getIconButton(IconLibrary._icoOpen);
    private final JButton btCreate = ComponentFactory.getIconButton(IconLibrary._icoOpenNew);
    
    private final int mappingModIdx;
    private final int referenceIdx;
    
    private boolean allowCreate;
    
    public DcReferencesField(int mappingModIdx) {
        super();
        
        fld.addMouseListener(this);
        
        this.mappingModIdx = mappingModIdx;
        this.referenceIdx = ((MappingModule) DcModules.get(mappingModIdx)).getReferencedModIdx();
        
        Connector connector = DcConfig.getInstance().getConnector();
        allowCreate = connector.getUser().isEditingAllowed(DcModules.get(referenceIdx));
        
        setLayout(Layout.getGBL());
        
        JScrollPane scrollIn = new JScrollPane(fld);
        scrollIn.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollIn.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollIn.setPreferredSize(new Dimension(350,50));
        ComponentFactory.setBorder(scrollIn);
        
        add(scrollIn, Layout.getGBC( 0, 0, 1, 2, 1000.0, 1000.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 0, 0, 0), 0, 0));        
        add(btCreate, Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 0, 2, 0, 0), 0, 0));        
        add(btOpen,   Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 0, 2, 0, 0), 0, 0));        
        
        btOpen.addActionListener(this);
        btOpen.setActionCommand("openDialog");

        btCreate.addActionListener(this);
        btCreate.setActionCommand("create");
        btCreate.setEnabled(allowCreate);
        
        this.setMinimumSize(new Dimension(200,42));
    }
    
    @Override
    public void setEditable(boolean b) {
        btCreate.setVisible(b);
        btOpen.setVisible(b);
        btOpen.setEnabled(b);
        btCreate.setEnabled(b && allowCreate);
    }
    
    @Override
    public void reset() {
    	references.clear();
    	setDescription();
    }    
    
    @Override
    public Object getValue() {
        return references;
    }
    
    public void setValue(Collection<DcMapping> c) {
        references.clear();
        references.addAll(c);
        setDescription();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setValue(Object o) {
        setValue((Collection<DcMapping>) o);
    }
    
    @Override
    public void clear() {
        fld = null;
        
        if (references != null) references.clear();
        
        removeAll();
    }
    
    private void create() {
        DcObject dco = DcModules.get(referenceIdx).getItem();
        ItemForm itemForm = new ItemForm(false, false, dco, true);
        itemForm.setListener(this);
        itemForm.setVisible(true);
    }
        
    private void openDialog() {
        MappingModule mappingModule = (MappingModule) DcModules.get(mappingModIdx);
        DcReferencesDialog dlg = new DcReferencesDialog(references, mappingModule, this);
        dlg.setVisible(true);
    }
    
    private void setDescription() {
        fld.setText("");
        if (references == null)
            return;
        
        StringBuffer desc = new StringBuffer("<html><body><div " + Utilities.getHtmlStyle() + ">");
        desc.append(fld.createLinks(references));
        desc.append("</div></body></html>");
        
        fld.setHtml(desc.toString());
        fld.setCaretPosition(0);
    }
    
    @Override
    public void notifyItemSaved(DcObject dco) {
        DcObject mapping = DcModules.get(mappingModIdx).getItem();
        mapping.setValue(DcMapping._B_REFERENCED_ID, dco.getID());
        references.add(mapping);
        setDescription();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(GUI.getInstance().setRenderingHint(g));
    }  

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("openDialog"))
            openDialog();
        else if (e.getActionCommand().equals("create"))
            create();        
    }
    
    @Override
    public void refresh() {
        setDescription();
    }  
    
    @Override
    public void mouseReleased(MouseEvent e) {
        if (btOpen.isEnabled() && e.getClickCount() == 2)
            openDialog();
    }
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    
    private class HtmlField extends DcHtmlEditorPane {

        @Override
        public void notifyItemSaved(DcObject dco) {
            
            // this will update the description of a property (for example)
            // on this references field.
            DcMapping m;
            
            if (references == null) return;
            
            for (DcObject reference : references) {
                m = (DcMapping) reference;
                if (dco.getID().equals(m.getReferencedID()))
                    m.setReference(dco);
            }
            
            setDescription();
        }
        
    }
}