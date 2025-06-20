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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcButton;
import org.datacrow.client.console.components.DcLongTextField;
import org.datacrow.client.console.components.DcObjectComboBox;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.clients.IClient;
import org.datacrow.core.data.DataFilter;
import org.datacrow.core.data.DataFilterEntry;
import org.datacrow.core.data.Operator;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcProperty;
import org.datacrow.core.objects.DcTemplate;
import org.datacrow.core.objects.template.Templates;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.CoreUtilities;

public class MergeItemsDialog extends DcDialog implements ActionListener, IClient {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(MergeItemsDialog.class.getName());

    private final JTextArea textLog = ComponentFactory.getTextArea();
    private final DcButton buttonApply = ComponentFactory.getButton(DcResources.getText("lblApply"));
    private final DcButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
    private final JProgressBar progressBar = new JProgressBar();
    
    private final Collection<IMergeItemsListener> listeners = new ArrayList<>();

    private final DcModule module;
    private final Collection<? extends DcObject> items;
    
    private DcObjectComboBox cbItems;

    private boolean canceled = false;
    
    public MergeItemsDialog(Collection<? extends DcObject> items, DcModule module) {
        super(GUI.getInstance().getRootFrame());
        
        this.setTitle(DcResources.getText("lblMergeItems", module.getObjectNamePlural()));
        this.module = module;
        this.items = items;
        
        if (module.getType() == DcModule._TYPE_PROPERTY_MODULE) {
        	setHelpIndex("dc.items.mergepropertyitems");
        } else {
        	setHelpIndex("dc.items.mergeitems");
        }

        build();

        setSize(DcSettings.getDimension(DcRepository.Settings.stMergeItemsDialogSize));
        setCenteredLocation();
    }

    private void replace() {
        DcObject target = (DcObject) cbItems.getSelectedItem();
        
        if (items.contains(target)) {
            GUI.getInstance().displayWarningMessage(DcResources.getText("msgMergeTargetSameAsSource"));
        } else {
        	buttonApply.setEnabled(false);
        	
            MergeTask task = new MergeTask(this, items, target);
            task.start();
        }
    }
    
    public void addListener(IMergeItemsListener listener) {
    	listeners.add(listener);
    }

    @Override
    public void notify(String msg) {
        if (textLog != null && msg != null) 
            textLog.insert(msg + '\n', 0);
    }
    
    @Override
    public void notifyError(Throwable t) {
        notify(t.getMessage());
        logger.error(t, t);
        GUI.getInstance().displayErrorMessage(t.getMessage());
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
        progressBar.setValue(progressBar.getMaximum());
        
        notifyListeners();
    }

    @Override
    public void notifyTaskStarted(int taskSize) {
        buttonApply.setEnabled(false);
        cbItems.setEnabled(false);
        
        progressBar.setValue(0);
        progressBar.setMaximum(taskSize);   
        
        textLog.setText("");
    }

    @Override
    public void notifyProcessed() {
        int value = progressBar.getValue();
        value = value < progressBar.getMaximum() ? value + 1 : 1;
        progressBar.setValue(value);
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public void close() {
        canceled = true;
        
        DcSettings.set(DcRepository.Settings.stMergeItemsDialogSize, getSize());
        
        if (items != null) items.clear();
        
        super.close();
    }

    private void build() {

        //**********************************************************
        //Overview panel
        //**********************************************************

        JPanel panelOverview = new JPanel();
        panelOverview.setLayout(Layout.getGBL());
        
        DcLongTextField txtExplanation = ComponentFactory.getLongTextField();
        
        txtExplanation.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        txtExplanation.setEditable(false);
        txtExplanation.setText(DcResources.getText("msgSelectedSourceItemsForMerge"));
        
        DcLongTextField txtSelectedItems = ComponentFactory.getLongTextField();
        txtSelectedItems.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        txtSelectedItems.setEditable(false);
        JScrollPane itemScroller = new JScrollPane(txtSelectedItems);
        itemScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        String s = "";
        for (DcObject dco : items)
        	s += (s.length() > 0 ? ", " : "") + dco.toString();

        txtSelectedItems.setText(s);
        
        itemScroller.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblSelectedItems")));
        
        panelOverview.add(txtExplanation, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
        panelOverview.add(itemScroller, Layout.getGBC( 0, 1, 1, 1, 20.0, 20.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                     new Insets(5, 0, 0, 0), 0, 0));
    	
        //**********************************************************
        //Input panel
        //**********************************************************
        cbItems = ComponentFactory.getObjectCombo(module.getIndex());
        cbItems.remove(items);
        
        JPanel panelInput = new JPanel();
        panelInput.setLayout(Layout.getGBL());
        
        panelInput.add(ComponentFactory.getLabel(DcResources.getText("lblMergeTarget")), Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets(0, 0, 0, 0), 0, 0));
        panelInput.add(cbItems, Layout.getGBC(1, 0, 1, 1, 100.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 10, 0, 0), 0, 0));
        
        //**********************************************************
        //Action panel
        //**********************************************************
        JPanel panelActions = new JPanel();
        
        buttonApply.addActionListener(this);
        buttonApply.setActionCommand("replace");
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");

        panelActions.add(buttonApply);
        panelActions.add(buttonClose);
        
        //**********************************************************
        //Progress panel
        //**********************************************************        
        JPanel panelProgress = new JPanel();
        panelProgress.setLayout(Layout.getGBL());
        panelProgress.add(progressBar, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                         ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                          new Insets(5, 5, 5, 5), 0, 0));  
        
        //**********************************************************
        //Log Panel
        //**********************************************************
        JPanel panelLog = new JPanel();
        panelLog.setLayout(Layout.getGBL());

        textLog.setEditable(false);
        JScrollPane logScroller = new JScrollPane(textLog);
        logScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelLog.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblLog")));
        panelLog.add(logScroller, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                     new Insets(5, 5, 5, 5), 0, 0));
        
        //**********************************************************
        //Main panel
        //**********************************************************
        this.getContentPane().setLayout(Layout.getGBL());

        this.getContentPane().add(panelOverview, Layout.getGBC(0, 0, 1, 1, 20.0, 20.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 10, 5, 10), 0, 0));
        this.getContentPane().add(panelInput  ,Layout.getGBC(0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 10, 5, 10), 0, 0));
          this.getContentPane().add(panelActions,Layout.getGBC(0, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets(5, 5, 5, 5), 0, 0));
          this.getContentPane().add(panelProgress,Layout.getGBC(0, 3, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelLog,Layout.getGBC(0, 4, 1, 1, 20.0, 20.0
                ,GridBagConstraints.SOUTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        
        pack();
    }
    
    public void notifyListeners() {
    	for (IMergeItemsListener listener : listeners) {
    		listener.notifyItemsMerged();
    	}
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("replace"))
            replace();
    }
    
    private class MergeTask extends Thread {
        
        private Collection<? extends DcObject> items;
        private DcObject target;
        
        private IClient client;
        
        private MergeTask(IClient client, Collection<? extends DcObject> items, DcObject target) {
            this.items = items;
            this.target = target;
            this.client = client;
        }
        
        @Override
        public void run() {
            Collection<DcObject> result = merge();
            
            if (result.size() > 0) {
                save(result);    
            } else {
                client.notify(DcResources.getText("msgSavingItemsNotNeeded"));
            }
            
            deleteItems();
            
            if (target.getModule().getType() == DcModule._TYPE_PROPERTY_MODULE) {
                String alternatives = (String) target.getValue(DcProperty._C_ALTERNATIVE_NAMES);
                alternatives = alternatives == null ? "" : alternatives;
                String altName;
                for (DcObject item : items) {
                    altName = (String) item.getValue(DcProperty._A_NAME);
                    
                    if (!CoreUtilities.isEmpty(altName) && !alternatives.toLowerCase().contains(";" + altName.toLowerCase() + ";")) {
                        alternatives += alternatives.endsWith(";") ? "" : ";";
                        alternatives += altName + ";";
                    }
                }
                
                target.setValue(DcProperty._C_ALTERNATIVE_NAMES, alternatives);
                
                try {
                    DcConfig.getInstance().getConnector().saveItem(target);
                } catch (Exception e) {
                    client.notifyError(e);
                }
            }
            
            client.notify(DcResources.getText("msgMergeCompleted"));
            GUI.getInstance().displayMessage(DcResources.getText("msgMergeCompleted"));
            client.notifyTaskCompleted(true, "");
            
            client = null;
            items = null;
            target = null;
        }
        
        private void deleteItems() {
            client.notifyTaskStarted(items.size());
            client.notify(DcResources.getText("msgDeletingReplacedItems", String.valueOf(items.size())));
            
            Connector connector = DcConfig.getInstance().getConnector();
            for (DcObject dco : items) {
                
                if (client.isCancelled()) break;
                
                try {
                    connector.deleteItem(dco);
                    
                    try {
                        sleep(100);
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                    
                } catch (Exception e) {
                    GUI.getInstance().displayErrorMessage(e.getMessage());
                    logger.error(e, e);
                }
            }
            
        }
        
        private void save(Collection<DcObject> c) {
            
            client.notifyTaskStarted(items.size());
            
            try {
                
                Connector connector = DcConfig.getInstance().getConnector();
                
                for (DcObject dco : c) {
                    
                    if (client.isCancelled()) break;
                    
                    client.notify(DcResources.getText("msgSavingItem", dco.toString()));
                    dco.setUpdateGUI(false);
                    connector.saveItem(dco);
                    
                    try {
                        sleep(100);
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                }
            
            } catch (Exception e) {
                GUI.getInstance().displayErrorMessage(e.getMessage());
                logger.error(e, e);
            } finally {
                GUI.getInstance().getSearchView(DcModules.getCurrent().getIndex()).refresh();
            }
        }
        
        private Collection<DcObject> getApplicableTemplates(DcModule module, DcField field) {
            Collection<DcObject> result = new ArrayList<DcObject>();
            
            List<DcTemplate> templates = Templates.getTemplates(module.getIndex());
            boolean hasReference = false;
            for (DcTemplate template : templates) {
                
                if (client.isCancelled()) break;
                
                Object o = template.getValue(field.getIndex());
                
                if (o == null) continue;
                
                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                    @SuppressWarnings("unchecked")
					Collection<DcObject> mappings = (Collection<DcObject>) template.getValue(field.getIndex());
                    
                    if (mappings == null) continue;
                    
                    for (DcObject mapping : mappings) { // loop through mappings
                        for (DcObject reference : items) { // loop through to be replaced values
                            if (mapping.getValue(DcMapping._B_REFERENCED_ID).equals(reference.getID()))
                                hasReference = true;
                        }
                    }
                } else {
                    for (DcObject reference : items) {
                        if (o.equals(reference))
                            hasReference = true;
                    }
                }
                
                if (hasReference)
                    result.add(template);
            }
            
            return result;
        }
        
        private Collection<DcObject> merge() {
            Collection<DcObject> c = new ArrayList<DcObject>();
            
            try {
                // loop through all modules
                for (DcModule mm : DcModules.getReferencingModulesAll(module.getIndex())) {
                    
                    if (client.isCancelled()) break;
                    
                    if (    mm.isAbstract() || 
                            mm.getType() == DcModule._TYPE_MAPPING_MODULE ||
                            mm.getType() == DcModule._TYPE_TEMPLATE_MODULE) continue;
                    
                    client.notify(DcResources.getText("msgProcessingItemsFromModule", mm.getName()));
                    
                    try {
                        sleep(100);
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                    
                    // loop through all the field of the module
                    client.notifyProcessed();
                    Connector connector = DcConfig.getInstance().getConnector();
                    for (DcField  field : mm.getFields()) {
                        
                        if (client.isCancelled()) break;
                        
                        if (field.getReferenceIdx() == module.getIndex()) {
                            DataFilter df = new DataFilter(mm.getIndex());
                           
                            // check for references to one of the to be removed items
                            for (DcObject reference : items)
                                df.addEntry(new DataFilterEntry(DataFilterEntry._OR, mm.getIndex(), field.getIndex(), Operator.CONTAINS, reference));
                           
                            Collection<Integer> include = new ArrayList<Integer>();
                            include.add(field.getIndex());
                            int[] fields = mm.getMinimalFields(include);
                            
                            client.notify(DcResources.getText("msgQueryingForReferences"));
                            Collection<DcObject> mmItems = connector.getItems(df, fields);
                            
                            // add the templates - check if they have a reference and in case they do add them to the overall result.
                            mmItems.addAll(getApplicableTemplates(mm, field));
                            
                            // loop through each of the referencing items
                            Collection<DcObject> removals = new ArrayList<DcObject>();
                            for (DcObject dco : mmItems) {
                    
                                if (client.isCancelled()) break;
                                
                                client.notify(DcResources.getText("msgProcessingItem", dco.toString()));
                                
                                // remove old reference for multi-reference fields
                                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                                
                                    @SuppressWarnings("unchecked")
									Collection<DcObject> mappings = (Collection<DcObject>) dco.getValue(field.getIndex());
                                    
                                    if (mappings == null) continue;
                                   
                                    for (DcObject mapping : mappings) { // loop through mappings
                                        for (DcObject reference : items) { // loop through to be replaced values
                                            if (mapping.getValue(DcMapping._B_REFERENCED_ID).equals(reference.getID()))
                                                removals.add(mapping);
                                        }
                                    }
                                   
                                    for (DcObject removal : removals)
                                        mappings.remove(removal);
                                }
                                
                                client.notifyProcessed();
                               
                                if (client.isCancelled()) break;
                                
                                // apply the target value and add to the results.
                                dco.createReference(field.getIndex(), target);
                                dco.setChanged(field.getIndex(), true);
                                c.add(dco);
                                try {
                                    sleep(100);
                                } catch (Exception e) {
                                    logger.error(e, e);
                                }
                            }   
                        }
                    }
                }
                
                client.notify(DcResources.getText("msgProcessingOfItemsSuccess"));
                
            } catch (Exception e) {
                GUI.getInstance().displayErrorMessage(e.getMessage());
                client.notify(e.getMessage());
                logger.error(e, e);
            }
            
            return c;
        }
    }
}