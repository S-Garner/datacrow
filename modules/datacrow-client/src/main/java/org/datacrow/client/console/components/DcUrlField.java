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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolTip;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.menu.DcEditorMouseListener;
import org.datacrow.client.util.launcher.URLLauncher;
import org.datacrow.core.IconLibrary;

public class DcUrlField extends JComponent implements IComponent, ActionListener, MouseListener {

    private final DcShortTextField text;

    public DcUrlField(int maxLength) {
    	text = ComponentFactory.getShortTextField(maxLength);
        text.setForeground(new Color(120,120,255));
        
        setBounds(0,0,0,0);
        
        addMouseListener(this);
        JButton buttonLaunch = ComponentFactory.getIconButton(IconLibrary._icoOpenApplication);
        buttonLaunch.addActionListener(this);
        
        setLayout(Layout.getGBL());
        add( text,         Layout.getGBC( 0, 0, 1, 1, 1000.0, 1000.0
                ,GridBagConstraints.WEST, GridBagConstraints.BOTH,
                 new Insets(0, 0, 0, 0), 0, 0));
        add( buttonLaunch, Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE,
                 new Insets(0, 2, 0, 0), 0, 0));    
        
        addMouseListener(new DcEditorMouseListener());
    }
    
    @Override
    public void reset() {
    	text.setText("");
    }      
    
    @Override
    public void clear() {}
    
    @Override
    public Object getValue() {
        return text.getText();
    }

    @Override
    public void setValue(Object o) {
        text.setText(o != null ? o.toString() : "");
    }

    @Override
    public void setEditable(boolean b) {
        text.setEditable(b);
    }
    
    public URL getURL() throws MalformedURLException {
        String s = text.getText();
        if (s != null && s.trim().length() > 0) {
            s = !s.toUpperCase().startsWith("HTTP://") && !s.toUpperCase().startsWith("HTTPS://") ?
                "https://" + s : s;
            return new URL(s);
        } else {
            return null;
        }
    }
    
    public void openURL() {
        try {
            URL url = getURL();
            if (url != null) {
            	URLLauncher launcher = new URLLauncher(url);
            	launcher.launch();
            }
        } catch (Exception exp) {
            GUI.getInstance().displayErrorMessage(exp.toString());
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        openURL();
    }

    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(GUI.getInstance().setRenderingHint(g));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2)
            openURL();
    }

    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}    
    @Override
    public void refresh() {}  
}
