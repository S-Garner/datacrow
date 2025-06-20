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

import java.awt.Frame;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JDialog;

import org.datacrow.client.console.GUI;

public class NativeDialog extends JDialog implements IDialog {

	private AtomicBoolean active;
    
    public NativeDialog() {
        super();
    }

    public NativeDialog(Frame owner) {
        super(owner);
    }
    
    public void setModal(AtomicBoolean active) {
        this.active = active;
    }
    
    public void close() {
        dispose();
    }
    
    public void setCenteredLocation() {
    	setLocationRelativeTo(
    			GUI.getInstance().getMainFrame() != null && GUI.getInstance().getMainFrame().isVisible() ? 
    					GUI.getInstance().getMainFrame() : null);
    }
 
    @Override
    public void dispose() {
        if (active != null) {
            synchronized (active) {
                active.set(false);
                active.notifyAll();
            }
        }
        
        super.dispose();   
    }      
}
