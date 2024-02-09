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
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcMenuItem;
import org.datacrow.client.console.components.DcPanel;
import org.datacrow.client.console.components.DcPopupMenu;
import org.datacrow.client.console.components.lists.DcListModel;
import org.datacrow.client.console.components.lists.DcPicturesList;
import org.datacrow.client.console.components.lists.elements.DcListElement;
import org.datacrow.client.console.menu.DcPicturesPanelMenu;
import org.datacrow.client.console.windows.BrowserDialog;
import org.datacrow.client.console.windows.OpenFromUrlDialog;
import org.datacrow.client.console.windows.PictureDialog;
import org.datacrow.client.util.Utilities;
import org.datacrow.client.util.filefilters.PictureFileFilter;
import org.datacrow.core.DcConfig;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;

public class PicturesPanel extends DcPanel implements MouseListener, ActionListener, DropTargetListener {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(PicturesPanel.class.getName());
    
	private String objectID;
	
    private final List<DcListElement> elementsAll = new ArrayList<DcListElement>();
    private final List<DcListElement> elementsCurrent = new ArrayList<DcListElement>();
    private final DcPicturesList list = new DcPicturesList();
    
    private final List<String> extensions = Arrays.asList(ImageIO.getReaderFileSuffixes());
    
    private boolean readonly;
    
    private boolean acceptDraggedFile = false;
    
    public PicturesPanel(boolean readonly) {
        this.list.setModel(new DcListModel<Object>());
        this.readonly = readonly;
        
        setTitle(DcResources.getText("lblPictures"));
        
        build();

        if (!readonly)
        	new DropTarget(this, DnDConstants.ACTION_COPY, this);
    }
    
    public void setObjectID(String objectID) {
    	this.objectID = objectID;
    }

    private void deletePictures() {
    	List<Picture> pictures = list.getSelectedPictures();
    	String msg = pictures.size() > 1 ? "msgDeletePicturesConfirmation" : "msgDeletePictureConfirmation";
    	   	
    	if (GUI.getInstance().displayQuestion(msg)) {
	    	for (Picture picture : pictures)
	    		deletePicture(picture);
	    	
	    	list.clearSelection();
    	}
    }
    
    private void deletePicture(Picture picture) {
    	if (picture != null) {
    		DcConfig.getInstance().getConnector().deletePicture(picture);
    		
    		SwingUtilities.invokeLater(new Thread(new Runnable() { 
                    @Override
                    public void run() {
                    	list.remove(picture);
                    	GUI.getInstance().getSearchView(DcModules.getCurrent().getIndex()).getCurrent().update(picture.getObjectID());
                    }
                }));
    	}
    }
    
    private void addPictures(List<File> files) {
    	for (File file : files) {
    		if (file.isFile())
    			addPicture(file);
    	}
    }

    private void addPictures(File[] files) {
    	
    	if (files == null) return;
    	
    	for (File file : files) {
    		if (file.isFile())
    			addPicture(file);
    	}
    }
    
    private void addPictureFromFile() {
    	BrowserDialog dlg = new BrowserDialog(DcResources.getText("lblSelectFile"), new PictureFileFilter());
    	File[] files = dlg.showSelectMultipleFilesDialog(this, null);
    	addPictures(files);
    }
    
    private void addPictureFromUrl() {
        OpenFromUrlDialog dialog = new OpenFromUrlDialog();
        dialog.setVisible(true);
        
        DcImageIcon image = dialog.getImage();
        if (image != null) {
        	Picture picture = new Picture(objectID, image);
        	addPicture(picture);
        }
    }
    
    private void addPictureFromMemory() {
        DcImageIcon image = Utilities.getImageFromClipboard();
        if (image != null) {
        	Picture picture = new Picture(objectID, image);
        	addPicture(picture);
        }
    }
    
    private void addPicture(File file) {
    	if (file == null) return;
    		
		Picture picture = new Picture(objectID, file.toString());
		addPicture(picture);
    }
    
    private void addPicture(Picture picture) {
    
    	DcConfig.getInstance().getConnector().savePicture(picture);
    	
		SwingUtilities.invokeLater(new Thread(new Runnable() { 
            @Override
            public void run() {
            	list.add(picture);
            	elementsAll.clear();
            	elementsAll.addAll(list.getElements());
            	
            	GUI.getInstance().getSearchView(
            			DcModules.getCurrent().getIndex()).getCurrent().update(picture.getObjectID());
            }
        }));
    }
    
    public void openPicture() {
    	try {
	    	Picture picture = list.getSelectedPicture();
	    	
	    	if (picture != null)
	    		new PictureDialog(picture);
	    	
    	} catch (Exception e) {
    		GUI.getInstance().displayErrorMessage(e.getMessage());
    		logger.error(e, e);
    	}
    }
    
    public void addPictures(final Collection<Picture> pictures) {
    	SwingUtilities.invokeLater(
                new Thread(new Runnable() { 
                    @Override
                    public void run() {
                    	
                    	reset();
                    	
                    	for (Picture picture : pictures)
                    		list.add(picture);
                    	
                    	elementsAll.addAll(list.getElements());
                    }
                }));
    }
    
    public void load(int moduleIdx, String objectID) {
    	
    	setObjectID(objectID);
    	
    	this.readonly = !DcConfig.getInstance().getConnector().getUser().isEditingAllowed(DcModules.get(moduleIdx)) || readonly;
    	
        SwingUtilities.invokeLater(
                new Thread(new Runnable() { 
                    @Override
                    public void run() {
                    	
                    	reset();
                    	
                    	Connector conn = DcConfig.getInstance().getConnector();
                    	Collection<Picture> pictures = conn.getPictures(objectID);
                    	
                    	for (Picture picture : pictures)
                    		list.add(picture);
                    	
                    	elementsAll.addAll(list.getElements());
                    }
                }));
    }
    
    private boolean allowActions() {
    	return !readonly;
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
        
        JPanel panel = new JPanel();
        panel.setLayout(Layout.getGBL());
        
        if (allowActions()) {
        	DcPicturesPanelMenu menu = new DcPicturesPanelMenu(this);
            this.add(menu, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                     GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(4, 5, 0, 5), 0, 0));
        }
        
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
        DcPicturesList list = (DcPicturesList) e.getSource();
        
        if (SwingUtilities.isRightMouseButton(e)) {
            if (list.getSelectedIndex() == -1) {
                int index = list.locationToIndex(e.getPoint());
                list.setSelectedIndex(index);
            }
            
            if (list.getSelectedIndex() > -1) {
                JPopupMenu menu = new PicturePopupMenu(this, readonly);                
                menu.setInvoker(list);
                menu.show(list, e.getX(), e.getY());
            }
        }
        
        if (e.getClickCount() == 2 && list.getSelectedIndex() > -1) 
            openPicture();
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
    		openPicture();
        else if (e.getActionCommand().equals("delete"))
            deletePictures();
        else if (e.getActionCommand().equals("add_from_file"))
            addPictureFromFile();
        else if (e.getActionCommand().equals("add_from_url"))
            addPictureFromUrl();
        else if (e.getActionCommand().equals("add_from_clipboard"))
            addPictureFromMemory();
    }
    
    private static class PicturePopupMenu extends DcPopupMenu {
        
		public PicturePopupMenu(PicturesPanel ap, boolean readonly) {

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


	@SuppressWarnings("rawtypes")
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
        Transferable t = dtde.getTransferable();
        if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        	
        	acceptDraggedFile = true;
        	
            try {
                Object td = t.getTransferData(DataFlavor.javaFileListFlavor);
                
                File file;
                String ext;

                if (td instanceof List) {
                    for (Object value : ((List) td)) {
                        if (value instanceof File) {
                            file = (File) value;
                            ext = getExtension(file.getName());
                            if (!extensions.contains(ext.toLowerCase()) && !extensions.contains(ext.toUpperCase())) {
                            	acceptDraggedFile = false;
                            	break;
                            }
                        }
                    }
                } else {
                	acceptDraggedFile = false;
                }
            } catch (UnsupportedFlavorException | IOException e) {
            	logger.warn("Dragged item is not supported, could not be dropped", e);
            }
        }

        if (acceptDraggedFile)
        	dtde.acceptDrag(DnDConstants.ACTION_COPY);
        else
            dtde.rejectDrag();
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {}

	@Override
	public void dragExit(DropTargetEvent dte) {}
	
	private String getExtension(String filename) {
		int idx = filename.lastIndexOf('.');
        return idx > -1 ? filename.substring(idx + 1) : "";
	}
	
    @SuppressWarnings("unchecked")
	@Override
    public void drop(DropTargetDropEvent dtde) {
    	
    	if (!acceptDraggedFile) 
    		return;
    	
        Transferable transferable = dtde.getTransferable();
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

        	dtde.acceptDrop(DnDConstants.ACTION_COPY);
        	
            try {
                List<File> transferData = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                if (transferData != null && transferData.size() > 0) {
                 	addPictures(transferData);
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
