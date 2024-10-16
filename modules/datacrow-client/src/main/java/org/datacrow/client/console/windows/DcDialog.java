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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.plugins.PluginHelper;
import org.datacrow.client.util.Utilities;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.settings.objects.DcDimension;

public class DcDialog extends JDialog implements IDialog {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DcDialog.class.getName());
    
    private String helpIndex = null;
    private AtomicBoolean active;

    public DcDialog(JFrame parent) {
        super(parent);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);
        
        if (GUI.getInstance().getMainFrame() != null) {
            PluginHelper.registerKey(getRootPane(), "Help");
            PluginHelper.registerKey(getRootPane(), "CloseWindow");
        }
        
        GUI.getInstance().addOpenWindow(this);
    }

    public DcDialog() {
        this(GUI.getInstance().getRootFrame());
    }
    
    @Override
    public void setModal(AtomicBoolean active) {
        this.active = active;
    }
    
    public void setSize(DcDimension d) {
    	setSize(new Dimension(d.getWidth(), d.getHeight()));
    }
 
    @Override
    public void dispose() {
        if (active != null) {
            synchronized (active) {
                active.set(false);
                active.notifyAll();
            }
        }
        
        try {
            super.dispose();
        } catch (Exception e) {}
    }
    
    protected void disableXCloseWindowButton() {
    	
    	setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    	
    	for (WindowListener wl : this.getWindowListeners())
    	    this.removeWindowListener(wl);
    	
    	this.addWindowListener(new WindowAdapter() {
    	    @Override
    	    public void windowClosing(WindowEvent e) {}
    	});
    }

    @Override
    public void setVisible(boolean b) {
        GUI gui = GUI.getInstance();
        if (b && gui.isSplashScreenActive())
            gui.showSplashScreen(false);
        
        if (!b) dispose();
        
        super.setVisible(b);
    }

    @Override
    public void close() {
        long start = logger.isDebugEnabled() ? new Date().getTime() : 0;

        GUI.getInstance().removeOpenWindow(this);
        
        helpIndex = null;
        
        if (rootPane.getInputMap() != null)
            rootPane.getInputMap().clear();
        
        if (rootPane.getActionMap() != null)
            rootPane.getActionMap().clear();
        
        rootPane.setActionMap(null);        
        
        ComponentFactory.clean(getContentPane());
        
        dispose();
        
        if (logger.isDebugEnabled()) {
            long end = new Date().getTime();
            logger.debug("Disposing of the dialog and its resources took " + (end - start) + "ms");
        }  
    }

    public void setHelpIndex(String helpIndex) {
    	this.helpIndex = helpIndex;
    }

    public String getHelpIndex() {
        return helpIndex;
    }

    public void setCenteredLocation() {
        setLocation(Utilities.getCenteredWindowLocation(getSize(), false));
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(GUI.getInstance().setRenderingHint(g));
    }    
    
    protected void addKeyListener(KeyStroke keyStroke, Action action, String name) {
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, name);
        rootPane.getActionMap().put(name, action);
    }
}
