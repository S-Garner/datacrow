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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.components.DcLongTextField;
import org.datacrow.client.console.components.DcMenu;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.resources.DcResources;

public class TextEditMenu extends DcMenu {

    public TextEditMenu(DcLongTextField fld) {
        super(DcResources.getText("lblEdit"));
        build(fld);
    }
    
    private void build(DcLongTextField fld) {
        HashMap<Object, Action> actions = new HashMap<Object, Action>();
        Action[] actionsArray = fld.getActions();
        Action a;
        for (int i = 0; i < actionsArray.length; i++) {
            a = actionsArray[i];
            actions.put(a.getValue(Action.NAME), a);
        }

        JMenuItem menuUndo = ComponentFactory.getMenuItem(fld.getTextFieldActions().getUndoAction());
        JMenuItem menuRedo = ComponentFactory.getMenuItem(fld.getTextFieldActions().getRedoAction());
        JMenuItem menuCut = ComponentFactory.getMenuItem(IconLibrary._icoCut, DcResources.getText("lblCut"));
        JMenuItem menuCopy = ComponentFactory.getMenuItem(IconLibrary._icoCopy, DcResources.getText("lblCopy"));
        JMenuItem menuPaste = ComponentFactory.getMenuItem(IconLibrary._icoPaste, DcResources.getText("lblPaste"));
        JMenuItem menuSelectAll = ComponentFactory.getMenuItem(DcResources.getText("lblSelectAll"));

        menuCut.addActionListener(actions.get(DefaultEditorKit.cutAction));
        menuCopy.addActionListener(actions.get(DefaultEditorKit.copyAction));
        menuPaste.addActionListener(actions.get(DefaultEditorKit.pasteAction));
        menuSelectAll.addActionListener(actions.get(DefaultEditorKit.selectAllAction));
        
        menuUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        menuRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        menuCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        menuCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        menuPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        menuSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        
        add(menuCut);
        add(menuCopy);
        add(menuPaste);
        addSeparator();
        add(menuUndo);
        add(menuRedo);
        addSeparator();
        add(menuSelectAll);
    }
}
