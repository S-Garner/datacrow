/******************************************************************************
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.JTextComponent;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.clients.UIClient;
import org.datacrow.client.console.components.DcCheckBox;
import org.datacrow.client.console.components.DcLabel;
import org.datacrow.client.console.components.DcLongTextField;
import org.datacrow.client.console.components.IComponent;
import org.datacrow.client.console.components.panels.AttachmentsPanel;
import org.datacrow.client.console.components.panels.LoanPanel;
import org.datacrow.client.console.components.panels.PictureOverviewPanel;
import org.datacrow.client.console.components.panels.RelatedItemsPanel;
import org.datacrow.client.console.windows.DcFrame;
import org.datacrow.client.console.windows.ItemTypeDialog;
import org.datacrow.client.console.windows.loan.LoanInformationPanel;
import org.datacrow.client.console.windows.messageboxes.CancelableQuestionBox;
import org.datacrow.client.plugins.PluginHelper;
import org.datacrow.client.tabs.Tab;
import org.datacrow.client.tabs.Tabs;
import org.datacrow.client.util.Utilities;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.clients.IClient;
import org.datacrow.core.console.IMasterView;
import org.datacrow.core.console.ISimpleItemView;
import org.datacrow.core.fileimporter.FileImporter;
import org.datacrow.core.fileimporter.FileImporters;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcTemplate;
import org.datacrow.core.objects.ValidationException;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.server.Connector;
import org.datacrow.core.services.OnlineServices;
import org.datacrow.core.services.Servers;
import org.datacrow.core.utilities.Base64;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.definitions.DcFieldDefinition;
import org.datacrow.core.wf.tasks.DcTask;
import org.datacrow.core.wf.tasks.DeleteItemTask;
import org.datacrow.core.wf.tasks.SaveItemTask;

public class ItemForm extends DcFrame implements ActionListener, IClient {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ItemForm.class.getName());
    
    protected DcTask task;
    
    // the item we will be working on
    protected DcObject dco;
    // the original item (for comparison of entered value; workaround for failed save issue)
    protected DcObject dcoOrig;

    protected final int moduleIdx;

    protected final Map<DcField, JLabel> labels = new HashMap<DcField, JLabel>();
    protected final Map<DcField, JComponent> fields = new HashMap<DcField, JComponent>();

    protected final JTabbedPane tabbedPane = ComponentFactory.getTabbedPane();
    
    private PictureOverviewPanel picturesPanel;
    
    protected final boolean update;

    protected boolean readonly;
    private boolean applyTemplate = true;

    private ISimpleItemView childView;
    
    private IItemFormListener listener;
    private LoanInformationPanel panelLoans;
    private DcTemplate template;
    
    private JButton buttonClose;
    private JButton buttonDelete;
    private JButton buttonInternet;
    private JButton buttonSave;
    
    public ItemForm(
            boolean readonly,
            boolean update,
            DcObject o,
            boolean applyTemplate) {
        this(null, readonly, update, o, applyTemplate);
    }
    
    public ItemForm(    DcTemplate template,
                        boolean readonly,
                        boolean update,
                        DcObject o,
                        boolean applyTemplate) {

        super("", null);
        
        Image icon = o.getModule().getIcon32() != null ? o.getModule().getIcon32().getImage() : 
                     o.getModule().getIcon16() != null ? o.getModule().getIcon16().getImage() :
                     IconLibrary._icoMain.getImage();
        
        setIconImage(icon);
        
        this.applyTemplate =  applyTemplate && !update;
        
        if (o.getModule().getType() == DcModule._TYPE_PROPERTY_MODULE)
            setHelpIndex("dc.items.itemform_property");
        else 
            setHelpIndex("dc.items.itemform");

        this.template = template;
        this.update = update;
        this.dcoOrig = o;

        Connector connector = DcConfig.getInstance().getConnector();
        if (!update && !readonly && o.getModule().isAbstract()) {
            ItemTypeDialog dialog = new ItemTypeDialog(DcResources.getText("lblSelectModuleHelp"));
            dialog.setVisible(true);
            this.moduleIdx = dialog.getSelectedModule();
            
            if (moduleIdx == -1)
                return;
            
            String parentID = dcoOrig.getParentID();
            
            this.dcoOrig = DcModules.get(moduleIdx).getItem();
            this.dco = DcModules.get(moduleIdx).getItem();
            
            if (DcModules.getCurrent().getIndex() == DcModules._CONTAINER) {
                if (this.template == null) 
                    this.template = (DcTemplate) dco.getModule().getTemplateModule().getItem();
                
                DcObject dco = GUI.getInstance().getSearchView(
                		DcModules.getCurrent().getIndex()).getCurrent().getSelectedItem();
                this.template.createReference(DcObject._SYS_CONTAINER, dco);
            }
            
            if (!CoreUtilities.isEmpty(parentID))
                dco.setValue(dco.getParentReferenceFieldIndex(), parentID);
            
        } else {
            this.dcoOrig = update ? connector.getItem(dcoOrig.getModule().getIndex(), dcoOrig.getID()) : dcoOrig.clone();
            
            // this happens for music tracks from within the SearchForm
            if (dcoOrig == null) {
                this.dcoOrig = o.clone();
                update = false;
            }
            
            this.dco = update ? connector.getItem(dcoOrig.getModule().getIndex(), dcoOrig.getID()) : dcoOrig.clone();
            this.moduleIdx = dco.getModule().getIndex();
        }
        
        this.readonly = readonly || !connector.getUser().isEditingAllowed(dco.getModule());
        
        setTitle(!update && !readonly ?
                  DcResources.getText("lblNewItem", dco.getModule().getObjectName()) :
                  dco.toString());

        JMenuBar mb = getDcMenuBar();
        if (mb != null) setJMenuBar(mb);
        
        setResizable(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.getContentPane().setLayout(Layout.getGBL());

        initializeComponents();
        final DcModule module = DcModules.get(moduleIdx);
        JPanel panelActions = getActionPanel(module, readonly);

        addInputPanels();
        addChildrenPanel();
    	addPictureTab();
        addRelationPanel();
        
        if (module.isTopModule() && update && connector.getUser().isAuthorized("EditAttachments"))
        	addAttachmentsPanel();
        
        if (module.canBeLend() && connector.getUser().isAuthorized("Loan") && update && !readonly)
            addLoanTab();

        getContentPane().add(tabbedPane,  Layout.getGBC(0, 0, 1, 1, 100.0, 100.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                             new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(panelActions,  Layout.getGBC(0, 1, 1, 1, 0.0, 0.0
                            ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                             new Insets(0, 0, 0, 5), 0, 0));

        setRequiredFields();
        setReadonly(readonly);
        // pictures have already been added
        setData(dco, true, false);

        pack();
        
        applySettings();
        setCenteredLocation();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent we) {
                try {
                	
                    int index;
                    DcField field;
                    JComponent component;
                    for (DcFieldDefinition definition : module.getFieldDefinitions().getDefinitions()) {
                        index = definition.getIndex();
                        field = dco.getField(index);
                        component = fields.get(field);
                        
                        if (component == null)
                            break;
                        
                        if (    component.isShowing() &&
                        		(component instanceof JTextField || component instanceof DcLongTextField) && 
                                field.isEnabled() && component.getParent() != null) {
                            component.requestFocusInWindow();
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
        }); 
    }

    private JMenuBar getDcMenuBar() {
        JMenuBar mb = null;
        
        FileImporters importers = FileImporters.getInstance();
        FileImporter importer = importers.getFileImporter(moduleIdx);

        if (    importer != null && importer.allowReparsing() && 
                DcModules.get(moduleIdx).getFileField() != null) {

            mb = ComponentFactory.getMenuBar();
            JMenu menuEdit = ComponentFactory.getMenu(DcResources.getText("lblFile"));

            PluginHelper.add(menuEdit, "AttachFileInfo");
            menuEdit.addSeparator();
            PluginHelper.add(menuEdit, "CloseWindow");
            
            mb.add(menuEdit);
        }
        
        return mb;
    }
    
    public void hide(DcField field) {
    	if (labels.containsKey(field)) labels.get(field).setVisible(false);
    	if (fields.containsKey(field)) fields.get(field).setVisible(false);
    }
    
    protected void applySettings() {
        DcModule m = dcoOrig != null ? dcoOrig.getModule() : DcModules.get(moduleIdx);
        setSize(m.getSettings().getDimension(DcRepository.ModuleSettings.stItemFormSize));
    }
    
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    @Override
    public void close() {
        close(false);
    }
    
    /**
     * Closes this form by setting the visibility to false
     */
    public void close(boolean aftersave) {
    	
        if (	!aftersave && 
        		!readonly &&
        		dco != null &&
        		((update && isChanged()) || !update)) {
        	
        	int result = GUI.getInstance().displayCancelableQuestion("msgNotSaved");
        	
            if (result == CancelableQuestionBox._RESULT_YES) {
                if (!saveValues())  
                	return;
            } else if (result == CancelableQuestionBox._RESULT_CANCEL) {
            	// do nothing
            	return;
            }
        }
        
        if (DcModules.get(moduleIdx).isChildModule()) {
            // After a child has been edited from within the item form the quick view
            // of the parent module should be updated to reflect this.
            // Bit of foul play to do it here but it works..
            int parentIdx = DcModules.get(moduleIdx).getParent().getIndex();
            IMasterView mv =  GUI.getInstance().getSearchView(parentIdx);
            if (mv != null) 
                mv.getCurrent().refreshQuickView();
        }
        
    	saveSettings();
    	
    	if (labels != null)
    	    labels.clear();
    	
    	if (fields != null)
    	    fields.clear();
    	
    	listener = null;
    	template = null;  
        
        ComponentFactory.clean(getJMenuBar());
        setJMenuBar(null);
        
        dco = null;
        dcoOrig = null;
        
        if (childView != null) {
            childView.clear();
            childView = null;
        }
        
        if (panelLoans != null) panelLoans.cancel();
        
        super.close();
    }
    
    /**
     * Adds a listener to this form. When a listener has been added the item will
     * also be saved directly instead of queued (for obvious reasons).
     * @param listener
     */
    public void setListener(IItemFormListener listener) {
        this.listener = listener;
    }

    protected void saveSettings() {
        DcModules.get(moduleIdx).setSetting(DcRepository.ModuleSettings.stItemFormSize, getSize());        
    }

    public void reset() {
    	if (picturesPanel != null)
    		picturesPanel.reset();
    	
    	for (JComponent c : fields.values()) {
    		if (c instanceof IComponent)
    			((IComponent) c).reset();
    	}
    }
    
    public void setData(DcObject object, boolean overwrite, boolean addPictures) {
        try {
            dco.applyEnhancers(update);
            
            if (applyTemplate && dco.getModule().getType() != DcModule._TYPE_TEMPLATE_MODULE)
                dco.applyTemplate(template);  
            
            if (childView != null) {
                if (update)
                    childView.load();
                else
                    childView.setItems(object.getCurrentChildren());
            }
    
            int[] indices = object.getFieldIndices();
            int index;
            DcField field;
            JComponent component;
            Object oldValue;
            Object newValue;
            boolean empty;
            for (int i = 0; i < indices.length; i++) {
                index = indices[i];
                
                field = dco.getField(index);
                
                if (field.isUiOnly() && field.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION) continue;
                
                component = fields.get(field);
                oldValue = ComponentFactory.getValue(component);
                newValue = object.getValue(index);
    
                empty = CoreUtilities.getComparableString(oldValue).length() == 0;
                if ((empty || overwrite) && (!CoreUtilities.isEmpty(newValue)))
                    ComponentFactory.setValue(component, newValue);
            }
            
            if (update) {
            	for (Picture p : object.getNewPictures()) {
            		picturesPanel.addPicture(p);
            		dco.addNewPicture(p);
            	}
            } else {
                if (object.getNewPictures().size() > 0 && addPictures)
                	picturesPanel.addPictures(object.getNewPictures());
            }
            
            
        } catch (Exception e) {
            logger.error("Error while setting values of [" + dco + "] on the item form", e);
        }
    }

    private void setReadonly(boolean readonly) {
        if (readonly) {
            for (JComponent component : fields.values())
                ComponentFactory.setUneditable(component);
        }
    }

    private void setRequiredFields() {
        JLabel label;
        for (DcFieldDefinition def : DcModules.get(moduleIdx).getFieldDefinitions().getDefinitions()) {
            if (def.isRequired()) {
                label = labels.get(DcModules.get(moduleIdx).getField(def.getIndex()));
                label.setForeground(ComponentFactory.getRequiredColor());
            }
        }
    }

    protected void deleteItem() {
        
        if (GUI.getInstance().displayQuestion("msgDeleteQuestion")) {
            task = new DeleteItemTask();
            task.addItem(dco);
            task.addClient(this);
            task.addClient(new UIClient(UIClient._DELETE, dco, true, true));

            Connector connector = DcConfig.getInstance().getConnector();
            connector.executeTask(task);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isChanged(int fieldIdx) {
        boolean changed = false;
        
        DcField field = dcoOrig.getField(fieldIdx);
        
        if (field == null) {
            logger.error("Field " + fieldIdx + " not found for module " + dco.getModule());
            return changed;
        }

        JComponent component = fields.get(field);
        Object o = ComponentFactory.getValue(component);

        if (field.getValueType() == DcRepository.ValueTypes._ICON) {
            byte[] newValue = o == null ? new byte[0] : ((DcImageIcon) o).getBytes();
            
            Object oOld = dcoOrig.getValue(fieldIdx);
            byte[] oldValue;
            if (oOld instanceof String)
                oldValue = Base64.decode(((String) oOld).toCharArray());
            else 
                oldValue = (byte[]) oOld;

            oldValue = oldValue == null ? new byte[0] : oldValue;
            if (!Utilities.sameImage(newValue, oldValue)) {
                dcoOrig.setChanged(DcObject._ID, true);
                logger.debug("Field " + field.getLabel() + " is changed. Old: " + oldValue + ". New: " + newValue);
                changed = true;
            }
        } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
            List<DcMapping> oldList = dcoOrig.getValue(fieldIdx) instanceof List ? (List<DcMapping>) dcoOrig.getValue(fieldIdx) : null;
            List<DcMapping> newList = (List<DcMapping>) o;
            
            oldList = oldList == null ? new ArrayList<DcMapping>() : oldList;
            newList = newList == null ? new ArrayList<DcMapping>() : newList;
            
            if (oldList.size() == newList.size()) {
                boolean found;
                for (DcMapping newMapping : newList) {
                    found = false;
                    for (DcMapping oldMapping : oldList) {
                        if (newMapping.getReferencedID().equals(oldMapping.getReferencedID()))
                            found = true;
                    }
                    changed = !found;
                    if (changed) logger.debug("Field " + field.getLabel() + " is changed. Old: " + oldList + ". New: " + newList);
                }
            } else {
                changed = true;
                logger.debug("Field " + field.getLabel() + " is changed. Old: " + oldList + ". New: " + newList);
            }
        } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION || !field.isUiOnly()) {
            
            if (field.getValueType() == DcRepository.ValueTypes._DATE ||
                field.getValueType() == DcRepository.ValueTypes._DATETIME) {
                
                Date dateOld = (Date) dcoOrig.getValue(fieldIdx);
                Date dateNew = (Date) o;
                
                if (   (dateOld == null && dateNew != null) || (dateNew == null && dateOld != null) ||
                       (dateOld != null && dateNew != null && dateOld.compareTo(dateNew) != 0)) {
                    changed = true;
                    logger.debug("Field " + field.getLabel() + " is changed. Old: " + dateOld + ". New: " + dateNew);
                }
            } else if (field.getValueType() == DcRepository.ValueTypes._BOOLEAN) { 
                if (dcoOrig.getValue(fieldIdx) == null && ((Boolean) o) == Boolean.FALSE) {
                    changed = false;
                } else {
                    String newValue = CoreUtilities.getComparableString(o);
                    String oldValue = CoreUtilities.getComparableString(dcoOrig.getValue(fieldIdx));
                    changed = !oldValue.equals(newValue);
                }
            } else {
                String newValue = CoreUtilities.getComparableString(o);
                String oldValue = CoreUtilities.getComparableString(dcoOrig.getValue(fieldIdx));
                changed = !oldValue.equals(newValue);
                if (changed) logger.debug("Field " + field.getLabel() + " is changed. Old: " + oldValue + ". New: " + newValue);
            } 
        }
        
        return changed;
    }
    
    protected boolean isChanged() {

    	if (dcoOrig.isDestroyed()) 
            return false;
        
        boolean changed = dcoOrig.isChanged() || dco.getNewPictures().size() > 0;

        int[] indices = dcoOrig.getFieldIndices();
        int index;
        DcField field;
        for (int i = 0; i < indices.length && !changed; i++) {
            index = indices[i];
            field = dcoOrig.getField(index);
            
            if (	index == DcObject._ID || 
            		index == DcObject._SYS_CREATED || 
            		index == DcObject._SYS_MODIFIED || 
            		(field.isUiOnly() && field.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION))
                continue;
            
            changed = isChanged(index);
            
            if (changed)
            	break;
        }

        return changed;
    }
    
    public DcObject getOriginalItem() {
        return dcoOrig;
    }    
    
    public DcObject getItem() {
        apply();
        return dco;
    }
    
    protected void removeChildren() {
        dco.removeChildren(); 
    }

    public void apply() {
    	
    	if (!update && childView != null) {
	        removeChildren();
	        if (DcModules.get(moduleIdx).getChild() != null && childView != null) {
	            for (DcObject child : childView.getItems()) {
	            	child.setNew(true);
	            	child.setValue(DcObject._ID, null);
	            	child.setIDs();
	                dco.addChild(child);
	            }
	        }
    	}

        if (update) 
            dco.markAsUnchanged();
        
        JComponent component;
        Object value;
        for (DcField field : fields.keySet()) {
            component = fields.get(field);
            
            if (field.isUiOnly() && field.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION) continue;
            
            value = ComponentFactory.getValue(component);
            value = value == null ? "" : value;
            
            // do not overwrite the parent ID in case this has been filled before.
            // causes a bug where children are saved without a parent.
            if (    field.getIndex() == DcModules.get(moduleIdx).getParentReferenceFieldIndex() &&
                    CoreUtilities.isEmpty(value)) {
                continue;
            }

            if (!update) {
                dco.setValue(field.getIndex(), value);

            } else if (update && isChanged(field.getIndex())) {
                dco.setValue(field.getIndex(), value);

            } else if (isChanged(field.getIndex())) {
                
                // check if the value has been cleared manually 
                // do not save if the value is empty, however do clear it when it was not empty before 
                // (online search bug, values of added items could not be removed).
                if (!CoreUtilities.isEmpty(value) || !CoreUtilities.isEmpty(dco.getValue(field.getIndex())))
                    dco.setValue(field.getIndex(), value);
            }
        }

        if (!update && picturesPanel != null) {
        	// add pictures from the list - remove existing pictures as these will already be available on the list
        	dco.clearNewPictures();
        	dco.addNewPictures(picturesPanel.getPictures());
        }
        
        if (update && dco.getModule().canBeLend())
            dco.setLoanInformation();
    }

    private void onlineSearch() {
    	apply();
    	OnlineServices os = Servers.getInstance().getOnlineServices(moduleIdx);
    	GUI.getInstance().getOnlineSearchForm(os, dco, this, true).setVisible(true);
    }
    
    protected boolean saveValues() {
    	
    	boolean success = true;
    	
        apply();

        if (task != null)
            task.cancel();
        
        if (!update || isChanged()) {
        	
        	try {
        		
        		buttonSave.setEnabled(false);
        		buttonClose.setEnabled(false);
        		
        		if (buttonDelete != null) buttonDelete.setEnabled(false);
        		if (buttonInternet != null) buttonInternet.setEnabled(false);
        		
        		// this is now a client side validation check. It will be performed again by the server (in server client mode),
        		// if the requests makes it through.
	        	dco.checkIntegrity();

	        	task = new SaveItemTask();
	            task.addItem(dco);
	            task.addClient(this);
	            task.addClient(new UIClient(update ? UIClient._UPDATE : UIClient._INSERT, dco, true, true));
	            
	            Connector connector = DcConfig.getInstance().getConnector();
	            connector.executeTask(task);
        	} catch (ValidationException ve) {
        		GUI.getInstance().displayWarningMessage(ve.getMessage());
        		success = false;
        	}
        	
        } else {
            close();
        }
        
        return success;
    }

    private void initializeComponents() {
        int[] indices = dco.getFieldIndices();
        int index;
        DcField field;
        JComponent c;
        for (int i = 0; i < indices.length; i ++) {
            index = indices[i];
            field = dco.getField(index);
            
            if (field == null) {
                logger.error("Field " + index + " not found for module " + dco.getModule());
                continue;
            }

            labels.put(field, ComponentFactory.getLabel(dco.getLabel(index)));
            if (index == DcObject._ID) {
                fields.put(field, ComponentFactory.getIdFieldDisabled());
            } else {
                c = ComponentFactory.getComponent(field.getModule(),
                                                             field.getReferenceIdx(),
                                                             field.getIndex(),
                                                             field.getFieldType(),
                                                             field.getLabel(),
                                                             field.getMaximumLength());
                fields.put(field, c);
            }
        }
    }

    protected void addInputPanels() {
        Map<String, Integer> positions = new HashMap<String, Integer>();
        Map<String, JPanel> panels = new LinkedHashMap<String, JPanel>();
        
        DcModule module = DcModules.get(moduleIdx);
        
        String name;
        for (DcFieldDefinition definition : module.getFieldDefinitions().getDefinitions()) {
            name = definition.getTab(module.getIndex());

            // get the tab (it's created in case it doesn't exist
            if (name != null && name.trim().length() > 0)
                Tabs.getInstance().getTab(module.getIndex(), name);
        }

        // get the tabs (sorted) and initialize the panels
        JPanel panel;
        for (Tab tab : Tabs.getInstance().getTabs(moduleIdx)) {
            name = tab.getName();
            panel = new JPanel();
            panel.setLayout(Layout.getGBL());
            panels.put(name, panel);
        }
        
        // add the fields to the panels
        int y;
        int fieldIdx;
        DcField field;
        JLabel label;
        JComponent component;
        int stretch;
        int factor;
        JTextComponent longText;
        JScrollPane pane;
        int space;
        for (DcFieldDefinition definition : module.getFieldDefinitions().getDefinitions()) {
            
            name = definition.getTab(module.getIndex());
            if (name == null || name.trim().length() == 0)
                continue;

            panel = panels.get(name);
            if (!positions.containsKey(name))
                positions.put(name, Integer.valueOf(0));
            
            y = positions.get(name).intValue();
            
            fieldIdx = definition.getIndex();
            field = dco.getField(fieldIdx);
            
            if (field == null) {
                logger.error("Field " + fieldIdx + " not found for module " + dco.getModule());
                continue;
            }
            
            label = labels.get(field);
            component = fields.get(field);
            
        	if ((!field.isUiOnly() || field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) && 
        	      field.isEnabled() && 
                 (fieldIdx != dco.getParentReferenceFieldIndex() || 
                  fieldIdx == DcObject._SYS_CONTAINER )) { // not a reference field
        		
        	    stretch = GridBagConstraints.HORIZONTAL;
                factor = 10;

                if (    field.getFieldType() == ComponentFactory._LONGTEXTFIELD ||
                        field.getFieldType() == ComponentFactory._TAGFIELD) {
                    
                    stretch = GridBagConstraints.BOTH;
                    factor = 100;

                    longText = (JTextArea) component;
                    longText.setMargin(new Insets(2, 1, 2, 5));

                    if (field.isReadOnly()) 
                        ComponentFactory.setUneditable(longText);
                    
                    pane = new JScrollPane(longText);
                    ComponentFactory.setBorder(pane);
                    
                    if (field.getFieldType() == ComponentFactory._TAGFIELD)
                        pane.setPreferredSize(new Dimension(100,40));
                    else 
                        pane.setPreferredSize(new Dimension(100,100));
                    
                    pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                    pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                    component = pane;
                } 
                
                if (field.getFieldType() == ComponentFactory._REFERENCESFIELD) {
                    stretch = GridBagConstraints.BOTH;
                    factor = 10;
                }
                
                if (component instanceof DcCheckBox)
                    ((DcCheckBox) component).setText("");

                space = 10; 
                panel.add(label,     Layout.getGBC(0, y, 1, 1, 1.0, 1.0
                        ,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                         new Insets(space, 2, 2, 5), 0, 0));
                panel.add(component, Layout.getGBC(1, y, 1, 1, factor, factor
                        ,GridBagConstraints.NORTHWEST, stretch,
                         new Insets(space, 2, 2, 2), 0, 0));
                
                positions.put(definition.getTab(moduleIdx), Integer.valueOf(y + 1));
                
                if (field.isReadOnly())
                    ComponentFactory.setUneditable(component);
            }
        }
        
        boolean containsLongFields;
        JPanel dummy;
        for (String tab : panels.keySet()) {
            
            panel = panels.get(tab);
            
            if (panel.getComponents().length == 0)
                continue;
            
            // check if we have vertical stretching fields.
            containsLongFields = false;
            int componentCount = 0;
            for (Component c : panel.getComponents()) {
                if (c instanceof DcLabel)
                    componentCount += 1;
                    
                if (c instanceof JScrollPane || c instanceof JTextArea)
                    containsLongFields = true;
            }
            
            if (componentCount >= 15) {
                JScrollPane sp = new JScrollPane(panel);
                sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                dummy = new JPanel();
                dummy.setLayout(Layout.getGBL());
                dummy.add(sp,  Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                       ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                        new Insets(2, 0, 0, 0), 0, 0));
                tabbedPane.addTab(tab, Tabs.getInstance().getTab(moduleIdx, tab).getIcon(), dummy);
            
            } else if (containsLongFields) {
                tabbedPane.addTab(tab, Tabs.getInstance().getTab(moduleIdx, tab).getIcon(), panel);

            } else {
                dummy = new JPanel();
                dummy.setLayout(Layout.getGBL());
                dummy.add(panel,  Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                       ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        new Insets(2, 0, 0, 0), 0, 0));
                tabbedPane.addTab(tab, Tabs.getInstance().getTab(moduleIdx, tab).getIcon(), dummy);
            }
        }
    }

    protected void addChildrenPanel() {
        DcModule module = DcModules.get(moduleIdx);
        DcModule childModule = module.getChild();
        
        if (childModule != null) {
            childView = GUI.getInstance().getItemViewForm(childModule.getIndex(), !update);
            
            // this makes sure the parent reference ID is set
            childView.setParentID(dcoOrig.getID());
            childView.hideDialogActions(true);
            
            tabbedPane.addTab(
                    childModule.getObjectNamePlural(), 
                    childModule.getIcon16(), 
                    ((JFrame) childView).getContentPane());
        }
    }
    
    protected void addRelationPanel() {
    	if (update) {
	        if (DcModules.getActualReferencingModules(moduleIdx).size() > 0 &&
	            moduleIdx != DcModules._CONTACTPERSON &&
	            moduleIdx != DcModules._CONTAINER) {
	        	
	            Connector connector = DcConfig.getInstance().getConnector();
	            List<DcObject> references = connector.getReferencingItems(moduleIdx, dco.getID());
	            if (references.size() > 0) {
	                RelatedItemsPanel rip = new RelatedItemsPanel(dco, readonly);
	                rip.setData(references);
	                tabbedPane.addTab(rip.getTitle(), rip.getIcon(), rip);
	            }
	        }
	        
	        if (moduleIdx == DcModules._CONTACTPERSON) {
	        	panelLoans = new LoanInformationPanel(dco);
	            tabbedPane.addTab(panelLoans.getTitle(), IconLibrary._icoLoan, panelLoans);
	            panelLoans.load();
	        }
    	}
    }
    
    protected void addAttachmentsPanel() {
    	AttachmentsPanel attachmentsPanel = new AttachmentsPanel(readonly);
    	attachmentsPanel.setObjectID(dco.getID());
    	attachmentsPanel.load();
    	
    	JPanel panel = new JPanel();
    	panel.setLayout(Layout.getGBL());
    	
        panel.add(attachmentsPanel, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 0), 0, 0));
    	
    	tabbedPane.addTab(DcResources.getText("lblAttachments"), IconLibrary._icoAttachments, panel);	
    }

    protected void addLoanTab() {
        tabbedPane.addTab(DcResources.getText("lblLoan"), IconLibrary._icoLoan, new LoanPanel(dco, null));
    }

    protected void addPictureTab() {    	
    	picturesPanel = new PictureOverviewPanel(dco.isNew(), readonly);
    	picturesPanel.setObjectID(dco.getID());
    	
    	if (dco.isNew())
    		picturesPanel.addPictures(dco.getNewPictures());
    	else
    		picturesPanel.load(dco.getModuleIdx());
    	
    	tabbedPane.addTab(DcResources.getText("lblPictures"), IconLibrary._icoPicture, picturesPanel);
    }

    public JPanel getActionPanel(DcModule module, boolean readonly) {
        JPanel panel = new JPanel();
        panel.setLayout(Layout.getGBL());

        buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        buttonDelete = ComponentFactory.getButton(DcResources.getText("lblDelete"));
        buttonInternet = null;

        buttonSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
        buttonSave.addActionListener(this);
        buttonSave.setActionCommand("save");

        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        
        Connector connector = DcConfig.getInstance().getConnector();
        SecuredUser su = connector.getUser();
        
        if (   !readonly && 
        		module.hasOnlineServices() && 
                su.isAuthorized("OnlineSearch") && 
                su.isEditingAllowed(module)) {
        	
            buttonInternet = ComponentFactory.getButton(DcResources.getText("lblOnlineUpdate"));
            buttonInternet.setIcon(IconLibrary._icoSearchOnline);
            buttonInternet.setMnemonic(KeyEvent.VK_U);
            buttonInternet.addActionListener(this);
            buttonInternet.setActionCommand("onlineSearch");
            panel.add(buttonInternet, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0
                     ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
                      new Insets(5, 5, 5, 5), 0, 0));
        }
        
        if (!readonly && su.isEditingAllowed(module)) {
            panel.add(buttonSave, Layout.getGBC(1, 0, 1, 1, 1.0, 1.0
                 ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
                  new Insets(5, 5, 5, 5), 0, 0));
        }

        if (su.isAdmin() && update && !readonly) {
            buttonDelete.addActionListener(this);
            buttonDelete.setActionCommand("delete");
            panel.add(buttonDelete, Layout.getGBC(2, 0, 1, 1, 1.0, 1.0
                     ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
                      new Insets(5, 5, 5, 5), 0, 0));
        }

        panel.add(buttonClose , Layout.getGBC(3, 0, 1, 1, 1.0, 1.0
                 ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
                  new Insets(5, 5, 5, 5), 0, 0));

        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("save"))
            saveValues();
        else if (e.getActionCommand().equals("delete"))
            deleteItem();
        else if (e.getActionCommand().equals("close"))
            close(false);
        else if (e.getActionCommand().equals("onlineSearch"))
            onlineSearch();
    }

    @Override
    public void notifyTaskCompleted(boolean success, String ID) {        
        if (listener != null &&
            task != null && 
            task.getId().equals(ID) && 
            task.getType() == DcTask._TYPE_SAVE_TASK) {
            
            listener.notifyItemSaved(dco);
        }
        
        if (success)
            this.close(true);
    }
    
    @Override
    public void notify(String msg) {
        GUI.getInstance().displayMessage(msg);
    }

    @Override
    public void notifyError(Throwable t) {
        GUI.getInstance().displayErrorMessage(t.getMessage());
        logger.error(t, t);
    }

    @Override
    public boolean askQuestion(String msg) {
        return GUI.getInstance().displayQuestion(msg);
    }
    
    @Override
    public void notifyWarning(String msg) {
        GUI.getInstance().displayWarningMessage(msg);
    }

    @Override
    public void notifyTaskStarted(int taskSize) {}

    @Override
    public void notifyProcessed() {
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
