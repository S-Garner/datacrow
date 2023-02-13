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

import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.data.DataFilter;
import org.datacrow.core.data.DataFilterEntry;
import org.datacrow.core.data.Operator;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.helpers.Item;
import org.datacrow.core.server.Connector;

public class ContainerNodeElement extends NodeElement {

	public ContainerNodeElement(String key, String displayValue, DcImageIcon icon) {
		super(key, displayValue, icon);
		addItem(key, DcModules._CONTAINER);
	}

	@Override
	public Map<String, Integer> getItems(DefaultMutableTreeNode node) {
		if (	DcModules.get(DcModules._CONTAINER).getSettings().getInt(
				DcRepository.ModuleSettings.stTreePanelShownItems) == DcModules._ITEM) {
			
			DataFilter df = new DataFilter(DcModules._ITEM);
			df.addEntry(new DataFilterEntry(DcModules._ITEM, Item._SYS_CONTAINER, Operator.EQUAL_TO, getKey()));
			
			String containerId; 
			for (DefaultMutableTreeNode child : ((DcDefaultMutableTreeNode) node).getChildrenFromLowerHierarchy(node)) {
				containerId = (String) ((ContainerNodeElement) child.getUserObject()).getKey();
				df.addEntry(new DataFilterEntry(DataFilterEntry._OR, DcModules._ITEM, Item._SYS_CONTAINER, Operator.EQUAL_TO, containerId));
			}

			Connector connector = DcConfig.getInstance().getConnector();
			return connector.getKeys(df);
		} else {
			return super.getItems(node);
		}
	}
	
	@Override
    public Map<String, Integer> getItemsSorted(List<String> allOrderedItems, DefaultMutableTreeNode node) {
    	return getItems(node);
    }
	
    @Override
    public String toString() {
        return getDisplayValue();
    }
}
