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
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcProgressBar;
import org.datacrow.client.console.components.tables.DcTable;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.clients.IClient;
import org.datacrow.core.console.IView;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;
import org.datacrow.core.settings.DcSettings;

public class FindReplaceTaskDialog extends DcDialog implements ActionListener, IClient {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(FindReplaceTaskDialog.class.getName());
    
    private final JProgressBar pb = new DcProgressBar();
    
    private boolean stopped = false;
    
    private final JButton buttonApply;
    private final JButton buttonClose;

    private final DcModule module;
    private final IView view;
    
    private final int[] fields;
    private final int field;

    private final Object replacement;
    private final Object value;
    private final List<DcObject> items;
    
    private DcTable tblItems;

    public FindReplaceTaskDialog(
            JFrame parent, 
            IView view, 
            List<DcObject> items, 
            int[] fields, 
            int field, 
            Object value, 
            Object replacement) {
        
        super(parent);
        setTitle(DcResources.getText("lblPreview"));
        
        this.view = view;
        this.module = view.getModule();
        this.items = items;
        this.field = field; 
        this.replacement = replacement;
        this.value = value;
        this.fields = fields;
        
        setHelpIndex("dc.tools.findreplace");
        
        buttonApply = ComponentFactory.getButton(DcResources.getText("lblApply"));
        buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));        
        
        buildDialog(module);
        setSize(DcSettings.getDimension(DcRepository.Settings.stFindReplaceTaskDialogSize));

        setCenteredLocation();
    }

    private void replace() {
        Updater updater = new Updater(this);
        updater.start();
    }


    @Override
    public void notify(String msg) {
    	logger.info(msg);
    }
    
    @Override
    public void notifyError(Throwable t) {
    	logger.error(t.getMessage());
    }

    @Override
    public void notifyWarning(String msg) {
    	logger.warn(msg);
    }
    
    @Override
    public boolean askQuestion(String msg) {
        return GUI.getInstance().displayQuestion(msg);
    }

    @Override
    public void notifyTaskCompleted(boolean success, String taskID) {
        pb.setValue(pb.getMaximum());
        buttonApply.setEnabled(true);
        close();
    }

    @Override
    public void notifyTaskStarted(int taskSize) {
        pb.setValue(0);
        pb.setMaximum(taskSize);
        
        buttonApply.setEnabled(false);
    }

    @Override
    public void notifyProcessed() {
        pb.setValue(pb.getValue() + 1);
    }

    @Override
    public boolean isCancelled() {
        return stopped;
    }

    private class Updater extends Thread {
        
        private IClient client;
        
        public Updater(IClient client) {
            this.client = client;
        }
        
        @Override
        public void run() {
            
            client.notifyTaskStarted(items.size());
            view.setListSelectionListenersEnabled(false);

            try {
                DcObject item = module.getItem();
                int colID = tblItems.getColumnIndexForField(DcObject._ID);
                int colValue = tblItems.getColumnIndexForField(field);
                int colParent = -1;
                
                if (item.getParentReferenceFieldIndex() > 0)
                    colParent = tblItems.getColumnIndexForField(item.getParentReferenceFieldIndex());
                
                Connector connector = DcConfig.getInstance().getConnector();
                String ID;
                for (int row = 0; row < tblItems.getRowCount(); row++) {
                    
	                if (client.isCancelled()) break;
	                
	                ID = (String) tblItems.getValueAt(row, colID, true);
	                
	                item.markAsUnchanged();
	                item.setNew(false);
	                item.setValueLowLevel(DcObject._ID, ID);
	                item.setValue(field, tblItems.getValueAt(row, colValue, true));
	                
	                if (colParent > -1)
	                    item.setValueLowLevel(item.getParentReferenceFieldIndex(), tblItems.getValueAt(row, colParent, true));
	                
	                try {
	                    item.setUpdateGUI(false);
	                    connector.saveItem(item);
                    } catch (Exception e) {
                        // warn the user of the event that occurred (for example an incorrect parent for a container)
                        GUI.getInstance().displayErrorMessage(e.getMessage());
                    }

	                client.notifyProcessed();

	                try {
	                    sleep(100);
	                } catch (Exception e) {
	                    logger.error(e, e);
	                }
	            }
            } finally {
                GUI.getInstance().getSearchView(module.getIndex()).refresh();
                
            	if (view != null) {
            	    view.setListSelectionListenersEnabled(true);
            	}
            	
            	client.notifyTaskCompleted(true, null);
            }
        }
    }

    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stFindReplaceTaskDialogSize, getSize());
        
        tblItems.clear();
        items.clear();
        
        super.close();
    }

    private void buildDialog(DcModule module) {
        //**********************************************************
        //Input panel
        //**********************************************************
        JPanel panelInput = new JPanel();
        panelInput.setLayout(Layout.getGBL());
        
        tblItems = ComponentFactory.getDCTable(module, false, false);
        JScrollPane sp = new JScrollPane(tblItems);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tblItems.activate();
        tblItems.setVisibleColumns(fields);
        
        Object oldValue;
        Object newValue;
        String s;
        for (DcObject item : items) {
            oldValue = (Object) item.getValue(field);
            
            if (oldValue instanceof String) {
                try {
                    s = (String) value;
                    newValue = ((String) oldValue).replaceAll("(?i)" + s, (String) replacement);
                    item.setValue(field, newValue);
                } catch (Exception e) {
                    logger.error(e, e);
                    GUI.getInstance().displayErrorMessage(e.getMessage());
                    break;
                }
            } else if (oldValue instanceof Collection) {
                @SuppressWarnings("unchecked")
				Collection<DcObject> collection = (Collection<DcObject>) oldValue;
                for (DcObject o : collection) {
                    oldValue = o;
                    if (o.getValue(DcMapping._B_REFERENCED_ID).equals(((DcObject) value).getID()))
                        break;
                }
                
                if (oldValue != null)
                    collection.remove(oldValue);

                item.addMapping((DcObject) replacement, field);
                
            } else {
                item.setValue(field, replacement);
            }
            
            tblItems.add(item);
        }

        panelInput.add(sp, Layout.getGBC( 0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));        
  
        //**********************************************************
        //Action panel
        //**********************************************************
        JPanel panelActions = new JPanel();

        buttonApply.addActionListener(this);
        buttonApply.setActionCommand("start");
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");

        panelActions.add(buttonApply);
        panelActions.add(buttonClose);
        
        //**********************************************************
        //Progress panel
        //**********************************************************        
        JPanel panelProgress = new JPanel();
        panelProgress.setLayout(Layout.getGBL());
        panelProgress.add(pb, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                         ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                          new Insets(5, 5, 5, 5), 0, 0));        
        
        //**********************************************************
        //Main panel
        //**********************************************************
        this.getContentPane().setLayout(Layout.getGBL());
        this.getContentPane().add(panelInput  ,Layout.getGBC(0, 0, 1, 1, 20.0, 20.0
                                              ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                               new Insets(5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelActions,Layout.getGBC(0, 2, 1, 1, 1.0, 1.0
                                              ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelProgress, Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
                                              ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                                              new Insets( 0, 0, 0, 0), 0, 0));        

        pack();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close")) {
            stopped = true;
            close();
        } else if (ae.getActionCommand().equals("start")) {
            replace();
        }
    }  
}
