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

package org.datacrow.client.console.windows.enhancers;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcLongTextField;
import org.datacrow.client.console.windows.DcDialog;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.data.DataFilters;
import org.datacrow.core.data.DcResultSet;
import org.datacrow.core.enhancers.AutoIncrementer;
import org.datacrow.core.enhancers.IValueEnhancer;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.definitions.DcFieldDefinition;

public class AutoIncrementDialog extends DcDialog implements ActionListener {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(AutoIncrementDialog.class.getName());
    
    private final AutoIncrementSettingsPanel pSettings = new AutoIncrementSettingsPanel();
    
    private final JProgressBar progressBar = new JProgressBar();
    
    private final JComboBox<Object> comboModus = ComponentFactory.getComboBox();
    private final JComboBox<Object> comboOrderBy1 = ComponentFactory.getComboBox();
    private final JComboBox<Object> comboOrderBy2 = ComponentFactory.getComboBox();
    private final JComboBox<Object> comboOrderBy3 = ComponentFactory.getComboBox();
    
    private final JButton buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
    private final JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
    private final JButton buttonRun = ComponentFactory.getButton(DcResources.getText("lblRun"));

    private boolean canceled = false;
    
    public AutoIncrementDialog() {
        super(GUI.getInstance().getMainFrame());
        
        setIconImage(IconLibrary._icoRenumber.getImage());

        buildDialog();
        
        setHelpIndex("dc.tools.autonumbering");
        setTitle(DcResources.getText("lblAutoNumbering"));

        setSize(new Dimension(600, 600));
        setModal(true);
        setCenteredLocation();
    }

    private void save() {
        pSettings.save();
    }
    
    public void initProgressBar(int maxValue) {
        progressBar.setValue(0);
        progressBar.setMaximum(maxValue);
    }

    public void updateProgressBar() {
        int current = progressBar.getValue();
        progressBar.setValue(current + 1);
    }    

    private void buildDialog() {
        getContentPane().setLayout(Layout.getGBL());

        /***********************************************************************
         * Renumber
         **********************************************************************/
        JPanel panelRenumber = new JPanel(false);
        panelRenumber.setLayout(Layout.getGBL());
        
        buttonRun.addActionListener(this);
        buttonRun.setActionCommand("renumber");
        
        DcLongTextField explanation = ComponentFactory.getLongTextField();
        ComponentFactory.setUneditable(explanation);
        
        DcModule module = DcModules.getCurrent();
        
        explanation.setText(DcResources.getText("lblRenumberExplanation", module.getObjectNamePlural()));
        
        JLabel labelOrderBy = ComponentFactory.getLabel(DcResources.getText("lblSortOn"));
        comboOrderBy1.addItem("");
        comboOrderBy2.addItem("");
        comboOrderBy3.addItem("");
        
        DcField field;
        for (DcFieldDefinition definition : module.getFieldDefinitions().getDefinitions()) {
            field = module.getField(definition.getIndex());
            if (field.isSearchable() && !field.isUiOnly()) {
                comboOrderBy1.addItem(field);
                comboOrderBy2.addItem(field);
                comboOrderBy3.addItem(field);
            }
        }
        
        JLabel labelMode = ComponentFactory.getLabel(DcResources.getText("lblRenumberMode"));
        comboModus.addItem(DcResources.getText("lblRestartNumbering"));
        comboModus.addItem(DcResources.getText("lblFillGapsOnly"));
        
        panelRenumber.add(explanation,   Layout.getGBC(0, 0, 2, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));
        panelRenumber.add(labelMode,  Layout.getGBC(0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));        
        panelRenumber.add(comboModus, Layout.getGBC(1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));        
        
        panelRenumber.add(labelOrderBy,  Layout.getGBC(0, 2, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));        
        panelRenumber.add(comboOrderBy1, Layout.getGBC(1, 2, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));        
        panelRenumber.add(comboOrderBy2, Layout.getGBC(1, 3, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));        
        panelRenumber.add(comboOrderBy3, Layout.getGBC(1, 4, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        
        JButton buttonCancel = ComponentFactory.getButton(DcResources.getText("lblCancel"));
        buttonCancel.addActionListener(this);
        buttonCancel.setActionCommand("cancel");
        
        JPanel panel = new JPanel();
        panel.add(buttonRun);
        panel.add(buttonCancel);        
        
        panelRenumber.add(panel,     Layout.getGBC(1, 5, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        panelRenumber.add(progressBar,   Layout.getGBC(0, 6, 2, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));        
        
        /***********************************************************************
         * MAIN PANEL
         **********************************************************************/
        pSettings.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblSettings")));
        panelRenumber.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblRenumberAll")));
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        
        buttonSave.addActionListener(this);
        buttonSave.setActionCommand("save");
        
        getContentPane().add(pSettings,     Layout.getGBC(0, 0, 1, 1, 5.0, 5.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(buttonSave,    Layout.getGBC(0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 10), 0, 0));
        getContentPane().add(panelRenumber, Layout.getGBC(0, 2, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(buttonClose,  Layout.getGBC(0, 3, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 10), 0, 0));        

        pack();
        setCenteredLocation();
    }
    
    private void cancel() {
        canceled = true;
    }    
    
    private void renumber() {
        save();
        
        DcField field1 = comboOrderBy1.getSelectedIndex() > 0 ? (DcField) comboOrderBy1.getSelectedItem() : null;
        DcField field2 = comboOrderBy2.getSelectedIndex() > 0 ? (DcField) comboOrderBy2.getSelectedItem() : null;
        DcField field3 = comboOrderBy3.getSelectedIndex() > 0 ? (DcField) comboOrderBy3.getSelectedItem() : null;
        
        int total = 0;
        if (field1 != null)
            total++;
        if (field2 != null)
            total++;
        if (field3 != null)
            total++;      
        
        DcField[] fields = new DcField[total];
        
        if (fields.length >= 1)
            fields[0] = field1;
        if (fields.length >= 2)
            fields[1] = field2;
        if (fields.length >= 3)
            fields[2] = field3;       
        
        Renumberer renumberer = new Renumberer(fields, comboModus.getSelectedIndex());
        canceled = false;
        renumberer.start();
    }

    private class Renumberer extends Thread {
        
        private final DcField[] ordering;
        private final int modus;
        private DcModule module = DcModules.getCurrent();
        
        public Renumberer(DcField[] ordering, int modus) {
            this.ordering = ordering;
            this.modus = modus;
        }
        
        @Override
        public void run() {
            boolean active = false;
            initiliazeUI(module);
            
            DcField field;
            IValueEnhancer[] enhancers;
            for (Iterator<DcField> iter = module.getFields().iterator(); iter.hasNext() && !canceled; ) {
                field =  iter.next();
                enhancers = field.getValueEnhancers();
                for (int i = 0; i < enhancers.length && !canceled; i++) {
                    if (enhancers[i].isEnabled() && enhancers[i] instanceof AutoIncrementer) {
                        active = true;
                        renumber((AutoIncrementer) enhancers[i], field);
                    }
                }
            }
            
            if (!active && !canceled) {
                GUI.getInstance().displayErrorMessage("msgNoRenumbersFound");
            } else {
                int moduleIdx = module.getIndex();
                DcConfig dcc = DcConfig.getInstance();
                Connector conn = dcc.getConnector();
                
                Map<String, Integer> keys = conn.getKeys(DataFilters.getCurrent(moduleIdx));
                GUI.getInstance().getSearchView(moduleIdx).add(keys);
            }
        }
        
        private void initiliazeUI(DcModule module) {
            int totalEnhancers = 0;
            
            IValueEnhancer[] enhancers;
            for (DcField field : module.getFields()) {
                enhancers = field.getValueEnhancers();
                for (int i = 0; i < enhancers.length; i++) {
                    totalEnhancers = enhancers[i].isEnabled() && enhancers[i] instanceof AutoIncrementer ? 
                                     totalEnhancers + 1 : totalEnhancers;
                }
            }            
            
            try {
                DcConfig dcc = DcConfig.getInstance();
                Connector conn = dcc.getConnector();
                
                DcResultSet result = conn.executeSQL("SELECT COUNT(ID) AS TOTAL FROM " + module.getTableName());
                int total = 0;
                if (result.getRowCount() > 0) {
                    total = result.getInt(0, 0);
                }
                
                initProgressBar(total * totalEnhancers);
            } catch (Exception e) {
                logger.error("Could not retrieve the total item count", e);
            }
        }
        
        private void renumber(AutoIncrementer incrementer, DcField field) {
            buttonClose.setEnabled(false);
            buttonRun.setEnabled(false);
            buttonSave.setEnabled(false);
            
            String collation = DcSettings.getString(DcRepository.Settings.stDatabaseLanguage);
            String order = "";
            for (int i = 0; i < ordering.length; i++) {
            	
            	if (ordering[i] != null) {
            	
	                if (order.length() > 0)
	                    order += ", ";
	                
	                order += ordering[i].getDatabaseFieldName();
	                if  (ordering[i].getValueType() == DcRepository.ValueTypes._STRING)
	                	order += " COLLATE \"" + collation + " 0\" ";
            	}
            }
            
            String qry = "SELECT ID, " + field.getDatabaseFieldName() + " FROM " + module.getTableName();
            
            if (order.length() > 0)
                qry += " ORDER BY " + order; 
            
            
            String qryCurrent = "SELECT " + field.getDatabaseFieldName() + " FROM " + module.getTableName() + 
                                " WHERE " + field.getDatabaseFieldName() + " IS NOT NULL AND " +
                                field.getDatabaseFieldName() + " > 0 " +
                                "ORDER BY 1" +
                                (field.getValueType() == DcRepository.ValueTypes._STRING ||
                                 field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE 
                                ? " COLLATE \"" + collation + " 0\" " :"");

            Connector conn = DcConfig.getInstance().getConnector();
            Collection<Integer> currentValues = new ArrayList<Integer>();
            try {
                DcResultSet result = conn.executeSQL(qryCurrent);
                for (int row = 0; row < result.getRowCount(); row++) {
                	try {
                		currentValues.add(Integer.valueOf(result.getInt(row, 0)));
                	} catch (Exception e) {
                		logger.debug("Could not get current value for row " + row, e);
                	}
                }
                
                result = conn.executeSQL(qry);
                int counter = 0;
                String ID;
                int current = 0;
                boolean allow = false;
                boolean currentfound = false;
                int x;
                String updateQuery;
                
                for (int row = 0; row < result.getRowCount(); row++) {
                    
                    if (canceled) break;
                    
                    ID = result.getString(row, 0);
                    
                    try {
                        current = result.getInt(row, 1);
                    } catch (Exception e) {
                        logger.debug("NullPointer was thrown when getting Int for Auto Incrementer. Ignored.");
                    }
                    
                    allow = false;
                    if (modus == 1 && current == 0) {

                        counter = counter + incrementer.getStep();
                        
                        if (currentValues.contains(counter)) {
                            currentfound = false;
                            for (Integer val : currentValues) {
                                x = val.intValue();
                                while (!currentfound && x == counter) {
                                    counter += incrementer.getStep();
                                }
                            }
                        }
                        
                        allow = true;
                        
                    } else if (modus == 0) {
                        counter = counter + incrementer.getStep();
                        allow = true;
                    }
                    
                    if (allow) {
                        updateQuery = "UPDATE " + module.getTableName() + 
                                             " SET " + field.getDatabaseFieldName() + " = " + counter  + 
                                             " WHERE ID = '" + ID + "'";
                        conn.executeSQL(updateQuery);
                    }
                    
                    updateProgressBar();
                }
                
            } catch (Exception e) {
                logger.error("An error occurred", e);
            } finally {
                if (buttonRun != null) {
                    buttonClose.setEnabled(true);
                    buttonRun.setEnabled(true);
                    buttonSave.setEnabled(true);
                }
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("renumber"))
            renumber();
        else if (ae.getActionCommand().equals("close"))
            close();
        else if (ae.getActionCommand().equals("cancel"))
            cancel();
        else if (ae.getActionCommand().equals("save"))
            save();
    }
}
