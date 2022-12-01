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

package org.datacrow.client.console.menu;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.components.DcMenuItem;
import org.datacrow.client.console.components.DcPopupMenu;
import org.datacrow.client.console.components.ITextComponent;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.resources.DcResources;

public class DcEditorPopupMenu extends DcPopupMenu implements ActionListener  {

    private static final Toolkit tk = Toolkit.getDefaultToolkit();
    
    private JTextComponent c;
    
    public DcEditorPopupMenu(JTextComponent c) {
        this.c = c;
        
        JMenuItem menuCut = ComponentFactory.getMenuItem(IconLibrary._icoCut, DcResources.getText("lblCut"));
        JMenuItem menuCopy =ComponentFactory.getMenuItem(IconLibrary._icoCopy, DcResources.getText("lblCopy"));
        JMenuItem menuPaste = ComponentFactory.getMenuItem(IconLibrary._icoPaste, DcResources.getText("lblPaste"));
        JMenuItem menuSelectAll = ComponentFactory.getMenuItem(DcResources.getText("lblSelectAll"));
         
        boolean isEditable = c.isEditable();
        boolean isTextSelected = c.getSelectedText() != null;

        menuCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        menuCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        menuPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        menuSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));

        menuSelectAll.setActionCommand("selectAll");
        menuPaste.setActionCommand("paste");
        menuCopy.setActionCommand("copy");        
        menuCut.setActionCommand("cut");
        
        if (isEditable && isTextSelected)
            menuCut.addActionListener(this);
        else
            menuCut.setEnabled(false);
        
        if (isTextSelected)
            menuCopy.addActionListener(this);
        else
            menuCopy.setEnabled(false);
        
        Transferable content = tk.getSystemClipboard().getContents(null);
        if (isEditable && content.isDataFlavorSupported(DataFlavor.stringFlavor))
            menuPaste.addActionListener(this);
        else
            menuPaste.setEnabled(false);
        
        if (    c.getText().length() > 0 && 
               (c.getSelectedText() == null || 
                c.getSelectedText().length() < c.getText().length())) {
            
            menuSelectAll.addActionListener(this);
        } else {
            menuSelectAll.setEnabled(false);
        }

        this.add(menuCut);
        this.add(menuCopy);
        this.add(menuPaste);
        
        if (c instanceof ITextComponent) {
            this.addSeparator();

            DcMenuItem menuUndo;
            DcMenuItem menuRedo;
            
            DcMenuItem menuInsertTimeStamp = ComponentFactory.getMenuItem(IconLibrary._icoCalendar, DcResources.getText("lblInsertTimeStamp"));
            menuInsertTimeStamp.setActionCommand("timestamp");

            menuUndo = ComponentFactory.getMenuItem(((ITextComponent) c).getTextFieldActions().getUndoAction());
            menuRedo = ComponentFactory.getMenuItem(((ITextComponent) c).getTextFieldActions().getRedoAction());
            menuInsertTimeStamp = ComponentFactory.getMenuItem(((ITextComponent) c).getTextFieldActions().getInsertTimestampAction());

            menuUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
            menuRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
            menuInsertTimeStamp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
            menuInsertTimeStamp.setEnabled(true);

            this.add(menuUndo);
            this.add(menuRedo);
            
            if (!isEditable) {
                menuUndo.setEnabled(false);
                menuRedo.setEnabled(false);
                menuInsertTimeStamp.setEnabled(false);
            }
            
            this.addSeparator();
            this.add(menuInsertTimeStamp);
        }
        
        this.addSeparator();
        this.add(menuSelectAll);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("cut"))
           c.cut(); 
        else if (ae.getActionCommand().equals("copy"))
           c.copy(); 
        else if (ae.getActionCommand().equals("paste"))
           c.paste(); 
        else if (ae.getActionCommand().equals("selectAll"))
           c.selectAll();
    }
}
