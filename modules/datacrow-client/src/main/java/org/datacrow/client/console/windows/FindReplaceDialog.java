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

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcComboBox;
import org.datacrow.client.console.components.DcEditableComboBox;
import org.datacrow.client.console.components.DcReferenceField;
import org.datacrow.client.console.components.renderers.ComboBoxRenderer;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.console.IView;
import org.datacrow.core.data.DataFilter;
import org.datacrow.core.data.DataFilterEntry;
import org.datacrow.core.data.Operator;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcProperty;
import org.datacrow.core.objects.DcSimpleValue;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.CoreUtilities;


public class FindReplaceDialog extends DcFrame implements ActionListener {

	private JButton buttonPreview;
    private JButton buttonClose;

    private DcModule module;
    private IView view;

    private final DcEditableComboBox cbOld = new DcEditableComboBox();
    private final DcEditableComboBox cbNew = new DcEditableComboBox();
    private final DcComboBox<Object> cbFields = ComponentFactory.getComboBox(new DefaultComboBoxModel<Object>());

    public FindReplaceDialog(IView view) {

        super(DcResources.getText("lblFindReplace"), IconLibrary._icoFindReplace);
        
        this.view = view;
        this.module = view.getModule();

        setHelpIndex("dc.tools.findreplace");

        buildDialog(module);

        setSize(DcSettings.getDimension(DcRepository.Settings.stFindReplaceDialogSize));
        setCenteredLocation();
        
        cbFields.setSelectedIndex(0);
    }
    
    private void applySelectedField(DcField field, DcComboBox<Object> cb) {
        JComponent c = ComponentFactory.getComponent(field.getModule(), field.getReferenceIdx(), field.getIndex(), field.getFieldType(), field.getLabel(), 255);
        
        if (c instanceof DcReferenceField) 
            c = ((DcReferenceField) c).getComboBox();
        
        cb.removeAllItems();
        
        Connector connector = DcConfig.getInstance().getConnector();
        if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
            if (connector.getCount(field.getReferenceIdx(), -1, null) > 1000) {
                for (DcSimpleValue value : connector.getSimpleValues(field.getReferenceIdx(), false))
                    cb.addItem(value);
            } else {
                int[] fields;
                if (DcModules.get(field.getReferenceIdx()).getType() == DcModule._TYPE_PROPERTY_MODULE)
                    fields = new int[] {DcObject._ID, DcProperty._A_NAME, DcProperty._B_ICON};
                else
                    fields = new int[] {DcObject._ID, DcModules.get(field.getReferenceIdx()).getDisplayFieldIdx()};
                
                List<DcObject> objects = connector.getItems(field.getReferenceIdx(), fields);
                for (Object o : objects)
                    cb.addItem(o);
                
                cb.setRenderer(ComboBoxRenderer.getInstance());
                cb.setEditable(false);
            }
            
        } else if (c instanceof JComboBox) {
            @SuppressWarnings("unchecked")
			JComboBox<Object> combo = (JComboBox<Object>) c;
            for (int i = 0; i < combo.getItemCount(); i++)
                cb.addItem(combo.getItemAt(i));
            
            cb.setRenderer(combo.getRenderer());
            cb.setEditable(false);
        } else {
            cb.setEditable(true);
        }

        cb.setEnabled(true);
    } 
    
    private void replace() {
        
        if (CoreUtilities.isEmpty(cbOld.getValue()) || CoreUtilities.isEmpty(cbNew.getValue())) {
            GUI.getInstance().displayWarningMessage(DcResources.getText("msgEnterValueForFind"));
            return;
        } else if (cbOld.getValue().equals(cbNew.getValue())) {
            GUI.getInstance().displayWarningMessage(DcResources.getText("msgFindReplaceValuesEqual"));
            return;
        }
        
        final DcField field = (DcField) cbFields.getSelectedItem();
        DataFilter df = new DataFilter(module.getIndex());
        
        if (field.getValueType() == DcRepository.ValueTypes._STRING)
        	df.addEntry(new DataFilterEntry(module.getIndex(), field.getIndex(), Operator.REGEX, cbOld.getValue()));
        else
        	df.addEntry(new DataFilterEntry(module.getIndex(), field.getIndex(), Operator.CONTAINS, cbOld.getValue()));

        Collection<Integer> include = new ArrayList<Integer>();
        include.add(field.getIndex());

        Connector connector = DcConfig.getInstance().getConnector();
        FindReplaceTaskDialog dlg = new FindReplaceTaskDialog(
                this, 
                view,
                connector.getItems(df, module.getMinimalFields(include)), 
                new int[] {DcObject._SYS_DISPLAYVALUE, field.getIndex()}, 
                field.getIndex(),
                cbOld.getValue(), 
                cbNew.getValue());
        dlg.setVisible(true);
    }

    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stFindReplaceDialogSize, getSize());
        super.close();
    }

    private void buildDialog(DcModule module) {

        for (DcField field : module.getFields()) {
            if (    field.isSearchable() && 
                    field.isEnabled() && 
                   !field.isReadOnly() &&
                    field.getIndex() != DcObject._SYS_EXTERNAL_REFERENCES &&
                   (field.getValueType() == DcRepository.ValueTypes._STRING ||
                    field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                    field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE))
                cbFields.addItem(field);
        }

        cbFields.addActionListener(this);
        cbFields.setActionCommand("fieldSelected");
        
        cbOld.setRenderer(new DefaultListCellRenderer());
        cbNew.setRenderer(new DefaultListCellRenderer());

        
        //**********************************************************
        //Input panel
        //**********************************************************
        JPanel panelInput = new JPanel();
        panelInput.setLayout(Layout.getGBL());
        
        for (DcField field : module.getFields()) {
            if (    field.isEnabled() && 
                   !field.isReadOnly() &&
                    field.isSearchable())
                cbFields.addItem(field);
        }

        panelInput.add(ComponentFactory.getLabel(DcResources.getText("lblField")), Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
        panelInput.add(cbFields, Layout.getGBC(1, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
        panelInput.add(ComponentFactory.getLabel(DcResources.getText("lblFind")), Layout.getGBC(0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
        panelInput.add(cbOld, Layout.getGBC(1, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
        panelInput.add(ComponentFactory.getLabel(DcResources.getText("lblReplacement")), Layout.getGBC(0, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
        panelInput.add(cbNew, Layout.getGBC(1, 2, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(0, 0, 0, 0), 0, 0));
        
        //**********************************************************
        //Action panel
        //**********************************************************
        JPanel panelActions = new JPanel();

        buttonPreview = ComponentFactory.getButton(DcResources.getText("lblPreview"));
        buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));

        buttonPreview.addActionListener(this);
        buttonPreview.setActionCommand("replace");
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");

        panelActions.add(buttonPreview);
        panelActions.add(buttonClose);
        
        //**********************************************************
        //Main panel
        //**********************************************************
        this.getContentPane().setLayout(Layout.getGBL());
        this.getContentPane().add(panelInput  ,Layout.getGBC(0, 0, 1, 1, 10.0, 10.0
                                              ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                                               new Insets(5, 5, 5, 5), 0, 0));
        this.getContentPane().add(panelActions,Layout.getGBC(0, 2, 1, 1, 1.0, 1.0
                                              ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));
        
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close")) {
            close();
        } else if (ae.getActionCommand().equals("replace")) {
            replace();
        } else if (ae.getActionCommand().equals("fieldSelected")) {
            applySelectedField((DcField) cbFields.getSelectedItem(), cbNew);
            applySelectedField((DcField) cbFields.getSelectedItem(), cbOld);
        }
    }  
}
