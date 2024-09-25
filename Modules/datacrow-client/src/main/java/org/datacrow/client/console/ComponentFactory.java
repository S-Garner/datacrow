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

package org.datacrow.client.console;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.ItemSelectable;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerListener;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyListener;
import java.awt.event.InputMethodListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;

import org.datacrow.client.console.components.AwsKeyRequestDialog;
import org.datacrow.client.console.components.DcButton;
import org.datacrow.client.console.components.DcCheckBox;
import org.datacrow.client.console.components.DcCollationComboBox;
import org.datacrow.client.console.components.DcColorSelector;
import org.datacrow.client.console.components.DcComboBox;
import org.datacrow.client.console.components.DcDateField;
import org.datacrow.client.console.components.DcDateFormatComboBox;
import org.datacrow.client.console.components.DcDecimalField;
import org.datacrow.client.console.components.DcDirectoriesAsDrivesField;
import org.datacrow.client.console.components.DcDriveMappingField;
import org.datacrow.client.console.components.DcFileField;
import org.datacrow.client.console.components.DcFileLauncherField;
import org.datacrow.client.console.components.DcFilePatternField;
import org.datacrow.client.console.components.DcFilePatternTextField;
import org.datacrow.client.console.components.DcFileSizeField;
import org.datacrow.client.console.components.DcFontRenderingComboBox;
import org.datacrow.client.console.components.DcFontSelector;
import org.datacrow.client.console.components.DcHtmlEditorPane;
import org.datacrow.client.console.components.DcIconSelectField;
import org.datacrow.client.console.components.DcIconSizeComboBox;
import org.datacrow.client.console.components.DcImageLabel;
import org.datacrow.client.console.components.DcLabel;
import org.datacrow.client.console.components.DcLoginNameField;
import org.datacrow.client.console.components.DcLongTextField;
import org.datacrow.client.console.components.DcLookAndFeelSelector;
import org.datacrow.client.console.components.DcMenu;
import org.datacrow.client.console.components.DcMenuBar;
import org.datacrow.client.console.components.DcMenuItem;
import org.datacrow.client.console.components.DcModuleSelector;
import org.datacrow.client.console.components.DcNumberField;
import org.datacrow.client.console.components.DcObjectComboBox;
import org.datacrow.client.console.components.DcPanel;
import org.datacrow.client.console.components.DcPasswordField;
import org.datacrow.client.console.components.DcPictureField;
import org.datacrow.client.console.components.DcProgramDefinitionsField;
import org.datacrow.client.console.components.DcRadioButton;
import org.datacrow.client.console.components.DcRatingComboBox;
import org.datacrow.client.console.components.DcReferenceField;
import org.datacrow.client.console.components.DcReferencesField;
import org.datacrow.client.console.components.DcResolutionComboBox;
import org.datacrow.client.console.components.DcShortTextField;
import org.datacrow.client.console.components.DcTabbedPane;
import org.datacrow.client.console.components.DcTagField;
import org.datacrow.client.console.components.DcTextPane;
import org.datacrow.client.console.components.DcTimeField;
import org.datacrow.client.console.components.DcTitledBorder;
import org.datacrow.client.console.components.DcToolBarButton;
import org.datacrow.client.console.components.DcTree;
import org.datacrow.client.console.components.DcUIScaleComboBox;
import org.datacrow.client.console.components.DcUrlField;
import org.datacrow.client.console.components.IComponent;
import org.datacrow.client.console.components.renderers.AvailabilityComboBoxRenderer;
import org.datacrow.client.console.components.renderers.ComboBoxRenderer;
import org.datacrow.client.console.components.tables.DcTable;
import org.datacrow.client.util.Utilities;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.console.UIComponents;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.plugin.Plugin;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.settings.objects.DcColor;
import org.datacrow.core.settings.objects.DcFont;
import org.datacrow.core.settings.objects.DcLookAndFeel;

import net.jacksum.JacksumAPI;

/**
 * Used to create each and every component for the Data Crow GUI.
 * 
 * @author Robert Jan van der Waals
 */
public final class ComponentFactory extends UIComponents {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ComponentFactory.class.getName());
    
    private static final FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
    
    private static final Font fontUnreadable = new Font("Dialog", Font.PLAIN, 1);
    private static final Color colorDisabled = new Color(240,240,240);
    private static final Color colorRequired = new Color(120, 0, 0);

    public static final Cursor _CURSOR_NORMAL = new Cursor(Cursor.DEFAULT_CURSOR);
    public static final Cursor _CURSOR_WAIT = new Cursor(Cursor.WAIT_CURSOR);    
    
    private static LookAndFeel defaultLaf;

    /**
     * Cleans the component. This method tries to dynamically clean any component
     * of its children, listeners and calls specific cleaner methods on custom components.
     * This will ensure the component to get GC-ed. After this call the component can no
     * longer be used.
     * 
     * @param component the component to clean
     */
    public static final void clean(final Component component) {
        
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(
                    new Thread(new Runnable() { 
                        @Override
                        public void run() {
                            clean(component);
                        }
                    }));
        }

        if (component instanceof JComponent) {
            JComponent c = (JComponent) component;
            
            // remove all listeners
            removeListeners(c);
            
            if (c instanceof org.datacrow.client.console.components.lists.DcList) 
                ((org.datacrow.client.console.components.lists.DcList) c).clear();
            
            if (c instanceof DcTable) 
                ((DcTable) c).clear();
            
            if (c instanceof DcPanel) 
                ((DcPanel) c).clear();
            
            if (c instanceof IComponent)
                ((IComponent) c).clear();
            
            if (c instanceof JMenu) {
                for (int i = 0; i < ((JMenu) c).getItemCount(); i++)
                    clean(((JMenu) c).getItem(i));
            }
            
            for (int i = 0; i < c.getComponentCount(); i++) {
                try {
                    clean(c.getComponent(i));
                } catch (Exception e) {}
            }

            c.setComponentPopupMenu(null);
            c.removeAll();
            c.invalidate();
        }
    }    
    
    public static int getPreferredFieldHeight() {
        return DcSettings.getSettings() != null ? DcSettings.getInt(DcRepository.Settings.stInputFieldHeight) : 20;
    }
    
    public static int getPreferredButtonHeight() {
        return DcSettings.getSettings() != null ? DcSettings.getInt(DcRepository.Settings.stButtonHeight) : 20;
    }
    
    public static void setLookAndFeel() {
        try {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
            
            if (defaultLaf == null)
                defaultLaf = UIManager.getLookAndFeel();
            
            if (DcSettings.getSettings() != null) {
                DcLookAndFeel laf = DcSettings.getLookAndFeel(DcRepository.Settings.stLookAndFeel);
                if (laf.getType() == DcLookAndFeel._LAF) {
                    UIManager.setLookAndFeel(laf.getClassName());
                } else {
                    UIManager.setLookAndFeel(defaultLaf);
                }
            }
            
            UIManager.getLookAndFeelDefaults().put("SplitPane.border", null);
            UIManager.put("TabbedPane.lightHighlight", UIManager.get("TabbedPane.background") );
            UIManager.put("TabbedPane.darkShadow", UIManager.get("TabbedPane.background") );
            UIManager.put("TabbedPane.shadow", UIManager.get("TabbedPane.background") );
            UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(Color.black, 1));
            
            UIManager.put("Tree.leafIcon", IconLibrary._icoTreeLeaf);
            UIManager.put("Tree.openIcon", IconLibrary._icoTreeOpen);
            UIManager.put("Tree.closedIcon", IconLibrary._icoTreeClosed);
        } catch (Exception e) {
            logger.error("Error while applying default UI properties", e);
        }        
    }
    
    private static final void removeListeners(JComponent c) {
        
        removeListeners(ActionListener.class, c);
        removeListeners(AncestorListener.class, c);

        removeListeners(ComponentListener.class, c);
        removeListeners(ContainerListener.class, c);

        removeListeners(HierarchyBoundsListener.class, c);
        removeListeners(HierarchyListener.class, c);

        removeListeners(InputMethodListener.class, c);
        removeListeners(ItemListener.class, c);
        
        removeListeners(KeyListener.class, c);
        
        removeListeners(MouseMotionListener.class, c);
        removeListeners(MouseWheelListener.class, c);
        removeListeners(MouseListener.class, c);
        
        removeListeners(PropertyChangeListener.class, c);
        removeListeners(VetoableChangeListener.class, c);
    }
    
    private static final void removeListeners(Class<? extends EventListener> clazz, JComponent c) {
         EventListener[] listeners = c.getListeners(clazz);
         for (int i = 0; i < listeners.length; i++) {
            
             if (listeners[i] instanceof java.awt.event.MouseListener) 
                 c.removeMouseListener((MouseListener) listeners[i]);
             
             if (listeners[i] instanceof java.awt.event.ActionListener) {
                 if (c instanceof JComboBox)
                     ((JComboBox<?>)c).removeActionListener((ActionListener) listeners[i]);    
                 if (c instanceof AbstractButton)
                     ((AbstractButton)c).removeActionListener((ActionListener) listeners[i]);
             }
             
             if (listeners[i] instanceof java.awt.event.ItemListener)
                 ((ItemSelectable) c).removeItemListener((ItemListener) listeners[i]);

             if (listeners[i] instanceof AncestorListener)
                 c.removeAncestorListener((AncestorListener) listeners[i]);

             if (listeners[i] instanceof ComponentListener)
                 c.removeComponentListener((ComponentListener) listeners[i]);

             if (listeners[i] instanceof ContainerListener)
                 c.removeContainerListener((ContainerListener) listeners[i]);

             if (listeners[i] instanceof FocusListener)
                 c.removeFocusListener((FocusListener) listeners[i]);

             if (listeners[i] instanceof HierarchyBoundsListener)
                 c.removeHierarchyBoundsListener((HierarchyBoundsListener) listeners[i]);

             if (listeners[i] instanceof HierarchyListener)
                 c.removeHierarchyListener((HierarchyListener) listeners[i]);

             if (listeners[i] instanceof InputMethodListener)
                 c.removeInputMethodListener((InputMethodListener) listeners[i]);

             if (listeners[i] instanceof KeyListener)
                 c.removeKeyListener((KeyListener) listeners[i]);

             if (listeners[i] instanceof MouseMotionListener)
                 c.removeMouseMotionListener((MouseMotionListener) listeners[i]);

             if (listeners[i] instanceof MouseWheelListener)
                 c.removeMouseWheelListener((MouseWheelListener) listeners[i]);

             if (listeners[i] instanceof PropertyChangeListener)
                 c.removePropertyChangeListener((PropertyChangeListener) listeners[i]);

             if (listeners[i] instanceof VetoableChangeListener)
                 c.removeVetoableChangeListener((VetoableChangeListener) listeners[i]);
         }
    }
    
    public static final JComponent getComponent(int majormodule, 
                                                int minormodule,
                                                int fieldIdx,
                                                int fieldType, 
                                                String label, 
                                                int maxTextLength) {
        switch (fieldType) {
            case _YESNOCOMBO:
                return getYesNoCombo();
            case _LONGTEXTFIELD:
                return getLongTextField();
            case _CHECKBOX:
                return getCheckBox(label);
            case _NUMBERFIELD:
                return getNumberField();
            case _CHARACTERFIELD:
                return getShortTextField(1);
            case _DECIMALFIELD:
                return getDecimalField();
            case _URLFIELD:
                return getURLField(maxTextLength);
            case _SIMPLEPICTUREFIELD:
                return getPictureField(true, false);
            case _PICTUREFIELD:
                return getPictureField(true, true);
            case _FONTSELECTOR:
                return getFontSelector();
            case _TIMEFIELD:
                return getTimeField();
            case _AVAILABILITYCOMBO:
                return getAvailabilityCombo();
            case _RATINGCOMBOBOX:
                return getRatingComboBox();
            case _FILEFIELD:
                return getFileField(false, false);
            case _DIRECTORYFIELD:
                return getFileField(false, true);
            case _PASSWORDFIELD:
                return getPasswordField();
            case _REFERENCEFIELD:
                return getReferenceField(minormodule);
            case _PROGRAMDEFINITIONFIELD:
                return getProgramDefinitionField();
            case _LOOKANDFEELSELECTOR:
                return getLookAndFeelSelector();
            case _MODULESELECTOR:
                return getModuleSelector();
            case _FILELAUNCHFIELD:
                return getFileLaunchField();
            case _REFERENCESFIELD:
                return getReferencesField(DcModules.getMappingModIdx(majormodule, minormodule, fieldIdx));
            case _DATEFIELD:
                return getDateField();          
            case _FILESIZEFIELD:
                return getFileSizeField();
            case _LOGINNAMEFIELD:
                return getLoginNameField();
            case _HASHTYPECOMBO:
                return getHashTypeComboBox();
            case _PERSONORDERCOMBO:
                return getPersonOrderComboBox();
            case _PERSONDISPLAYFORMATCOMBO:
                return getPersonDisplayFormatComboBox();
            case _LANGUAGECOMBO:
                return getLanguageCombobox();
            case _DRIVEMAPPING:
                return getDriveMappingField();
            case _CHARACTERSETCOMBO:
                return getCharacterSetCombobox();
            case _DIRECTORIESASDRIVES:
                return getDirectoriesAsDrivesField();
            case _FONTRENDERINGCOMBO:
                return getFontRenderingCombo();
            case _TAGFIELD:
                return getTagField(DcModules.getMappingModIdx(majormodule, minormodule, fieldIdx));
            case _SIMPLEREFERENCESFIELD:
                return getSimpleReferencesField(DcModules.getMappingModIdx(majormodule, minormodule, fieldIdx));
            case _DATEFOMATCOMBO:
                return getDateFormatCombo();
            case _ICONSIZECOMBO:
                return getIconSizeCombo();
            case _COLLATIONCOMBO:
            	return getCollationCombo();
            case _RESOLUTIONCOMBO:
            	return getResolutionCombo();
            case _ICONFIELD:
            	return getIconField();
            default:
                return getShortTextField(maxTextLength);
        }
    }
    
    public static DcTagField getTagField(int mappingModIx) {
        return new DcTagField(mappingModIx);
    }
    
    public static DcHtmlEditorPane getHtmlEditorPane() {
        return new DcHtmlEditorPane();
    }
    
    public static DcLongTextField getHelpTextField() {
        DcLongTextField textHelp = ComponentFactory.getLongTextField();
        textHelp.setBorder(null);
        textHelp.setEditable(false);
        textHelp.setMargin(new Insets(5,5,5,5));

        return textHelp;
    }
    
    public static final AwsKeyRequestDialog getAwsKeyRequestField() {
        return new AwsKeyRequestDialog();
    }
    
    public static final DcColorSelector getColorSelector(String settingsKey) {
        return new DcColorSelector(settingsKey);
    }

	public static final void setUneditable(JComponent component) {
        if (component instanceof IComponent)
            ((IComponent) component).setEditable(false);
	}

	public static final DcProgramDefinitionsField getProgramDefinitionField() {
		return new DcProgramDefinitionsField();
	}

    public static final DcFileLauncherField getFileLaunchField() {
        return new DcFileLauncherField();
    }
    
    public static final DcModuleSelector getModuleSelector() {
        return new DcModuleSelector();
    }

    public static final DcReferencesField getReferencesField(int mappingModIdx) {
        return new DcReferencesField(mappingModIdx);
    }
    
    public static final DcReferencesField getSimpleReferencesField(int mappingModIdx) {
        DcReferencesField referencesField = getReferencesField(mappingModIdx);
        referencesField.setEditable(false);
        return referencesField;
    }    
    
    public static final DcPasswordField getPasswordField() {
        DcPasswordField passwordField = new DcPasswordField();
        passwordField.setPreferredSize(new Dimension(100, getPreferredFieldHeight()));
        passwordField.setMinimumSize(new Dimension(50, getPreferredFieldHeight()));
        return passwordField;
    }

    public static final DcFontSelector getFontSelector() {
        return new DcFontSelector();
    }

    public static final DcLookAndFeelSelector getLookAndFeelSelector() {
        return new DcLookAndFeelSelector();
    }    
    
    public static final DcTimeField getTimeField() {
    	return new DcTimeField();
    }
    
    public static final JComboBox<Object> getHashTypeComboBox() {
        JComboBox<Object> cb = getComboBox();
        for (String s : JacksumAPI.getAvailableAlgorithms().keySet()) {
            cb.addItem(s);
        }
        return cb;
    }

    public static final DcComboBox<String> getFontRenderingCombo() {
        return new DcFontRenderingComboBox();
    }
    
    public static final DcIconSizeComboBox getIconSizeCombo() {
        return new DcIconSizeComboBox();
    }
    
    public static final DcUIScaleComboBox getUIScaleCombo() {
        return new DcUIScaleComboBox();
    }    
    
    public static final DcComboBox<String> getCollationCombo() {
    	return new DcCollationComboBox();
    }
    
    public static final DcResolutionComboBox getResolutionCombo() {
    	return new DcResolutionComboBox();
    }
    
    public static final DcIconSelectField getIconField() {
    	return new DcIconSelectField(new Dimension(64, 64));
    }  

    public static final JComboBox<Object> getDateFormatCombo() {
        return new DcDateFormatComboBox();
    }
    
    public static final JComboBox<Object> getPersonOrderComboBox() {
        JComboBox<Object> cb = getComboBox();
        cb.addItem(DcResources.getText("lblPersonOrginalOrder"));
        cb.addItem(DcResources.getText("lblPersonOrderByLastname"));
        cb.addItem(DcResources.getText("lblPersonOrderByFirstname"));
        return cb;
    }
    
    public static final DcDriveMappingField getDriveMappingField() {
        return new DcDriveMappingField();
    }
    
    public static final DcDirectoriesAsDrivesField getDirectoriesAsDrivesField() {
        return new DcDirectoriesAsDrivesField();
    }
    
    public static final JComboBox<Object> getLanguageCombobox() {
        JComboBox<Object> cb = getComboBox();
        for (String language : DcResources.getLanguages())
            cb.addItem(language);
        
        cb.setSelectedIndex(0);
        return cb;
    }
    
    public static final JComboBox<Object> getCharacterSetCombobox() {
        JComboBox<Object> cb = getComboBox();
        for (String charSet : Utilities.getCharacterSets()) 
            cb.addItem(charSet);

        cb.setSelectedIndex(0);
        return cb;
    }

    public static final JComboBox<Object> getPersonDisplayFormatComboBox() {
        JComboBox<Object> cb = getComboBox();
        cb.addItem(DcResources.getText("lblPersonFirstnameLastName"));
        cb.addItem(DcResources.getText("lblPersonLastNameFirstname"));
        return cb;
    }
    
    public static final DcObjectComboBox getObjectCombo(int module) {
        DcObjectComboBox comboBox = new DcObjectComboBox(module);
        comboBox.setFont(getStandardFont());
        return comboBox;
    }
    
    public static final DcReferenceField getReferenceField(int module) {
        DcReferenceField ref = new DcReferenceField(module);
        return ref;
    }
    
    public static final DcComboBox<Boolean> getAvailabilityCombo() {
        DcComboBox<Boolean> cb = new DcComboBox<>();
        cb.setFont(getStandardFont());
        cb.setRenderer(AvailabilityComboBoxRenderer.getInstance());

        cb.addItem(Boolean.TRUE);
        cb.addItem(Boolean.FALSE);
        return cb;
    }
    
    public static final DcRadioButton getRadioButton(String label, ImageIcon icon, String command) {
        DcRadioButton radioButton = new DcRadioButton(label, icon, false);
        radioButton.setSelectedIcon(icon);
        radioButton.setActionCommand(command);
        radioButton.setFont(getSystemFont());
        return radioButton;
    }
    
    public static final DcRadioButton getRadioButton(String label, ImageIcon icon) {
        DcRadioButton radioButton = new DcRadioButton(label, icon, false);
        radioButton.setFont(getSystemFont());
        return radioButton;
    }

    public static final DcFileField getFileField(boolean save, boolean dirsOnly) {
        DcFileField ff = new DcFileField(null);
        ff.setModus(save, dirsOnly);
        return ff;
    }

    public static final DcFileField getFileField(boolean save, boolean dirsOnly, FileFilter filter) {
        DcFileField ff = new DcFileField(filter);
        ff.setModus(save, dirsOnly);
        return ff;
    }

    public static final DcFileSizeField getFileSizeField() {
        DcFileSizeField fld = new DcFileSizeField();
        fld.setFont(getStandardFont());
        return fld;
    }
    
    public static final DcLoginNameField getLoginNameField() {
        DcLoginNameField fld = new DcLoginNameField();
        fld.setFont(getStandardFont());
        fld.setMinimumSize(new Dimension(50, getPreferredFieldHeight()));
        fld.setPreferredSize(new Dimension(fld.getWidth(), ComponentFactory.getPreferredFieldHeight()));
        return fld;
    }
    
    public static final DcDateField getDateField() {
        DcDateField dateField = new DcDateField();
        dateField.setFont(getStandardFont());
        dateField.setPreferredSize(new Dimension(dateField.getWidth(), ComponentFactory.getPreferredFieldHeight()));
        dateField.setMinimumSize(new Dimension(50, getPreferredFieldHeight()));
        return dateField;
    }

    public static final DcPictureField getPictureField(boolean scaled, boolean allowActions) {
        DcPictureField pictureField = new DcPictureField(scaled, allowActions);
        return pictureField;
    }

    public static final DcUrlField getURLField(int maxLength) {
        DcUrlField urlField = new DcUrlField(maxLength);
        urlField.setFont(getStandardFont());
        return urlField;
    }

    public static final DcTree getTree(DefaultMutableTreeNode model) {
        DcTree tree = new DcTree(model);
        tree.setFont(getStandardFont());
        return tree;
    }

    public static final DcNumberField getNumberField() {
        DcNumberField numberField = new DcNumberField();
        numberField.setPreferredSize(new Dimension(numberField.getWidth(), getPreferredFieldHeight()));
        numberField.setMinimumSize(new Dimension(50, getPreferredFieldHeight()));
        numberField.setFont(getStandardFont());
        return numberField;
    }
    
    public static final DcDecimalField getDecimalField() {
        DcDecimalField decimalField = new DcDecimalField();
        decimalField.setPreferredSize(new Dimension(decimalField.getWidth(), getPreferredFieldHeight()));
        decimalField.setMinimumSize(new Dimension(50, getPreferredFieldHeight()));
        decimalField.setFont(getStandardFont());
        return decimalField;
    }    

    public static final DcRatingComboBox getRatingComboBox() {
        return new DcRatingComboBox();
    }

    public static final DcComboBox<String> getMP3GenreComboBox() {
        DcComboBox<String> cb = new DcComboBox<>();
        cb.setFont(getStandardFont());
        cb.setRenderer(ComboBoxRenderer.getInstance());
        
        cb.addItem("");
        cb.setSelectedIndex(0);
        cb.setFont(getStandardFont());

        for (String genre : DcRepository.Collections.colMusicGenres)
        	cb.addItem(genre);

        return cb;
    }

    public static final DcComboBox<Object> getComboBox(Object[] items) {
        DcComboBox<Object> comboBox = new DcComboBox<>(items);
        comboBox.setFont(getStandardFont());
        comboBox.setRenderer(ComboBoxRenderer.getInstance());
        return comboBox;
    }    

    public static final DcIconSelectField getIconSelectField(ImageIcon icon) {
        return new DcIconSelectField(icon);
    }
    
    public static final DcImageLabel getImageLabel(ImageIcon icon) {
        return new DcImageLabel(icon);
    }
    
    public static final DcComboBox<Object> getComboBox() {
        DcComboBox<Object> comboBox = new DcComboBox<>();
        comboBox.setFont(getStandardFont());
        comboBox.setRenderer(ComboBoxRenderer.getInstance());
        return comboBox;
    }

    public static final DcComboBox<Object> getComboBox(DefaultComboBoxModel<Object> model) {
        DcComboBox<Object> comboBox = new DcComboBox<>(model);
        comboBox.setFont(getStandardFont());
        comboBox.setRenderer(ComboBoxRenderer.getInstance());
        return comboBox;
    }
    
    public static final DcCheckBox getCheckBox(String labelText) {
        DcCheckBox checkBox = new DcCheckBox(labelText);
        checkBox.setFont(getSystemFont());
        return checkBox;
    }

    public static final DcMenuItem getMenuItem(String text) {
        DcMenuItem menuItem = new DcMenuItem(text);
        menuItem.setFont(getSystemFont());
        menuItem.setLayout(layout);
        return menuItem;
    }
    
    public static final DcMenuItem getMenuItem(AbstractAction action) {
        DcMenuItem menuItem = new DcMenuItem(action);
        menuItem.setFont(getSystemFont());
        menuItem.setLayout(layout);
        return menuItem;
    }    
    
    public static final DcMenuItem getMenuItem(ImageIcon icon, String text) {
        DcMenuItem menuItem = getMenuItem(text);
        menuItem.setIcon(icon);
        return menuItem;
    }

    public static final DcMenuItem getMenuItem(Plugin plugin) {
        DcMenuItem menuItem = new DcMenuItem(plugin);//getMenuItem(plugin.getLabel());
        menuItem.setFont(getSystemFont());
        menuItem.setLayout(layout);
        menuItem.setText(plugin.getLabel());
        menuItem.setIcon(plugin.getIcon());
        menuItem.setAccelerator(plugin.getKeyStroke());
        menuItem.setToolTipText(plugin.getHelpText() == null ? menuItem.getText() : plugin.getHelpText());
        return menuItem;
    }

    public static DcToolBarButton getToolBarButton(Plugin plugin) {
        DcToolBarButton button = new DcToolBarButton(plugin);        
        button.setFont(getSystemFont());
        button.setLayout(layout);
        return button;
    }
    
    public static final DcMenu getMenu(String text) {
        DcMenu menu = new DcMenu(text);
        return menu;
    }

    public static final DcMenu getMenu(ImageIcon icon, String text) {
        DcMenu menu = new DcMenu(text);
        menu.setFont(getSystemFont());
        menu.setIcon(icon);
        return menu;
    }

    public static final DcButton getTableHeader(String title) {
        DcButton button = getButton(title);
        button.setFont(ComponentFactory.getStandardFont());
        button.setPreferredSize(new Dimension(button.getWidth(), 20));
        return button;
    }

    public static final DcButton getIconButton(ImageIcon icon) {
        DcButton button = getButton("");
        button.setIcon(icon);
        button.setFont(getSystemFont());
        
    	int iconHeight = DcSettings.getInt(DcRepository.Settings.stIconSize);
        int minHeight = DcSettings.getSettings() != null ? DcSettings.getInt(DcRepository.Settings.stInputFieldHeight) : 20;
        
        int size = iconHeight > minHeight ? iconHeight : minHeight;

        Dimension dim = new Dimension(size, size);

        button.setMaximumSize(dim);
        button.setMinimumSize(dim);
        button.setPreferredSize(dim);
        
        button.setBorder(null);
        button.setBackground(new DcLabel().getBackground());
        
        return button;
    }
    
    public static final DcButton getButton(ImageIcon icon) {
        DcButton button = getButton("");
        button.setIcon(icon);
        button.setFont(getSystemFont());
        return button;
    }

    public static final DcButton getButton(String buttonText) {
    	return getButton(buttonText, null);
    }
    
    public static final DcButton getButton(String buttonText, ImageIcon icon) {
        DcButton button = icon == null ? new DcButton() : new DcButton(icon);
        ToolTipManager.sharedInstance().registerComponent(button);
        button.setName("bt" + buttonText);
        button.setText(buttonText);
        
        int height = getPreferredButtonHeight();
        
        if (buttonText != null) {
            if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblSave") : "Save"))
                button.setMnemonic('S');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblCancel") : "Cancel"))
                button.setMnemonic('C');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblClose") : "Close"))
                button.setMnemonic('C');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblRun") : "Run"))
                button.setMnemonic('R');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblOK") : "OK"))
                button.setMnemonic('O');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblApply") : "Apply"))
                button.setMnemonic('A');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblNew") : "New"))
                button.setMnemonic('N');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblNext") : "Next"))
                button.setMnemonic('N');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblDelete") : "Delete"))
                button.setMnemonic('D');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblAddNew") : "Add New"))
                button.setMnemonic('A');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblAdd") : "Add"))
                button.setMnemonic('A');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblClear") : "Clear"))
                button.setMnemonic('L');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblStop") : "Stop"))
                button.setMnemonic('T');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblStart") : "Start"))
                button.setMnemonic('S');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblYes") : "Yes"))
                button.setMnemonic('Y');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblNo") : "No"))
                button.setMnemonic('N');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblRemove") : "Remove"))
                button.setMnemonic('R');
            else if (buttonText.equals(DcResources.isInitialized() ? DcResources.getText("lblBack") : "Back"))
                button.setMnemonic('B');
        }
        
        button.setPreferredSize(new Dimension(120, height));
        button.setMaximumSize(new Dimension(120, height));
        button.setMinimumSize(new Dimension(120, height));
        button.setFont(getSystemFont());
        return button;
    }

    public static final DcLongTextField getTextArea() {
        DcLongTextField textArea = new DcLongTextField();
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(5,5,5,5));
        textArea.setFont(getStandardFont());
        return textArea;
    }
    
    public static final DcFilePatternField getFilePatternField(int module) {
        DcFilePatternField fpf = new DcFilePatternField(module);
        fpf.setWrapStyleWord(true);
        fpf.setLineWrap(true);
        fpf.setEditable(true);
        fpf.setMargin(new Insets(5,5,5,5));
        fpf.setFont(getStandardFont());
        return fpf;
    }    

    public static final DcTextPane getTextPane() {
        DcTextPane textpane = new DcTextPane();
        textpane.setFont(getStandardFont());
        return textpane;
    }

    public static final DcLongTextField getLongTextField() {
        DcLongTextField longText = new DcLongTextField();
        longText.setWrapStyleWord(true);
        longText.setLineWrap(true);
        longText.setEditable(true);
        longText.setFont(getStandardFont());
        return longText;
    }

    public static final DcLabel getLabel(ImageIcon icon) {
        DcLabel label = new DcLabel(icon);
        return label;
    }

    public static final DcLabel getLabel(String labelText, ImageIcon icon) {
        DcLabel label = getLabel(labelText);
        if (icon != null) label.setIcon(icon);
        return label;
    }

    public static final  JLabel getLabel(String labelText, int length) {
        JLabel label = getLabel(labelText);
        label.setPreferredSize(new Dimension(length, ComponentFactory.getPreferredFieldHeight()));
        label.setMinimumSize(new Dimension(20, getPreferredFieldHeight()));
        label.setText(labelText);
        return label;
    }

    public static final DcLabel getLabel(String labelText) {
        DcLabel label = new DcLabel();
        label.setText(labelText);
        label.setRequestFocusEnabled(false);
        label.setFont(getSystemFont());
        label.setToolTipText(labelText);

        return label;
    }
    
    public static final DcFilePatternTextField getFilePatternTextField() {
        DcFilePatternTextField fptf = new DcFilePatternTextField();
        fptf.setFont(getStandardFont());
        fptf.setPreferredSize(new Dimension(50, getPreferredFieldHeight()));
        fptf.setMinimumSize(new Dimension(50, getPreferredFieldHeight()));
        return fptf;
    }

    public static final DcShortTextField getShortTextField(int maxTextLength) {
        DcShortTextField textField = new DcShortTextField(maxTextLength);
        textField.setFont(getStandardFont());
        textField.setPreferredSize(new Dimension(50, getPreferredFieldHeight()));
        textField.setMinimumSize(new Dimension(50, getPreferredFieldHeight()));
        return textField;
    }
    
    public static final DcShortTextField getTextFieldDisabled() {
        DcShortTextField textField = new DcShortTextField(4000);
        textField.setPreferredSize(new Dimension(50, getPreferredFieldHeight()));
        textField.setMinimumSize(new Dimension(50, getPreferredFieldHeight()));
        textField.setEnabled(false);
        textField.setEditable(false);
        textField.setFont(ComponentFactory.getStandardFont());
        textField.setForeground(ComponentFactory.getDisabledColor());

        return textField;
    }
    
    public static final void setBorder(JComponent c) {
        //c.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    }
    
    public static final DcShortTextField getIdFieldDisabled() {
        DcShortTextField textField = new DcShortTextField(50);
        textField.setPreferredSize(new Dimension(50, getPreferredFieldHeight()));
        textField.setMinimumSize(new Dimension(50, getPreferredFieldHeight()));
        textField.setEnabled(false);
        textField.setEditable(false);
        textField.setFont(ComponentFactory.getStandardFont());
        textField.setForeground(ComponentFactory.getDisabledColor());
        return textField;
    }    

    public static final DcTabbedPane getTabbedPane() {
        DcTabbedPane tabbedPane = new DcTabbedPane();
        tabbedPane.setFont(getSystemFont());
        return tabbedPane;
    }

    public static final DcTable getDCTable(boolean readonly, boolean caching) {
        DcTable table = new DcTable(readonly, caching);
        return table;
    }

    public static final DcTable getDCTable(DcModule module, boolean readonly, boolean caching) {
    	DcTable table = new DcTable(module, readonly, caching);
        return table;
    }

    public static final JMenuBar getMenuBar() {
        DcMenuBar menuBar = new DcMenuBar();
        menuBar.setFont(getStandardFont());
        return menuBar;
    }

    public static final DcComboBox<String> getYesNoCombo() {
    	DcComboBox<String> comboBox = new DcComboBox<>();
        comboBox.setFont(getStandardFont());
        comboBox.setRenderer(ComboBoxRenderer.getInstance());
        
        comboBox.addItem(DcResources.getText("lblYes"));
        comboBox.addItem(DcResources.getText("lblNo"));
        comboBox.setFont(getStandardFont());

        return comboBox;
    }
    
    public static final TitledBorder getSelectionBorder() {
        DcColor color = DcSettings.getColor(DcRepository.Settings.stSelectionColor);
        Color c = new Color(color.getR(), color.getG(), color.getB());
        TitledBorder border = new DcTitledBorder(BorderFactory.createLineBorder(c, 1), "");
        border.setTitleFont(getSystemFont());
        return border;
    }

    public static final TitledBorder getTitleBorder(String title) {
        TitledBorder border = new DcTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1), title);
        border.setTitleFont(getSystemFont());
        return border;
    }

    public static final Color getCurrentForegroundColor() {
        Color color = UIManager.getLookAndFeelDefaults().getColor("TextField.foreground");
        return color == null ? Color.BLACK : color;
    }
    
    public static final Color getColor(String settingsKey) {
    	DcColor c = DcSettings.getColor(settingsKey);
    	return new Color(c.getR(), c.getG(), c.getB());
    }
    
    public static final Font getFont(String settingsKey) {
    	DcFont font = DcSettings.getFont(settingsKey);
    	Font f = new Font(font.getName(), font.getStyle(), font.getSize());
        return DcSettings.getSettings() != null ? f : Font.getFont(Font.SANS_SERIF);
    }
    
    public static final Font getStandardFont() {
    	return getFont(DcRepository.Settings.stStandardFont);
    }

    public static final Font getSystemFont() {
    	return getFont(DcRepository.Settings.stSystemFont);
    }

    public static final Font getUnreadableFont() {
        return fontUnreadable;
    }

    public static final Color getDisabledColor() {
       return colorDisabled;
    }

    public static final Color getRequiredColor() {
        return colorRequired;
    }

    public static final Color getTableHeaderColor() {
        return new Color(220, 220, 220);
    }

    public static final void setValue(JComponent c, Object o) {
        if (c instanceof IComponent)
            ((IComponent) c).setValue(o);
        else 
            logger.debug("Could not set value for " + c + " as its does not implement IComponent");
    }

    public static Object getValue(JComponent c) {
        if (c instanceof IComponent)
            return ((IComponent) c).getValue(); 
        else 
            logger.debug("Could not get value for " + c + " as its does not implement IComponent");
        
        return null;
    }
    
    public static <T extends JComponent> List<T> getChildComponents(
            Class<T> clazz, Container parent, boolean includeNested) {

        List<T> children = new ArrayList<T>();

        for (Component c : parent.getComponents()) {
            boolean isClazz = clazz.isAssignableFrom(c.getClass());
            if (isClazz) {
                children.add(clazz.cast(c));
            }
            if (includeNested && c instanceof JComponent) {
                children.addAll(getChildComponents(clazz, (Container) c,
                        includeNested));
            }
        }

        return children;
    }    
}
