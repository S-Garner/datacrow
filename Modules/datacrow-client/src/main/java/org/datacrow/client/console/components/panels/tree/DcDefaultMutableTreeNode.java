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

package org.datacrow.client.console.components.panels.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

public class DcDefaultMutableTreeNode extends DefaultMutableTreeNode {

    public DcDefaultMutableTreeNode(Object userObject) {
        super(userObject);
    }

    public void addItem(String item, Integer moduleIdx) {
        NodeElement ne = (NodeElement) getUserObject();
        ne.addItem(item, moduleIdx);
    }
    
    public void removeItem(String item) {
        NodeElement ne = (NodeElement) getUserObject();
        ne.removeItem(item);
    }
    
    public int getItemCount() {
        return ((NodeElement) getUserObject()).getCount();
    }
    
    public Map<String, Integer> getItems() {
        Object o = getUserObject();
        return o instanceof String ? new HashMap<String, Integer>() : ((NodeElement) getUserObject()).getItems(this);
    }

    public List<String> getItemList() {
        return new ArrayList<String>(((NodeElement) getUserObject()).getItems(this).keySet());
    }
    
    public Map<String, Integer> getItemsSorted(List<String> allSortedItems) {
        Object uo = getUserObject();
        return uo instanceof String ? getItems() : ((NodeElement) uo).getItemsSorted(allSortedItems, this);
    }
    
    public boolean contains(String item) {
        return ((NodeElement) getUserObject()).getItems(this).containsKey(item);
    }
    
    @Override
    public int hashCode() {
        return getUserObject().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DcDefaultMutableTreeNode)) return false;
        
        DcDefaultMutableTreeNode node = (DcDefaultMutableTreeNode) obj;
        return node.getUserObject().equals(getUserObject());
    }
    
    /**
     * Returns all direct children for the given node.
     */
    public Collection<DefaultMutableTreeNode> getChildren(
    			DefaultMutableTreeNode parent) {
	        
    	Collection<DefaultMutableTreeNode> children = new ArrayList<>();
        int size = parent.getChildCount();
        
        DefaultMutableTreeNode child;
        for (int i = 0; i < size; i++) {
            child = (DefaultMutableTreeNode) parent.getChildAt(i);
            children.add(child);
        }
        
        return children;
    }    
    
    /**
     * Returns all children, including children of children; the whole hierarchy.
     */
    public Collection<DefaultMutableTreeNode> getChildrenFromLowerHierarchy(
    			DefaultMutableTreeNode parent) {
	        
    	Collection<DefaultMutableTreeNode> children = new ArrayList<>();

    	for (DefaultMutableTreeNode child : getChildren(parent)) {
    		children.add(child);
    		children.addAll(getChildren(child));
    	}
    	
        return children;
    }    
}
