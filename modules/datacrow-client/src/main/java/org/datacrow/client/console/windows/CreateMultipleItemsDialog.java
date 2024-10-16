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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcPopupMenu;
import org.datacrow.client.console.components.tables.DcTable;
import org.datacrow.client.console.windows.itemforms.ItemForm;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.modules.DcPropertyModule;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcProperty;
import org.datacrow.core.objects.ValidationException;
import org.datacrow.core.objects.helpers.MusicTrack;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;
import org.datacrow.core.utilities.definitions.DcFieldDefinition;

public class CreateMultipleItemsDialog extends DcDialog implements ActionListener, MouseListener {
    
	private final int moduleIdx;
	
	private final DcTable table;
	
	private final JButton buttonAdd = ComponentFactory.getButton(DcResources.getText("lblAdd"));
    private final JButton buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
    private final JButton buttonCancel = ComponentFactory.getButton(DcResources.getText("lblClose"));
	
	private SavingTask task;
	
	public CreateMultipleItemsDialog(int moduleIdx) {
		super();
		
		this.moduleIdx = moduleIdx;
		this.table = new DcTable(DcModules.get(moduleIdx), false, false);
		setIconImage(IconLibrary._icoItemsNew.getImage());
		
		table.addMouseListener(this);
		
		setTitle(DcResources.getText("lblAddMultiple"));
		
		build();
		
		pack();
		setCenteredLocation();
	}
	
	private void save() {
		if (task == null || !task.isAlive()) {
			task = new SavingTask();
			task.start();
		}
	}
	
	private void add() {
		DcModule module = DcModules.get(moduleIdx);
		DcObject dco = module.getItem();
		if (module.isChildModule()) {
		    String parentID;
			if (GUI.getInstance().getRootFrame() instanceof ItemForm)
				parentID = ((ItemForm) GUI.getInstance().getRootFrame()).getItem().getID();
			else
				parentID = GUI.getInstance().getSearchView(module.getIndex()).getCurrent().getSelectedItem().getID();
			
			dco.setValue(dco.getParentReferenceFieldIndex(), parentID);
		}
		table.add(dco);
	}
	
	public void setActionsAllowed(boolean b) {
		buttonAdd.setEnabled(b);
		buttonSave.setEnabled(b);
		buttonCancel.setEnabled(b);
	}
	
	@Override
    public void close() {
		if (task == null || !task.isAlive()) {
			task = null;
			super.close();
		}
		
		table.clear();
    }

	private int[] getFields() {
		DcModule module = DcModules.get(moduleIdx);
		
		Collection<Integer> fields = new ArrayList<Integer>();
		DcField field;
        for (DcFieldDefinition definition : module.getFieldDefinitions().getDefinitions()) {
        	field = module.getField(definition.getIndex());
            if (	definition.isEnabled() && 
            		!field.isReadOnly() && 
            		!field.isUiOnly() && 
            		field.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION &&
            		field.getValueType() != DcRepository.ValueTypes._BOOLEAN) 
                fields.add(Integer.valueOf(definition.getIndex()));
        }
        
        int[] result = new int[fields.size()];
        int i = 0;
        for (Integer fieldIdx : fields)
            result[i++] = fieldIdx.intValue();
        
        return result;
	}
	
	private void build() {
        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        table.setDynamicLoading(false);
        table.activate();
        
        if (DcModules.get(moduleIdx) instanceof DcPropertyModule) {
        	table.setIgnoreSettings(true);
        	table.setVisibleColumns(new int[] {DcProperty._A_NAME});
        } else if (moduleIdx == DcModules._MUSIC_TRACK) {
            table.setIgnoreSettings(true);
            table.setVisibleColumns(new int[] {
                    MusicTrack._F_TRACKNUMBER, 
                    MusicTrack._A_TITLE,
                    MusicTrack._J_PLAYLENGTH});
        } else {
        	table.setIgnoreSettings(true);
            table.setVisibleColumns(getFields());
        }
        
        getContentPane().setLayout(Layout.getGBL());
        
        getContentPane().add(sp,  Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(0, 0, 0, 0), 0, 0));

        JPanel panelActions = new JPanel();
        
        buttonAdd.addActionListener(this);
        buttonSave.addActionListener(this);
        buttonCancel.addActionListener(this);
        
        buttonAdd.setActionCommand("add");
        buttonSave.setActionCommand("save");
        buttonCancel.setActionCommand("cancel");
        
        panelActions.add(buttonAdd);
        panelActions.add(buttonSave);
        panelActions.add(buttonCancel);
        
        getContentPane().add(panelActions, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets(0, 0, 0, 0), 0, 0));
	}

	@Override
    public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand().equals("save"))
			save();
		else if (ae.getActionCommand().equals("cancel"))
			close();
		else if (ae.getActionCommand().equals("add"))
			add();
		else if (ae.getActionCommand().equals("delete"))
			table.remove(table.getSelectedRows());
    }
	
	private class SavingTask extends Thread {
		@Override
		public void run() {
		    DcObject dco;
		    Connector connector = DcConfig.getInstance().getConnector();
		    
		    StringBuffer sb = new StringBuffer();
		    
			while (table.getRowCount() > 0) {
				dco = table.getItemAt(0);
				dco.setIDs();
				
				try {
					if (connector.saveItem(dco) &&
					    GUI.getInstance().getSearchView(dco.getModuleIdx()) != null) {
					    GUI.getInstance().getSearchView(dco.getModuleIdx()).add(dco);
					}
					
					table.removeRow(0);
				} catch (ValidationException e) {
					
					if (sb.length() > 0)
						sb.append("\r\n");
					
					sb.append(e.getMessage());
				}
			}
			
			if (sb.length() > 0)
				GUI.getInstance().displayWarningMessage(sb.toString());
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (table.getSelectedIndex() > -1 && SwingUtilities.isRightMouseButton(e)) {
            JPopupMenu menu = new TablePopupMenu(this);                
            menu.setInvoker(table);
            menu.show(table, e.getX(), e.getY());
        }
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
	
	private static class TablePopupMenu extends DcPopupMenu {
        
		public TablePopupMenu(CreateMultipleItemsDialog dlg) {
            JMenuItem menuDelete = new JMenuItem(DcResources.getText("lblDelete", ""), IconLibrary._icoDelete);
            
            menuDelete.addActionListener(dlg);
            menuDelete.setActionCommand("delete");
            
            add(menuDelete);
        }
    }	
}