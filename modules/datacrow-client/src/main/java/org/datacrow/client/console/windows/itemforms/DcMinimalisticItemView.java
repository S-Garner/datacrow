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

package org.datacrow.client.console.windows.itemforms;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcMenu;
import org.datacrow.client.console.components.DcMenuBar;
import org.datacrow.client.console.components.DcMenuItem;
import org.datacrow.client.console.components.DcPanel;
import org.datacrow.client.console.components.DcShortTextField;
import org.datacrow.client.console.components.lists.DcListModel;
import org.datacrow.client.console.components.lists.DcObjectList;
import org.datacrow.client.console.components.lists.elements.DcListElement;
import org.datacrow.client.console.components.lists.elements.DcObjectListElement;
import org.datacrow.client.console.menu.DcPropertyViewPopupMenu;
import org.datacrow.client.console.windows.CreateMultipleItemsDialog;
import org.datacrow.client.console.windows.DcFrame;
import org.datacrow.client.console.windows.IMergeItemsListener;
import org.datacrow.client.console.windows.MergeItemsDialog;
import org.datacrow.client.plugins.PluginHelper;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.clients.IClient;
import org.datacrow.core.console.ISimpleItemView;
import org.datacrow.core.data.DataFilter;
import org.datacrow.core.data.DataFilterEntry;
import org.datacrow.core.data.Operator;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;
import org.datacrow.core.settings.Settings;
import org.datacrow.core.wf.tasks.DcTask;
import org.datacrow.core.wf.tasks.DeleteItemTask;


public class DcMinimalisticItemView extends DcFrame 
	implements ActionListener, MouseListener, ISimpleItemView, KeyListener, IClient, IMergeItemsListener {

    private static final FlowLayout layout = new FlowLayout(FlowLayout.LEFT);

    private final JButton buttonCreateMultiple = ComponentFactory.getButton(DcResources.getText("lblCreateMultiple"));
    private final JButton buttonNew = ComponentFactory.getButton(DcResources.getText("lblNew"));
    private final JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
    
    private final JPanel panelActions = new JPanel();

    protected final DcObjectList list;

    private final boolean readonly;
    protected final int module;
    
    protected DcTask task;
    
    private boolean cancelled = false;
    
    private JScrollPane scroller;
    private String parentID;
    private JPanel statusPanel;

    private List<DcListElement> all = new ArrayList<DcListElement>();
    
    private DcPanel panel = new DcPanel();
    
    public DcMinimalisticItemView(int module, boolean readonly) {
        super(DcModules.get(module).getObjectNamePlural(), DcModules.get(module).getIcon32());
        
        this.list = new DcObjectList(DcObjectList._LISTING, false, true);
        this.readonly = readonly;
        
        GUI.getInstance().setRootFrame(this);
        this.module = module;
        
        buildPanel();

        pack();
        
        setHelpIndex("dc.items.administration");
        
        Settings settings = DcModules.get(module).getSettings();
        setSize(settings.getDimension(DcRepository.ModuleSettings.stSimpleItemViewSize));
        setCenteredLocation();
    }
    
    public void storeElements() {
        all.addAll(list.getElements());
    }
    
    @Override
    public void hideDialogActions(boolean b) {
        buttonClose.setVisible(!b);
        statusPanel.setVisible(!b);
    }
    
    @Override
    public void setParentID(String ID) {
        parentID = ID;
    }
    
    @Override
    public String getParentID() {
        return parentID;
    }

    @Override
    public void close() {
        cancelled = true;
        
        if (task != null) {
            task.cancel();
            task = null;
        }
            
        setVisible(false);
    }
    
    public void open() {
        open(true);
    }
    
    public void open(boolean edit) {
        DcObject dco = list.getSelectedItem();
        if (dco != null) {
            dco.markAsUnchanged();
            DcMinimalisticItemForm itemForm = new DcMinimalisticItemForm(!edit, true, dco, this);
            itemForm.setVisible(true);
        }
    }
    
    public void createMultiple() {
    	CreateMultipleItemsDialog dlg = new CreateMultipleItemsDialog(getModuleIdx());
    	dlg.setVisible(true);
    	load();
    }
    
    public void mergeItems() {
        Collection<DcObject> items = list.getSelectedItems();
        if (items.size() == 0) {
            GUI.getInstance().displayWarningMessage(DcResources.getText("msgMergeNoItemsSelected"));
        } else {
            MergeItemsDialog dlg = new MergeItemsDialog(items, DcModules.get(module));
            dlg.addListener(this);
            dlg.setVisible(true);
        }
    }
    
    public void createNew() {
        DcMinimalisticItemForm itemForm = new DcMinimalisticItemForm(false, false, DcModules.get(module).getItem(), this);
        itemForm.setVisible(true);
    }
    
    @Override
    public void setItems(List<DcObject> objects) {
        all.clear();
        list.clear();
        list.add(objects);
        
        storeElements();
    }
    
    @Override
    public Collection<DcObject> getItems() {
        return list.getItems();
    }

    @Override
    public void notify(String msg) {
        GUI.getInstance().displayMessage(msg);
    }
    
    @Override
    public void notifyError(Throwable t) {
        notify(t.getMessage());
    }

    @Override
    public void notifyWarning(String msg) {
        notify(msg);
    }
    
    @Override
    public boolean askQuestion(String msg) {
        return GUI.getInstance().displayQuestion(msg);
    }

    @Override
    public void notifyTaskCompleted(boolean success, String taskID) {
        list.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        allowActions();
        list.setSelectedIndex(0);
        
        if (success)
            load();
    }

    @Override
    public void notifyTaskStarted(int taskSize) {
        panel.initProgressBar(taskSize);
        denyActions();
        list.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    }

    @Override
    public void notifyProcessed() {
        panel.updateProgressBar();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    protected JPopupMenu getPopupMenu() {
        return new DcPropertyViewPopupMenu(this);
    }
    
    protected int getModuleIdx() {
        return module;
    }
    
    public DcModule getModule() {
        return DcModules.get(getModuleIdx());
    }
    
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            load();
            setCenteredLocation();
        }
    }    
    
    @Override
    public void clear() {
        if (task != null) task.cancel();
        if (all != null) all.clear();
        if (list != null) list.clear();
        
        task = null;
        scroller = null;
        statusPanel = null;
        all = null;
        panel = null;
    }
    
    @Override
    public void load() {
        
    	if (all != null) 
        	all.clear();
        
        list.clear();
        
        DcObject dco = DcModules.get(module).getItem();
        
        DataFilter filter = new DataFilter(module);
        
        if (getParentID() != null)
            filter.addEntry(
            		new DataFilterEntry(module, 
	            		DcModules.get(module).getParentReferenceFieldIndex(), 
	            		Operator.EQUAL_TO, getParentID()));
        
        filter.setOrder(dco.getField(DcModules.get(module).getDefaultSortFieldIdx()));
        
        Connector connector = DcConfig.getInstance().getConnector();
        
        SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
		        list.add(connector.getKeys(filter));
		        
		        storeElements();
		        
		        invalidate();
		        repaint();
			}
		});
    }    

    @Override
    public void setFont(Font font) {
        Font fontNormal = ComponentFactory.getStandardFont();
        Font fontSystem = ComponentFactory.getSystemFont();

        super.setFont(fontNormal);
        if (panel != null) {
            panel.setFont(fontSystem);
            buttonClose.setFont(fontSystem);
            buttonNew.setFont(fontSystem);
            buttonCreateMultiple.setFont(fontSystem);
            list.setFont(fontNormal);
        }
    }      
    
    public void denyActions() {
        list.removeMouseListener(this);    
    }

    public void allowActions() {
        list.removeMouseListener(this);
        list.addMouseListener(this);
    }    
    
    /**
     * Indicates whether there is a data task running at this moment
     */
    protected boolean isTaskRunning() {
        boolean isTaskRunning = task != null && task.isRunning();
        if (isTaskRunning) GUI.getInstance().displayWarningMessage("msgJobRunning");

        return isTaskRunning; 
    }     

    private void buildPanel() {
        getContentPane().setLayout(Layout.getGBL());
        
        scroller = new JScrollPane(list);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);        
        
        list.addMouseListener(this);
        
        //**********************************************************
        //Result panel
        //**********************************************************
        panel.setLayout(Layout.getGBL());
        panel.add(scroller,  Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        
        //**********************************************************
        //Action panel
        //**********************************************************
        panelActions.setLayout(layout);
        
        buttonCreateMultiple.addActionListener(this);
        buttonCreateMultiple.setActionCommand("createMultiple");
        buttonCreateMultiple.setToolTipText(DcResources.getText("tpCreateMultiple"));
        
        buttonNew.addActionListener(this);
        buttonNew.setActionCommand("createNew");
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        
        if (!getModule().isAbstract() && getModule().getType() != DcModule._TYPE_TEMPLATE_MODULE)
            panelActions.add(buttonCreateMultiple);
        
        if (!getModule().isAbstract())
            panelActions.add(buttonNew);
        
        panelActions.add(buttonClose);
        
        //**********************************************************
        //Main panel
        //**********************************************************
        statusPanel = panel.getProgressPanel();
        
        setJMenuBar(new DcMinimalisticItemViewMenu(DcModules.get(module), this));
    
        JTextField txtFilter = ComponentFactory.getShortTextField(255);
        txtFilter.addKeyListener(this);        

        JPanel panel2 = new JPanel();
        panel2.setLayout(Layout.getGBL());
        panel2.add(ComponentFactory.getLabel(DcResources.getText("lblFilter")), Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 5), 0, 0));
        panel2.add(txtFilter, Layout.getGBC( 1, 0, 1, 1, 100.0, 100.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        getContentPane().add(panel2, Layout.getGBC( 0, 0, 2, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 10, 5, 0, 5), 0, 0));
        
        getContentPane().add(panel, Layout.getGBC( 0, 1, 1, 1, 80.0, 80.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(0, 0, 0, 0), 0, 0));
        
        if (!readonly) {
            getContentPane().add(panelActions,  Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                     new Insets(0, 0, 0, 0), 0, 0));
            getContentPane().add(statusPanel,  Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(0, 0, 0, 0), 0, 0));
        }
    }
    
    private void delete(Collection<DcObject> items, boolean silent) {
        if (items.size() > 0) {
        	if (isTaskRunning() || (!silent && !GUI.getInstance().displayQuestion("msgDeleteQuestion"))) 
                return;
            
            task = new DeleteItemTask();
            task.addItems(items);
            task.addClient(this);
            task.setSilent(silent);
            task.setModule(getModuleIdx());
            
            Connector connector = DcConfig.getInstance().getConnector();
            connector.executeTask(task);
        } else {
            GUI.getInstance().displayWarningMessage("msgSelectItemToDel");
        }
    }
    
    public void deleteUnused() {
        if (isTaskRunning() || !GUI.getInstance().displayQuestion(
                DcResources.getText("msgDeleteQuestionUnusedItems", DcModules.get(module).getObjectNamePlural()))) 
            return;
        
        Collection<DcObject> objects = list.getItems();
        delete(objects, true);
    }
    
    public void delete() {
        cancelled = false;
        Collection<DcObject> objects = list.getSelectedItems();
        delete(objects, false);
    }  
    
    @Override
    public void keyPressed(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}    
    
    @Override
    public void keyReleased(KeyEvent e) {
        DcShortTextField txtFilter = (DcShortTextField) e.getSource();
        String filter = txtFilter.getText();
        
        if (filter.trim().length() == 0) {
            ((DcListModel<Object>) list.getModel()).clear();
            list.addElements(all);
        } else {
            List<DcListElement> filtered = new ArrayList<DcListElement>();
            for (DcListElement el : all) {
                DcObjectListElement element = (DcObjectListElement) el;
                if (element.getDcObject().toString().toLowerCase().contains(filter.toLowerCase()))
                    filtered.add(el);
            }
        
            ((DcListModel<Object>) list.getModel()).clear();
            list.addElements(filtered);
        }
    }    
    
    @Override
    public void mouseReleased(MouseEvent e) {
        if (!readonly) {
            if (SwingUtilities.isRightMouseButton(e)) {
                if (list.getSelectedIndex() == -1) {
                    int index = list.locationToIndex(e.getPoint());
                    list.setSelectedIndex(index);
                }
                
                if (list.getSelectedIndex() > -1) {
                    JPopupMenu menu = getPopupMenu();                
                    menu.setInvoker(list);
                    menu.show(list, e.getX(), e.getY());
                }
            }

            if (e.getClickCount() == 2 && list.getSelectedIndex() > -1) 
                open();
        }
    }
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("createNew"))
            createNew();
        else if (e.getActionCommand().equals("close"))
            close();
        else if (e.getActionCommand().equals("createMultiple"))
            createMultiple();
        else if (e.getActionCommand().equals("delete"))
            delete();
        else if (e.getActionCommand().equals("open_readonly"))
            open(false);        
        else if (e.getActionCommand().equals("open_edit"))
            open(true);
        else if (e.getActionCommand().equals("delete_unused"))
            deleteUnused();
        else if (e.getActionCommand().equals("merge"))
            mergeItems();
    }
    
    private class DcMinimalisticItemViewMenu extends DcMenuBar {
        
        private DcModule module;
        
        public DcMinimalisticItemViewMenu(DcModule module, DcMinimalisticItemView parent) {
            this.module = module;
            build(parent);
        }
        
        private void build(DcMinimalisticItemView parent) {
            DcMenu menuEdit = ComponentFactory.getMenu(DcResources.getText("lblEdit"));
            
            DcMenuItem miOpen = new DcMenuItem(DcResources.getText("lblOpen"));
            DcMenuItem miEdit = new DcMenuItem(DcResources.getText("lblEdit"));
            DcMenuItem miAdd = new DcMenuItem(DcResources.getText("lblNewItem", ""));
            DcMenuItem miDelete = new DcMenuItem(DcResources.getText("lblDelete"));
            DcMenuItem miDeleteUnused = new DcMenuItem(DcResources.getText("lblDeleteUnassigned", module.getObjectNamePlural()));
            DcMenuItem miMerge = new DcMenuItem(DcResources.getText("lblMergeItems", getModule().getObjectNamePlural()));
            
            miOpen.addActionListener(parent);
            miEdit.addActionListener(parent);
            miAdd.addActionListener(parent);
            miDelete.addActionListener(parent);
            miDeleteUnused.addActionListener(parent);
            miMerge.addActionListener(parent);
            
            miOpen.setActionCommand("open_readonly");
            miEdit.setActionCommand("open_edit");
            miAdd.setActionCommand("createNew");
            miDelete.setActionCommand("delete");
            
            miDeleteUnused.setActionCommand("delete_unused");
            miMerge.setActionCommand("merge");
            
            miOpen.setIcon(IconLibrary._icoOpen);
            miEdit.setIcon(IconLibrary._icoOpen);
            miAdd.setIcon(IconLibrary._icoAdd);
            miDelete.setIcon(IconLibrary._icoDelete);
            miDeleteUnused.setIcon(IconLibrary._icoDelete);
            miMerge.setIcon(IconLibrary._icoMerge);
            
            menuEdit.add(miOpen);
            menuEdit.add(miEdit);
            menuEdit.add(miAdd);
            menuEdit.addSeparator();
            menuEdit.add(miDelete);
            
            if (module.getType() != DcModule._TYPE_TEMPLATE_MODULE) {
                menuEdit.add(miDeleteUnused);
                menuEdit.addSeparator();
                menuEdit.add(miMerge);
            }
            
            add(menuEdit);
            
            if (module.getType() != DcModule._TYPE_TEMPLATE_MODULE) {
                DcMenu menuSettings = ComponentFactory.getMenu(DcResources.getText("lblSettings"));
                PluginHelper.add(menuSettings, "ItemFormSettings", module.getIndex());
                PluginHelper.add(menuSettings, "FieldSettings", module.getIndex());
                add(menuSettings);
            }
        }
    }

    @Override
    public void applySettings() {
        setFont(null);
    }

	@Override
	public void notifyItemsMerged() {
		load();
	}
}