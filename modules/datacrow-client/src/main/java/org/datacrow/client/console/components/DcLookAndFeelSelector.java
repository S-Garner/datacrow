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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.windows.DcDialog;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.settings.objects.DcLookAndFeel;

public class DcLookAndFeelSelector extends JComponent implements IComponent, ActionListener {
    
    private final DcComboBox<?> cbFieldHeight = ComponentFactory.getComboBox();
    private final DcComboBox<?> cbButtonHeight = ComponentFactory.getComboBox();
    private final DcComboBox<?> cbTreeNodeHeight = ComponentFactory.getComboBox();
    private final DcComboBox<?> cbTableRowHeight = ComponentFactory.getComboBox();
    private final DcIconSizeComboBox cbIconSize = ComponentFactory.getIconSizeCombo();
    private final DcUIScaleComboBox cbUIScale = ComponentFactory.getUIScaleCombo();
    private final JCheckBox checkNoLF = ComponentFactory.getCheckBox(DcResources.getText("lblNoLF"));
    private final JCheckBox checkSystemLF = ComponentFactory.getCheckBox(DcResources.getText("lblLaf"));
    private final JComboBox<Object> comboSystemLF = ComponentFactory.getComboBox();
    
    private DcDialog parent;
    
    private boolean applyModus = true;
    private boolean restartRequired = false;
   
    public DcLookAndFeelSelector() {
        applyModus = false;
        buildComponent();
        fillSystemLFCombo();
        applyModus = true;
    }
    
    public void setParent(DcDialog dialog) {
        this.parent = dialog;
    }
    
    @Override
    public void reset() {
    	cbFieldHeight.setSelectedIndex(0);
    	cbButtonHeight.setSelectedIndex(0);
    	cbTreeNodeHeight.setSelectedIndex(0);
    	cbTableRowHeight.setSelectedIndex(0);
    	cbIconSize.setSelectedIndex(0);
    	cbUIScale.setSelectedIndex(0);
    	checkNoLF.setSelected(false);
    	checkSystemLF.setSelected(false);
    	comboSystemLF.setSelectedIndex(0);
    }    
    
    @Override
    public void clear() {
    	cbFieldHeight.clear();
    	cbButtonHeight.clear();
    	cbTreeNodeHeight.clear();
    	cbTableRowHeight.clear();
    	cbIconSize.clear();
    	cbUIScale.clear();
    	
        parent = null;
    }
    
    public boolean isRestartRequired() {
    	return restartRequired;
    }
    
    private void fillSystemLFCombo() {
        LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
        LookAndFeelInfo laf;
        for (int i = 0; i < lafs.length; i++) {
            laf = lafs[i];
            if (	laf.getName().toLowerCase().indexOf("cde/motif") == -1 &&
            		laf.getName().toLowerCase().indexOf("nimbus") == -1 &&
            		laf.getName().toLowerCase().indexOf("windows classic") == -1)
                comboSystemLF.addItem(new DcLookAndFeel(laf.getName(), laf.getClassName(), null, DcLookAndFeel._LAF));
        }
    }
    
    @Override
    public Object getValue() {
        return getLookAndFeel();
    }
    
    public DcLookAndFeel getLookAndFeel() {
        return null;
    }
    
    /**
     * Applies a value to this field
     */
    @Override
    public void setValue(Object o) {
        applyModus = false;
        if (o instanceof DcLookAndFeel) { 
            DcLookAndFeel laf = (DcLookAndFeel) o;
            if (laf.getType() == DcLookAndFeel._NONE) {
                checkNoLF.setSelected(true);
            } else { 
                comboSystemLF.setSelectedItem(laf);
                checkSystemLF.setSelected(true);
            }
        }
        
        cbFieldHeight.setSelectedItem(Long.valueOf(DcSettings.getInt(DcRepository.Settings.stInputFieldHeight)));
        cbButtonHeight.setSelectedItem(Long.valueOf(DcSettings.getInt(DcRepository.Settings.stButtonHeight)));
        cbTreeNodeHeight.setSelectedItem(Long.valueOf(DcSettings.getInt(DcRepository.Settings.stTreeNodeHeight)));
        cbTableRowHeight.setSelectedItem(Long.valueOf(DcSettings.getInt(DcRepository.Settings.stTableRowHeight)));
        cbIconSize.setValue(DcSettings.getLong(DcRepository.Settings.stIconSize));
        cbUIScale.setValue(DcConfig.getInstance().getClientSettings().getUIScaling());
        
        applyModus = true;
    }
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        
        if (checkNoLF != null) {
            checkNoLF.setFont(ComponentFactory.getStandardFont());
            checkSystemLF.setFont(ComponentFactory.getStandardFont());
            comboSystemLF.setFont(ComponentFactory.getStandardFont());
        }
    }     
    
    @Override
    public void setEditable(boolean b) {
        checkNoLF.setEnabled(b);
        checkSystemLF.setEnabled(b);
        comboSystemLF.setEnabled(b);
        cbButtonHeight.setEnabled(b);
        cbFieldHeight.setEnabled(b);
        cbTreeNodeHeight.setEnabled(b);
        cbTableRowHeight.setEnabled(b);
        cbIconSize.setEnabled(b);
        cbUIScale.setEnabled(b);
    }
    
    /**
     * Builds this component
     */
    private void buildComponent() {
        setLayout(Layout.getGBL());
        
        for (int i = 20; i < 41; i++) {
            cbFieldHeight.addItem(Long.valueOf(i));
            cbButtonHeight.addItem(Long.valueOf(i));
            cbTableRowHeight.addItem(Long.valueOf(i));
            cbTreeNodeHeight.addItem(Long.valueOf(i));
        }
        
        cbFieldHeight.addActionListener(this);
        cbFieldHeight.setActionCommand("applyInputFieldHeight");
        cbFieldHeight.setToolTipText(DcResources.getText("tpInputFieldHeight"));

        cbButtonHeight.addActionListener(this);
        cbButtonHeight.setActionCommand("applyButtonHeight");
        cbButtonHeight.setToolTipText(DcResources.getText("tpButtonHeight"));
        
        cbTableRowHeight.addActionListener(this);
        cbTableRowHeight.setActionCommand("applyTableRowHeight");
        cbTableRowHeight.setToolTipText(DcResources.getText("tpTableRowHeight"));

        cbTreeNodeHeight.addActionListener(this);
        cbTreeNodeHeight.setActionCommand("applyTreeNodeHeight");
        cbTreeNodeHeight.setToolTipText(DcResources.getText("tpTreeNodeHeight"));
        
        cbIconSize.addActionListener(this);
        cbIconSize.setActionCommand("applyIconSize");
        
        cbUIScale.addActionListener(this);
        cbUIScale.setActionCommand("applyScale");
        
        DcLabel lblIconSize = ComponentFactory.getLabel(DcResources.getText("lblIconSize"));
        DcLabel lblUIScaling = ComponentFactory.getLabel(DcResources.getText("lblUIScaling"));
        DcLabel lblTableRowHeight = ComponentFactory.getLabel(DcResources.getText("lblTableRowHeight"));
        DcLabel lblTreeNodeHeight = ComponentFactory.getLabel(DcResources.getText("lblTreeNodeHeight"));
        DcLabel lblFieldHeight = ComponentFactory.getLabel(DcResources.getText("lblInputFieldHeight"));
        lblFieldHeight.setToolTipText(DcResources.getText("tpInputFieldHeight"));
        DcLabel lblButtonHeight = ComponentFactory.getLabel(DcResources.getText("lblButtonHeight"));
        lblButtonHeight.setToolTipText(DcResources.getText("tpButtonHeight"));
        
        checkNoLF.addActionListener(new TypeActionListener(DcLookAndFeel._NONE));
        checkSystemLF.addActionListener(new TypeActionListener(DcLookAndFeel._LAF));
        
        comboSystemLF.addActionListener(this);
        
        add(checkNoLF,       Layout.getGBC( 0, 0, 2, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(checkSystemLF,   Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(comboSystemLF,   Layout.getGBC( 1, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(lblFieldHeight,  Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(cbFieldHeight,   Layout.getGBC( 1, 3, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(lblButtonHeight, Layout.getGBC( 0, 4, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(cbButtonHeight,  Layout.getGBC( 1, 4, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(lblTableRowHeight,Layout.getGBC( 0, 5, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(cbTableRowHeight, Layout.getGBC( 1, 5, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));      
        add(lblTreeNodeHeight,Layout.getGBC( 0, 6, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(cbTreeNodeHeight, Layout.getGBC( 1, 6, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(lblIconSize,Layout.getGBC( 0, 7, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(cbIconSize, Layout.getGBC( 1, 7, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(lblUIScaling, Layout.getGBC( 0, 8, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
        add(cbUIScale, Layout.getGBC( 1, 8, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));        
    }
    
    public static class FontStyle {
        final int style;
        final String name;
        
        public FontStyle(int index, String name) {
            this.style = index;
            this.name = name;
        }
        
        public int getIndex() {
            return style;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    private void applyLAF() {
        if (applyModus) {
            DcLookAndFeel laf = null;
            
            if (checkNoLF.isSelected()) 
                laf = DcLookAndFeel.getDisabled();
            else if (checkSystemLF.isSelected())
                laf = (DcLookAndFeel) comboSystemLF.getSelectedItem();
            
            if (laf != null) {
                GUI.getInstance().applyLAF();
                GUI.getInstance().getMainFrame().updateLAF(laf);
                if (parent != null) {
                    SwingUtilities.updateComponentTreeUI(parent.getContentPane());
                    parent.repaint();
                    parent.validate();
                    parent.close();
                }
            }
        }
    }
    
    private class TypeActionListener implements ActionListener {
        
        private int type = DcLookAndFeel._NONE;
        
        public TypeActionListener(int type) {
            this.type = type;
        }
        
        @Override
        public void actionPerformed(ActionEvent arg0) {
            checkNoLF.setSelected(false);
            checkSystemLF.setSelected(false);
            comboSystemLF.setEnabled(false);
            
            if (type == DcLookAndFeel._NONE) {
                checkNoLF.setSelected(true);
            } else if (type == DcLookAndFeel._LAF) {
                checkSystemLF.setSelected(true);
                comboSystemLF.setEnabled(true);
            }
            
            applyLAF();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("applyInputFieldHeight")) {
            Long value = (Long) cbFieldHeight.getSelectedItem();
            if (value != null) DcSettings.set(DcRepository.Settings.stInputFieldHeight, value);
        } else if (ae.getActionCommand().equals("applyButtonHeight")) {
            Long value = (Long) cbButtonHeight.getSelectedItem();
            if (value != null) DcSettings.set(DcRepository.Settings.stButtonHeight, value);
        } else if (ae.getActionCommand().equals("applyTreeNodeHeight")) {
            Long value = (Long) cbTreeNodeHeight.getSelectedItem();
            if (value != null) DcSettings.set(DcRepository.Settings.stTreeNodeHeight, value);
        } else if (ae.getActionCommand().equals("applyTableRowHeight")) {
            Long value = (Long) cbTableRowHeight.getSelectedItem();
            if (value != null) DcSettings.set(DcRepository.Settings.stTableRowHeight, value);
        } else if (ae.getActionCommand().equals("applyScale")) {            
            Long value = (Long) cbUIScale.getValue();
            if (value != null && !value.equals(DcConfig.getInstance().getClientSettings().getUIScaling())) {
            	DcConfig dcc = DcConfig.getInstance();
	            dcc.getClientSettings().setUiScaling(value);
	            dcc.getClientSettings().save();
            	
            	if (applyModus) restartRequired = true;
            }
        } else if (ae.getActionCommand().equals("applyIconSize")) {
            Long value = (Long) cbIconSize.getValue();
            if (value != null && !value.equals(DcSettings.getLong(DcRepository.Settings.stIconSize))) {
            	DcSettings.set(DcRepository.Settings.stIconSize, value);
            	if (value.intValue() > ((Long) cbTreeNodeHeight.getSelectedItem()).intValue())
            		cbTreeNodeHeight.setSelectedItem(value);
            	if (value.intValue() > ((Long) cbFieldHeight.getSelectedItem()).intValue())
            		cbFieldHeight.setSelectedItem(value);
            	if (value.intValue() > ((Long) cbTableRowHeight.getSelectedItem()).intValue())
            		cbTableRowHeight.setSelectedItem(value);
            	if (value.intValue() > ((Long) cbButtonHeight.getSelectedItem()).intValue())
            		cbButtonHeight.setSelectedItem(value);
            	
            	applyLAF();
            }
        } else {
            applyLAF();
        }
    }
    
    @Override
    public void refresh() {}
}