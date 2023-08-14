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

package org.datacrow.client.console.components.panels;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcHtmlEditorPane;
import org.datacrow.client.console.menu.DcEditorPopupMenu;
import org.datacrow.client.plugins.PluginHelper;
import org.datacrow.client.util.Utilities;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcMediaObject;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.Picture;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.settings.objects.DcFont;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.StringUtils;
import org.datacrow.core.utilities.definitions.DcFieldDefinition;
import org.datacrow.core.utilities.definitions.QuickViewFieldDefinition;
import org.datacrow.core.utilities.definitions.QuickViewFieldDefinitions;

public class QuickViewPanel extends JPanel implements ChangeListener, MouseListener {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(QuickViewPanel.class.getName());
    
    public static final String _DIRECTION_HORIZONTAL = DcResources.getText("lblHorizontal");
    public static final String _DIRECTION_VERTICAL = DcResources.getText("lblVertical");  

    private final Map<Integer, ImagePanel> imagePanels = new LinkedHashMap<>();
    
    private final JTabbedPane tabbedPane = ComponentFactory.getTabbedPane();
    
    private final boolean showInlineImages;
    private final boolean showAttachments;

    private DcHtmlEditorPane descriptionPane;
    
	private DcObject dco;
    
    private String key;
    private int moduleIdx;
    
    private JScrollPane scroller;
    private boolean isAllowPopup = true;
    
    private final AttachmentsPanel attachmentsPanel = new AttachmentsPanel(false);
    
    private RelatedItemsPanel relatedItemsPanel = null;
    
    public QuickViewPanel(boolean showInlineImages, boolean showAttachments) {
        this.showInlineImages = showInlineImages;
        this.showAttachments = showAttachments;
        
        setLayout(Layout.getGBL());
        tabbedPane.addChangeListener(this);
        
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buildPanel();
    }    
    
    public void reloadImage() {
        if (tabbedPane.getSelectedIndex() > 0)
        	loadTab();
    }
    
    public void isAllowPopup(boolean b) {
    	isAllowPopup = b;
    }
    
    private void loadTab() {
    	Integer key = Integer.valueOf(tabbedPane.getSelectedIndex());
    	
    	if (imagePanels.containsKey(key)) {
    		ImagePanel panel = imagePanels.get(key);
    		panel.load();
    	} else {
    		if (tabbedPane.getSelectedComponent() instanceof AttachmentsPanel)
    			attachmentsPanel.load();
    	}
    }
    
    private void createImageTabs(DcObject dco) {
        try {
            reset();
    
            Picture picture;
            ImagePanel panel;

            for (DcFieldDefinition definition : dco.getModule().getFieldDefinitions().getDefinitions()) {
                if (dco.isEnabled(definition.getIndex()) && 
                    dco.getField(definition.getIndex()).getValueType() == DcRepository.ValueTypes._PICTURE) {
                    
                    picture = (Picture) dco.getValue(definition.getIndex());

                    if (picture == null) continue;
                    
                    if (picture.hasImage()) {
                        panel = new ImagePanel(picture);
                        panel.addMouseListener(this);
                            
                        tabbedPane.addTab(dco.getLabel(definition.getIndex()), IconLibrary._icoPicture, panel);
                        
                        imagePanels.put(Integer.valueOf(tabbedPane.getTabCount() - 1), panel);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("An error occurred while setting the images", e);
        }
    }
    
    public void refresh() {
        int caret = descriptionPane.getCaretPosition();
        
        String key = this.key;
        this.dco = null;
        this.key = null;
        
        setObject(key, moduleIdx);
        
        try {
            descriptionPane.setCaretPosition(caret);
        } catch (Exception e) {
            logger.debug("Error while setting the quick view caret position", e);
        }
    }
    
    public void setObject(String key, int moduleIdx) {
    	
    	if (key == null || key.equals(this.key)) return;
    	
        Collection<Integer> fields = new ArrayList<Integer>();
        QuickViewFieldDefinitions definitions = 
            (QuickViewFieldDefinitions) DcModules.get(moduleIdx).getSettings().getDefinitions(DcRepository.ModuleSettings.stQuickViewFieldDefinitions);
        
        DcModule module = DcModules.get(moduleIdx);
        for (QuickViewFieldDefinition def : definitions.getDefinitions())
            if (def != null &&  module.getField(def.getField()) != null && 
                def.isEnabled() && module.getField(def.getField()).isEnabled()) {
             
                fields.add(def.getField());
            }
        
        Connector connector = DcConfig.getInstance().getConnector();
        setObject(connector.getItem(moduleIdx, key, module.getMinimalFields(fields)));
    }   
    
    public void setObject(DcObject dco) {
        try {
            
            if (dco == null) return;
            
            int tab = tabbedPane.getSelectedIndex();
            moduleIdx = dco.getModule().getIndex();
            
            clear();
            
            this.dco = dco;
            this.key = dco.getID();
            
            if (DcModules.getCurrent().isAbstract())
            	this.dco.reload();
            
            String html = "<html><body >\n" +
                            getDescriptionTable(dco) +
                          "</body> </html>";
            
            descriptionPane.setHtml(html);
            descriptionPane.setBackground(
            		ComponentFactory.getColor(DcRepository.Settings.stQuickViewBackgroundColor));
            
            try {
                descriptionPane.setCaretPosition(0);
            } catch (Exception exp) {}
            
            attachmentsPanel.setObjectID(dco.getID());
            
            List<DcObject> references = DcConfig.getInstance().getConnector().getReferencingItems(moduleIdx, dco.getID());
            
            if (relatedItemsPanel != null) {
            	relatedItemsPanel.clear();
            	relatedItemsPanel = null;
            }
            
            if (references.size() > 0) {
            	relatedItemsPanel = new RelatedItemsPanel(dco, true);
            	relatedItemsPanel.setData(references);
            }
            
            if (dco.getModule().getSettings().getBoolean(DcRepository.ModuleSettings.stShowPicturesInSeparateTabs))
                createImageTabs(dco);
            
            boolean error = true;
            tab += 1;
            
            // prevent endless loop
            int counter = 0;
            while (error && counter < 6) {
                counter ++;
                try {
                    tab -= 1;
                    tabbedPane.setSelectedIndex(tab);
                    error = false;
                } catch (Exception ignore) {}
            }
        } catch (Exception e) {
            logger.error("An error occurred while setting the information of " + dco, e);
        }        
    }
    
    public void clear() {
        dco = null;
        key = null;

        descriptionPane.setHtml("<html><body " + 
                Utilities.getHtmlStyle(DcSettings.getColor(DcRepository.Settings.stQuickViewBackgroundColor)) + ">\n</body> </html>");
        
    	reset();
    }
    
    private void removeTabs() {
        tabbedPane.removeChangeListener(this);
        tabbedPane.removeAll();
        tabbedPane.addChangeListener(this);
    }
    
    private void reset() {
        removeTabs();

        attachmentsPanel.reset();
        
        tabbedPane.addTab(DcResources.getText("lblDescription"), IconLibrary._icoInformation, scroller);
        
        if (showAttachments && dco != null)
        	tabbedPane.addTab(DcResources.getText("lblAttachments"), IconLibrary._icoAttachments, attachmentsPanel);
        
        if (relatedItemsPanel != null)
        	tabbedPane.addTab(DcResources.getText("lblRelatedItems"), IconLibrary._icoRelations, relatedItemsPanel);
        
        for (ImagePanel panel : imagePanels.values())
            panel.clear();        
        
        imagePanels.clear();
    }
    
    private String getDescriptionTable(DcObject dco) {
        DcFont font = DcSettings.getFont(DcRepository.Settings.stStandardFont);
        
        String table = "<div " + Utilities.getHtmlStyle("margin-bottom: 10pt;", null, font, font.getSize() + 4) + ">" +
        		"<b>" + dco.toString() + "</b><br></div>";
        table += "<table " + Utilities.getHtmlStyle(DcSettings.getColor(DcRepository.Settings.stQuickViewBackgroundColor)) + ">";
        
        QuickViewFieldDefinitions definitions = (QuickViewFieldDefinitions) 
            dco.getModule().getSettings().getDefinitions(DcRepository.ModuleSettings.stQuickViewFieldDefinitions);
        
        for (QuickViewFieldDefinition def : definitions.getDefinitions()) {
            
            if (dco.getField(def.getField()) == null) {
                logger.error("Field " + def.getField() + " not found for module " + dco.getModule());
                continue;
            }
            
            if (dco.getField(def.getField()).isEnabled() && def.isEnabled()) 
                table = addTableRow(dco, table, def.getField(), def.getDirectrion(), def.getMaxLength());    
        }
        
        table += "</table>";    
        
        if (dco.getModule().getChild() != null && dco.getModule().getIndex() != DcModules._USER) {
            table += "\n\n";
            table += getChildTable(dco);
        }
        
        return table;
    }
    
    private String getChildTable(DcObject dco) {
        DcModule module = dco.getModule().getChild();
        
        QuickViewFieldDefinitions definitions = 
            (QuickViewFieldDefinitions) module.getSettings().getDefinitions(DcRepository.ModuleSettings.stQuickViewFieldDefinitions);
        
        if (!module.isAbstract()) {
            Collection<Integer> additional = new ArrayList<Integer>();
            for (QuickViewFieldDefinition definition : definitions.getDefinitions())
                if (definition.isEnabled())
                    additional.add(definition.getField());
            
            additional.add(DcObject._ID);
            
            int[] fields = new int[additional.size()];
            int idx = 0;
            for (Integer i : additional)
                fields[idx++] = i.intValue();
            
            dco.loadChildren(fields);
        } else {
            dco.loadChildren(new int[] {DcObject._ID});
        }
        
        Collection<DcObject> children = dco.getChildren();
        
        if (children == null || children.size() == 0)
            return "";
        
        Font font = ComponentFactory.getStandardFont();
        font = font.deriveFont(Font.BOLD);

        String table = "<p " + Utilities.getHtmlStyle(null, null, 
        		new DcFont(font.getName(), font.getStyle(), font.getSize()), font.getSize() + 4) + ">" +
                "<b>" + module.getObjectNamePlural()  + "</b></p>";
        
        table += "<table " + Utilities.getHtmlStyle(DcSettings.getColor(DcRepository.Settings.stQuickViewBackgroundColor)) + ">\n";
        
        boolean first;
        StringBuffer description;
        String value;

        for (DcObject child : children) {
            
            if (module.isAbstract())
                child.load(new int[] {DcObject._ID, child.getModule().getDisplayFieldIdx()});
            
            table += "<tr><td>";

            first = true;
            description = new StringBuffer();
            for (QuickViewFieldDefinition definition : definitions.getDefinitions()) {
                value = child.getDisplayString(definition.getField());
                
                if (definition.isEnabled() && value.trim().length() > 0) {
                    
                    if (first) description.append("<b>");
                    
                    if (!first) description.append(" ");
                    
                    description.append(value);
                    
                    if (first) description.append("</b>");
                    
                    first = false;
                }
            }
            
            table += descriptionPane.createLink(child, description.toString());
            table += "</td></tr>";
        }
        table += "</table>";
        return table;
    }
    
    @SuppressWarnings("unchecked")
    protected String addTableRow(DcObject dco, String htmlTable, int index, String direction, int maxLength) {
        
        String table = htmlTable;
        
        if (	dco.isEnabled(index) && // field must be enabled
        		(dco.getField(index).getFieldType() != ComponentFactory._PICTUREFIELD || showInlineImages)) {
        	
            Font f = ComponentFactory.getStandardFont();
            DcFont fText = new DcFont(f.getName(), f.getStyle(), f.getSize());
            
            boolean horizontal = direction.equals(_DIRECTION_HORIZONTAL) || direction.toLowerCase().equals("horizontal");

            if (!CoreUtilities.isEmpty(dco.getValue(index))) {

            	table += "<tr><td>";
                
                if (dco.getField(index).getFieldType() != ComponentFactory._PICTUREFIELD)
                    table += "<b>" + dco.getLabel(index) + "</b>";
                
                if (!horizontal) {
                    table += "</td></tr>";
                    table += "<tr><td>";
                } else {
                    table += "&nbsp;";
                }

                String value = "";
                
                DcField field = dco.getField(index);
                
                if (dco.getModule().getType() == DcModule._TYPE_MEDIA_MODULE && index == DcMediaObject._E_RATING) {
                    int rating = ((Long) dco.getValue(index)).intValue();
                    value = Utilities.getHtmlRating(rating);
                } else if (field.getFieldType() == ComponentFactory._FILEFIELD ||
                    dco.getField(index).getFieldType() == ComponentFactory._FILELAUNCHFIELD) { 
                
                    String filename = dco.getDisplayString(index);
                    if (filename.startsWith("./") || filename.startsWith(".\\"))
                        filename = new File(DcConfig.getInstance().getInstallationDir(), filename.substring(2, filename.length())).toString();
                    
                    value = "<a href=\"file://" + filename +  "?original=" +filename+  "\" " + Utilities.getHtmlStyle(fText) + ">" + new File(filename).getName() + "</a>";                        
                } else if (field.getFieldType() == ComponentFactory._PICTUREFIELD && showInlineImages) {
                	Picture p = (Picture) dco.getValue(index);
                	if (DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_CLIENT) {
                	    value = "<img src=\"" + p.getThumbnailUrl() + "\" alt=\"" + dco.getLabel(index) + "\">";
                	} else {
                	    value = "<img src=\"file:///" + DcConfig.getInstance().getImageDir() + "/" + p.getScaledFilename() + 
                	            "\" alt=\"" + dco.getLabel(index) + "\"\">";
                	}
                } else if (field.getFieldType() == ComponentFactory._URLFIELD) {
                	value = "<a href=\"" +  dco.getValue(index) + "\" " + Utilities.getHtmlStyle(fText) + ">" + DcResources.getText("lblLink") + "</a>";
                } else if (field.getFieldType() == ComponentFactory._REFERENCEFIELD ||
                		   field.getFieldType() == ComponentFactory._REFERENCESFIELD ||
                		   field.getFieldType() == ComponentFactory._TAGFIELD) {
                    
                    if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                        int i = 0;
                        
                        List<DcObject> references = (List<DcObject>) dco.getValue(index);
                        for (DcObject reference : references) {
                            if (i > 0 && horizontal)
                                value += "&nbsp;&nbsp;";
                            
                            if (reference instanceof DcMapping) 
                                reference = ((DcMapping) reference).getReferencedObject();
                            
                            if (reference == null)
                                continue;
                            
                            value += descriptionPane.createLink(reference, reference.toString());
                            
                            if (!horizontal)
                                value += "<br>";
                                
                            i++;
                        }
                    } else {
                    	Connector connector = DcConfig.getInstance().getConnector();
                        Object o = dco.getValue(index);
                        DcObject reference = o instanceof DcObject ? (DcObject) o : 
                                connector.getItem(dco.getField(index).getReferenceIdx(), (String) o);
                        reference = reference == null && o instanceof String ? 
                                connector.getItemByKeyword(dco.getField(index).getReferenceIdx(), (String) o) : reference;
                                
                        if (reference != null)
                            value += descriptionPane.createLink(reference, reference.toString());
                        else
                            logger.warn("Could not find referenced object for " + o + " to display it on the QuickViewPanel");
                    }
                } else { // Add simple value
                    value = dco.getDisplayString(index);
                    
                    if (field.getValueType() == DcRepository.ValueTypes._STRING) {
                    	value = value.replaceAll("(\r\n|\n)", "<br />");
                        value = value.replaceAll("[\t]", "    ");
                    }
                  
                    if (maxLength > 0)
                        value = StringUtils.concatUserFriendly(value, maxLength);
                }
                
                table += value;
                table += "</td></tr>";
            } 
        }
        
        return table;
    }    
    
    private void buildPanel() {
        // description panel
    	tabbedPane.setFont(ComponentFactory.getSystemFont());
    	
        descriptionPane = ComponentFactory.getHtmlEditorPane();
        descriptionPane.addMouseListener(this);

        scroller = new JScrollPane(descriptionPane);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // add the standard tabs
        tabbedPane.addTab(DcResources.getText("lblDescription"), IconLibrary._icoInformation ,scroller);
        
        add(tabbedPane, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
            new Insets(4, 0, 5, 0), 0, 0));
        
        String html = "<html><body " + Utilities.getHtmlStyle(DcSettings.getColor(DcRepository.Settings.stQuickViewBackgroundColor)) + "></body> </html>";
        descriptionPane.setHtml(html);
    }    
    
    private void showPopupMenu(int x, int y) {
    	if (isAllowPopup) {
	        PopupMenu popupMenu = new PopupMenu();
	        
	        if (descriptionPane.isShowing())
	            popupMenu.show(descriptionPane, x, y);
	        else 
	            popupMenu.show(this, x, y);
    	}
    }    
    
    @Override
    public void stateChanged(ChangeEvent evt) {
        JTabbedPane pane = (JTabbedPane) evt.getSource();
        
        if (pane.getSelectedIndex() > 0)
            loadTab();
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e))
            showPopupMenu(e.getX(), e.getY());

        if (e.getClickCount() == 2 && dco != null) {
            GUI.getInstance().getSearchView(moduleIdx).getCurrent().open(
                    !DcSettings.getBoolean(DcRepository.Settings.stOpenItemsInEditModus));
        }
            
    }

    @Override
	public void setFont(Font font) {
		super.setFont(font);
		
		if (tabbedPane != null)
			tabbedPane.setFont(font);
	}                                      

	@Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    
    private class PopupMenu extends DcEditorPopupMenu {
        public PopupMenu() {
            super(descriptionPane);
            
            addSeparator();

            if (!descriptionPane.isShowing())
                removeAll();
            
            PluginHelper.add(this, "ToggleQuickView");
            PluginHelper.add(this, "QuickViewSettings");
        }
    }   
}
