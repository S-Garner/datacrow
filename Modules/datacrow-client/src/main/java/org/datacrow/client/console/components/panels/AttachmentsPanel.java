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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcMenuItem;
import org.datacrow.client.console.components.DcPanel;
import org.datacrow.client.console.components.DcPopupMenu;
import org.datacrow.client.console.components.DcShortTextField;
import org.datacrow.client.console.components.lists.DcAttachmentList;
import org.datacrow.client.console.components.lists.DcListModel;
import org.datacrow.client.console.components.lists.elements.DcAttachmentListElement;
import org.datacrow.client.console.components.lists.elements.DcListElement;
import org.datacrow.client.console.menu.DcAttachmentPanelMenu;
import org.datacrow.client.console.windows.BrowserDialog;
import org.datacrow.client.util.launcher.FileLauncher;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.attachments.Attachment;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.CoreUtilities;

public class AttachmentsPanel extends DcPanel implements MouseListener, ActionListener, KeyListener, DropTargetListener {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(AttachmentsPanel.class.getName());
    
	private String objectID;
	
    private final List<DcListElement> elementsAll = new ArrayList<DcListElement>();
    private final List<DcListElement> elementsCurrent = new ArrayList<DcListElement>();
    private final DcAttachmentList list = new DcAttachmentList();
    
    private final boolean readonly;
    
    public AttachmentsPanel(boolean readonly) {
        
        this.list.setModel(new DcListModel<Object>());
        this.readonly = !DcConfig.getInstance().getConnector().getUser().isAuthorized("EditAttachments") || readonly;
        
        setTitle(DcResources.getText("lblAttachments"));
        
        build();

        if (!readonly)
        	new DropTarget(this, DnDConstants.ACTION_COPY, this);
    }
    
    public void setObjectID(String objectID) {
    	this.objectID = objectID;
    }

    private void deleteAttachments() {
    	List<Attachment> attachments = list.getSelectedAttachments();
    	String msg = attachments.size() > 1 ? "msgDeleteAttachmentsConfirmation" : "msgDeleteAttachmentConfirmation";
    	   	
    	if (GUI.getInstance().displayQuestion(msg)) {
	    	for (Attachment attachment : attachments)
	    		deleteAttachment(attachment);
	    	
	    	list.clearSelection();
    	}
    }
    
    private void deleteAttachment(Attachment attachment) {
    	if (attachment != null) {
    		DcConfig.getInstance().getConnector().deleteAttachment(attachment);
    		SwingUtilities.invokeLater(new Thread(new Runnable() { 
                    @Override
                    public void run() {
                    	list.remove(attachment);
                    }
                }));
    	}
    }
    
    private void addAttachments(List<File> files) {
    	for (File file : files) {
    		if (file.isFile())
    			addAttachment(file);
    	}
    }

    private void addAttachments(File[] files) {
    	
    	if (files == null) return;
    	
    	for (File file : files) {
    		if (file.isFile())
    			addAttachment(file);
    	}
    }
    
    private void addAttachment() {
    	BrowserDialog dlg = new BrowserDialog(DcResources.getText("lblSelectFile"));
    	File[] files = dlg.showSelectMultipleFilesDialog(this, null);
    	addAttachments(files);
    }
    
    private void addAttachment(File file) {
    	if (file == null) return;
    		
		long maxSize = DcSettings.getLong(DcRepository.Settings.stMaximumAttachmentFileSize);
		
		if (file.length() > maxSize * 1000) {
			GUI.getInstance().displayWarningMessage(DcResources.getText("msgFileIsTooLarge", 
					new String[] {String.valueOf(maxSize), String.valueOf(file.length() / 1000)}));
		} else {
    		try {
	    		Attachment attachment = new Attachment(objectID, file);
	    		
	    		// check if file has not already been attached - allow user to overwrite the existing attachment
	    		if (list.getAttachments().contains(attachment)) {
	    			if (GUI.getInstance().displayQuestion("msgAttachmentAlreadyExistsOverwrite"))
	    				deleteAttachment(attachment);
	    			else 
	    				return;
	    		} 
	    		
	    		attachment.setData(CoreUtilities.readFile(file));
	    		
	    		DcConfig.getInstance().getConnector().saveAttachment(attachment);
	    		
	    		SwingUtilities.invokeLater(new Thread(new Runnable() { 
                    @Override
                    public void run() {
                    	list.add(attachment);
                    	elementsAll.clear();
                    	elementsAll.addAll(list.getElements());
                    }
                }));
	    		
    		} catch (Exception e) {
    			GUI.getInstance().displayErrorMessage(DcResources.getText("msgCouldNotReadAttachment"));
    			logger.error(e, e);
    		}
		}
    }
    
    public void openAttachment() {
    	try {
	    	Attachment attachment = list.getSelectedAttachment();
	    	
    		DcConfig.getInstance().getConnector().loadAttachment(attachment);
    		
    		String tmpdir = CoreUtilities.getTempFolder();
    		File file = new File(tmpdir, attachment.getObjectID() + "_" + attachment.getName());
    		
    		CoreUtilities.writeToFile(attachment.getData(), file);
    		attachment.setLocalFile(file);
    		
    		file.deleteOnExit();
	    	
	        new FileLauncher(attachment.getLocalFile()).launch();
	        
	        attachment.clear();
	        
    	} catch (Exception e) {
    		GUI.getInstance().displayErrorMessage(e.getMessage());
    		logger.error(e, e);
    	}
    }
    
    public void load() {
        SwingUtilities.invokeLater(
                new Thread(new Runnable() { 
                    @Override
                    public void run() {
                    	
                    	reset();
                    	
                    	Connector conn = DcConfig.getInstance().getConnector();
                    	Collection<Attachment> attachments = conn.getAttachmentsList(objectID);
                    	
                    	for (Attachment attachment : attachments)
                    		list.add(attachment);
                    	
                    	elementsAll.addAll(list.getElements());
                    }
                }));
    }
    
    private boolean allowActions() {
    	return !readonly && 
    			DcConfig.getInstance().getConnector().getUser().isAuthorized("EditAttachments");
    }
    
    public void reset() {
        if (elementsAll != null) elementsAll.clear();
        if (elementsCurrent != null) elementsCurrent.clear();
        if (list != null) list.clear();
    }
    
    @Override
    public void clear() {
    	reset();
        
        super.clear();
    }

    private void build() {
        list.addMouseListener(this);
        
        JScrollPane scroller = new JScrollPane(list);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scroller.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        setLayout(Layout.getGBL());
        
        JTextField txtFilter = ComponentFactory.getShortTextField(255);
        txtFilter.addKeyListener(this);        

        JPanel panel = new JPanel();
        panel.setLayout(Layout.getGBL());
        
        
        if (allowActions()) {
        	DcAttachmentPanelMenu menu = new DcAttachmentPanelMenu(this);
            this.add(menu, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                     GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(4, 5, 0, 5), 0, 0));
        }
        
        panel.add(ComponentFactory.getLabel(DcResources.getText("lblFilter")), 
        		 Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 0, 0, 5), 0, 0));
        panel.add(txtFilter, Layout.getGBC( 1, 0, 1, 1, 100.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
        
        add(panel, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 10, 5, 0, 5), 0, 0));
        add(scroller,  Layout.getGBC( 0, 2, 1, 1, 100.0, 100.0
                ,GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
        add(getProgressPanel(), Layout.getGBC( 0, 3, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 5, 5, 5), 0, 0));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        DcAttachmentList list = (DcAttachmentList) e.getSource();
        
        if (SwingUtilities.isRightMouseButton(e)) {
            if (list.getSelectedIndex() == -1) {
                int index = list.locationToIndex(e.getPoint());
                list.setSelectedIndex(index);
            }
            
            if (list.getSelectedIndex() > -1) {
                JPopupMenu menu = new AttachmentPopupMenu(this, readonly);                
                menu.setInvoker(list);
                menu.show(list, e.getX(), e.getY());
            }
        }
        
        if (e.getClickCount() == 2 && list.getSelectedIndex() > -1) 
            openAttachment();
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
    	if (e.getActionCommand().equals("open"))
            openAttachment();
        else if (e.getActionCommand().equals("delete"))
            deleteAttachments();
        else if (e.getActionCommand().equals("add"))
            addAttachment();
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        DcShortTextField txtFilter = (DcShortTextField) e.getSource();
        String filter = txtFilter.getText();
        
        if (filter.trim().length() == 0) {
            ((DcListModel<Object>) list.getModel()).clear();
            list.addElements(elementsAll);
        } else {
            List<DcListElement> filtered = new ArrayList<DcListElement>();
            for (DcListElement el : elementsAll) {
            	DcAttachmentListElement element = (DcAttachmentListElement) el;
                if (element.getAttachment().getName().toLowerCase().contains(filter.toLowerCase()))
                    filtered.add(el);
            }
        
            ((DcListModel<Object>) list.getModel()).clear();
            list.addElements(filtered);
        }
    }  
    
    private static class AttachmentPopupMenu extends DcPopupMenu {
        
		public AttachmentPopupMenu(AttachmentsPanel ap, boolean readonly) {

            JMenuItem menuOpen = new DcMenuItem(DcResources.getText("lblOpen", ""));
            menuOpen.setIcon(IconLibrary._icoOpen);
            
            JMenuItem menuDelete = new DcMenuItem(DcResources.getText("lblDelete", ""));
            menuDelete.setIcon(IconLibrary._icoDelete);
            
            menuOpen.addActionListener(ap);
            menuOpen.setActionCommand("open");
            add(menuOpen);
            
            if (!readonly) {
            	menuDelete.addActionListener(ap);
            	menuDelete.setActionCommand("delete");
            	add(menuDelete);
            }
        }
    }

    private void checkDragAction(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
        } else {
            dtde.rejectDrag();
        }
    }
    
	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		checkDragAction(dtde);		
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		checkDragAction(dtde);
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		checkDragAction(dtde);
	}

	@Override
	public void dragExit(DropTargetEvent dte) {}

	@SuppressWarnings("unchecked")
    @Override
    public void drop(DropTargetDropEvent dtde) {
        Transferable transferable = dtde.getTransferable();
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dtde.acceptDrop(dtde.getDropAction());
            try {
                List<File> transferData = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                if (transferData != null && transferData.size() > 0) {
                	
                	addAttachments(transferData);
                    dtde.dropComplete(true);
                }
            } catch (Exception e) {
                logger.warn("Dragged item is not supported, could not be dropped", e);
            }
        } else {
            dtde.rejectDrop();
        }
    }	
}
