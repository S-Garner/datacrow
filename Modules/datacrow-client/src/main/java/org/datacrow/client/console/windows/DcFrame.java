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

package org.datacrow.client.console.windows;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.plugins.PluginHelper;
import org.datacrow.client.util.Utilities;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.console.IWindow;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.settings.objects.DcDimension;
import org.datacrow.core.utilities.CoreUtilities;

public class DcFrame extends JFrame implements WindowFocusListener, IWindow {

	private String helpIndex = null;

    public DcFrame(String title, DcImageIcon icon) {
        super(title);
        
        DcImageIcon image = icon == null ? IconLibrary._icoMain : icon;
        
        List<Image> images = new ArrayList<>();
        
        int width = image.getIconWidth();
        
        if (width < 64) {
            images.add(image.getImage());
        } else {
            images.add(CoreUtilities.getScaledImage(image, 64, 64));
            
            if (width > 128) {
                images.add(CoreUtilities.getScaledImage(image, 128, 128));
            } else if (width == 128) {
                images.add(image.getImage());
            }

            if (width > 256) {
                images.add(CoreUtilities.getScaledImage(image, 256, 256));
            } else if (width == 256) {
                images.add(image.getImage());
            }
            
            if (width > 256) {
                images.add(image.getImage());
            }            
        }
        
        setIconImages(images);
        GUI.getInstance().setRootFrame(this);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        
        addWindowFocusListener(this);
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        PluginHelper.registerKey(getRootPane(), "Help");
        PluginHelper.registerKey(getRootPane(), "CloseWindow");
        
        GUI.getInstance().addOpenWindow(this);
    }
    
    @Override
    public void close() {

        GUI.getInstance().removeOpenWindow(this);
        
        for (WindowListener wl : getWindowListeners())
            removeWindowListener(wl);
        
        helpIndex = null;
        
        if (rootPane.getInputMap() != null)
            rootPane.getInputMap().clear();

        if (rootPane.getActionMap() != null) {
            rootPane.getActionMap().clear();
            rootPane.setActionMap(null);
        }
        
        ComponentFactory.clean(getContentPane());
        dispose();
        
        GUI gui = GUI.getInstance();
        gui.setRootFrame(gui.getMainFrame());
    }
    
    @Override
    public void setSize(Dimension d) {
        if (d == null || d.height == 20 || d.width < 20)
            return;
        
        super.setSize(d);
    }

    protected void addKeyListener(KeyStroke keyStroke, Action action, String name) {
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, name);
        rootPane.getActionMap().put(name, action);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) GUI.getInstance().setRootFrame(this);
    }

    public void setHelpIndex(String helpID) {
        helpIndex =  helpID;
    }
    
    public String getHelpIndex() {
        return helpIndex;
    }

    protected void setCenteredLocation() {
        setLocation(Utilities.getCenteredWindowLocation(getSize(), false));
    }
    
    public void setSize(DcDimension d) {
    	setSize(new Dimension(d.getWidth(), d.getHeight()));
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        GUI.getInstance().setRootFrame(this);
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(GUI.getInstance().setRenderingHint(g));
    }
}
