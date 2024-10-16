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

import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.datacrow.client.console.GUI;
import org.datacrow.client.console.components.renderers.DcTreeRenderer;
import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.settings.DcSettings;

public class DcTree extends JTree {
	
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DcTree.class.getName());
    
    public DcTree(DefaultMutableTreeNode node) {
        super(node);
        setProperties();
    }

    public DcTree(DefaultTreeModel model) {
        super(model);
        setProperties();
    }   
    
    private void setProperties() {
        setCellRenderer(new DcTreeRenderer());
        
        setRowHeight(DcSettings.getInt(DcRepository.Settings.stTreeNodeHeight));
    }
    
    @Override
    public void removeTreeSelectionListener(TreeSelectionListener tsl) {
        for (TreeSelectionListener listener : getTreeSelectionListeners()) {
            if (listener == tsl) 
                super.removeTreeSelectionListener(listener);
        }
    }
    
    @Override
    public void addTreeSelectionListener(TreeSelectionListener tsl) {
        removeTreeSelectionListener(tsl);
        super.addTreeSelectionListener(tsl);
    }    
    
    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
    	try {
    		super.paintComponent(GUI.getInstance().setRenderingHint(g));
    	} catch (Exception e) {
    	    logger.debug(e, e);
    	}
    }      
}
