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

package org.datacrow.client.console.wizards.itemexport;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcNumberField;
import org.datacrow.core.migration.itemexport.ItemExporterSettings;
import org.datacrow.core.resources.DcResources;

public class ItemExporterImageSettingsPanel extends JPanel {
    
	private final JCheckBox cbIncludeAttachments = ComponentFactory.getCheckBox(DcResources.getText("lblIncludeAttachments"));
	private final JCheckBox cbCopyAttachments = ComponentFactory.getCheckBox(DcResources.getText("lblCopyAttachments"));
	
	private final JCheckBox cbIncludeImages = ComponentFactory.getCheckBox(DcResources.getText("lblIncludeImage"));
    private final JCheckBox cbResizeImages = ComponentFactory.getCheckBox(DcResources.getText("lblScaleImages"));
    private final JCheckBox cbCopyImages = ComponentFactory.getCheckBox(DcResources.getText("lblCopyImage"));
    
    private final DcNumberField nfWidth = ComponentFactory.getNumberField();
    private final DcNumberField nfHeight = ComponentFactory.getNumberField();
    
    private final DcNumberField nfMaxTextLength = ComponentFactory.getNumberField();
    
    public ItemExporterImageSettingsPanel() {
        super();
        build();
    }
    
    private void applySelection() {
    	
    	cbCopyImages.setEnabled(cbIncludeImages.isSelected());
    	cbCopyImages.setSelected(!cbIncludeImages.isSelected() ? false : cbCopyImages.isSelected());
    	
        cbResizeImages.setEnabled(cbCopyImages.isSelected());
        cbResizeImages.setSelected(!cbCopyImages.isSelected() ? false : cbResizeImages.isSelected());
        
        nfWidth.setEnabled(cbResizeImages.isSelected());
        nfHeight.setEnabled(cbResizeImages.isSelected());
        
        cbCopyAttachments.setEnabled(cbIncludeAttachments.isSelected());
        cbCopyAttachments.setSelected(!cbIncludeAttachments.isSelected() ? false : cbCopyAttachments.isSelected());
    }
    
    public void saveSettings(ItemExporterSettings properties) {
    	
    	properties.set(ItemExporterSettings._INCLUDE_IMAGES, cbIncludeImages.isSelected());
        properties.set(ItemExporterSettings._COPY_IMAGES, cbCopyImages.isSelected());
        properties.set(ItemExporterSettings._SCALE_IMAGES, cbResizeImages.isSelected());
        properties.set(ItemExporterSettings._MAX_TEXT_LENGTH, nfMaxTextLength.getValue());
        properties.set(ItemExporterSettings._IMAGE_WIDTH, nfWidth.getValue());
        properties.set(ItemExporterSettings._IMAGE_HEIGHT,nfHeight.getValue());
        
        properties.set(ItemExporterSettings._INCLUDE_ATTACHMENTS, cbIncludeAttachments.isSelected());
        properties.set(ItemExporterSettings._COPY_ATTACHMENTS, cbCopyAttachments.isSelected());
    }
    
    public void applySettings(ItemExporterSettings properties) {
    	
    	cbIncludeAttachments.setSelected(properties.getBoolean(ItemExporterSettings._INCLUDE_ATTACHMENTS));
    	cbCopyAttachments.setSelected(properties.getBoolean(ItemExporterSettings._COPY_ATTACHMENTS));
    	
    	cbIncludeImages.setSelected(properties.getBoolean(ItemExporterSettings._INCLUDE_IMAGES));
        cbCopyImages.setSelected(properties.getBoolean(ItemExporterSettings._COPY_IMAGES));
        cbResizeImages.setSelected(properties.getBoolean(ItemExporterSettings._SCALE_IMAGES));
        nfMaxTextLength.setValue(properties.getInt(ItemExporterSettings._MAX_TEXT_LENGTH));
        
        nfWidth.setValue(properties.getInt(ItemExporterSettings._IMAGE_WIDTH));
        nfHeight.setValue(properties.getInt(ItemExporterSettings._IMAGE_HEIGHT));
        
        applySelection();
    }
    
    @Override
    public void setEnabled(boolean b) {
    	cbIncludeAttachments.setEnabled(b);
    	cbCopyAttachments.setEnabled(b);
    	cbIncludeImages.setEnabled(b);
        cbResizeImages.setEnabled(b);
        cbCopyImages.setEnabled(b);
        nfWidth.setEnabled(b);
        nfHeight.setEnabled(b);
        nfMaxTextLength.setEnabled(b);
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        
        PictureSettingListener rl = new PictureSettingListener();
        cbResizeImages.addActionListener(rl);
        cbCopyImages.addActionListener(rl);
        cbIncludeImages.addActionListener(rl);
        cbIncludeAttachments.addActionListener(rl);
        cbCopyAttachments.addActionListener(rl);

        Dimension size = new Dimension(100, ComponentFactory.getPreferredFieldHeight());
        nfHeight.setMinimumSize(size);
        nfHeight.setPreferredSize(size);
        nfWidth.setMinimumSize(size);
        nfWidth.setPreferredSize(size);
        
        JPanel panelImages = new JPanel();
        panelImages.setLayout(Layout.getGBL());

        panelImages.add(cbIncludeImages,  Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 5, 5, 5, 5), 0, 0));
        panelImages.add(cbCopyImages,  Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets( 5, 5, 5, 5), 0, 0));
        panelImages.add(cbResizeImages,  
                Layout.getGBC( 0, 2, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets( 5, 5, 5, 5), 0, 0));
        panelImages.add(nfWidth,        Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets( 5, 5, 5, 5), 0, 0));
        panelImages.add(ComponentFactory.getLabel(DcResources.getText("lblWidth")), 
                Layout.getGBC( 1, 3, 1, 1, 1.0, 1.0
               ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets( 5, 5, 5, 5), 0, 0));
        panelImages.add(nfHeight,       Layout.getGBC( 0, 4, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets( 5, 5, 5, 5), 0, 0));
        panelImages.add(ComponentFactory.getLabel(DcResources.getText("lblHeight")), 
                Layout.getGBC( 1, 4, 1, 1, 1.0, 1.0
               ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets( 5, 5, 5, 5), 0, 0));
        
        panelImages.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblImages")));

        JPanel panelAttachments = new JPanel();
        panelAttachments.setLayout(Layout.getGBL());
        
        panelAttachments.add(cbIncludeAttachments, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets( 5, 5, 5, 5), 0, 0));
        panelAttachments.add(cbCopyAttachments, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets( 5, 5, 5, 5), 0, 0));
        
        panelAttachments.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblAttachments")));
        
        nfMaxTextLength.setMinimumSize(new Dimension(40, ComponentFactory.getPreferredFieldHeight()));
        nfMaxTextLength.setPreferredSize(new Dimension(40, ComponentFactory.getPreferredFieldHeight()));
        
        JPanel panelText = new JPanel();
        panelText.setLayout(Layout.getGBL());
        panelText.add(ComponentFactory.getLabel(DcResources.getText("lblMaxTextLength")), 
                        Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                       ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        new Insets( 5, 5, 5, 5), 0, 0));
        panelText.add(nfMaxTextLength, 
                        Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                       ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        new Insets( 5, 5, 5, 5), 0, 0));
        
        panelText.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblText")));
        
		add(panelImages, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0, 
			GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 
			new Insets(5, 5, 5, 5), 0, 0));
		add(panelAttachments, Layout.getGBC(0, 1, 1, 1, 1.0, 1.0, 
			GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 
			new Insets(5, 5, 5, 5), 0, 0));
		add(panelText, Layout.getGBC(0, 2, 1, 1, 1.0, 1.0, 
			GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 0, 0));
    }
    
    private class PictureSettingListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            applySelection();
        }
    }
}
