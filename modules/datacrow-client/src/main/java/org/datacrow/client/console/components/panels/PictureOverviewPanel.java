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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcMenuItem;
import org.datacrow.client.console.components.DcPanel;
import org.datacrow.client.console.components.DcPopupMenu;
import org.datacrow.client.console.components.fileselection.ImageFilePreviewPanel;
import org.datacrow.client.console.components.lists.DcListModel;
import org.datacrow.client.console.components.lists.DcPicturesList;
import org.datacrow.client.console.menu.PictureOverviewEditMenu;
import org.datacrow.client.console.menu.PictureOverviewReorderMenu;
import org.datacrow.client.console.windows.BrowserDialog;
import org.datacrow.client.console.windows.IPictureEditorListener;
import org.datacrow.client.console.windows.OpenFromUrlDialog;
import org.datacrow.client.console.windows.PictureEditorDialog;
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

public class PictureOverviewPanel extends DcPanel {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(PictureOverviewPanel.class.getName());
    
    private final PictureListPanel pictureEditList;
    
    public PictureOverviewPanel(boolean newItemMode, boolean readonly) {
    	
    	this.pictureEditList = new PictureListPanel(
    			readonly,
    			newItemMode,
    			readonly ? DcPicturesList._MODE_READONLY : DcPicturesList._MODE_EDIT);

        
        setTitle(DcResources.getText("lblPictures"));
        
        build();
    }

    public void addPicture(Picture picture) {
    	pictureEditList.addPicture(picture);
    }
    
    
    public void addPictures(final Collection<Picture> pictures) {
    	pictureEditList.addPictures(pictures);
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        
        add(pictureEditList, Layout.getGBC( 0, 2, 1, 1, 100.0, 100.0
                ,GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));
    }
    
    public void setObjectID(String objectID) {
    	this.pictureEditList.setObjectID(objectID);
    }

    public void reset() {
    	this.pictureEditList.reset();
    }
    
    public void load(int moduleIdx) {
    	this.pictureEditList.load(moduleIdx);
    }
    
    public Collection<Picture> getPictures() {
    	return pictureEditList.getPictures();
    }
    
    private static class PictureListPanel extends DcPanel implements MouseListener, ActionListener, IPictureEditorListener {
    	
    	private final PictureOverviewEditMenu menuEdit;
    	private final PictureOverviewReorderMenu menuReorder;
    	
    	private boolean newItemMode = true;
    	
        private final DcPicturesList pictureEditList;
        private final DcPicturesList pictureReorderList;
        
        private JScrollPane scrollerEdit;
        private JScrollPane scrollerReorder;
        
        private String objectID;
        
        private boolean readonly = false;
        
        private int mode;
    	
    	private PictureListPanel(boolean readonly, boolean newItemMode, int mode) {
    		
    		this.mode = !DcConfig.getInstance().getConnector().getUser().isAuthorized("EditPictures") ? DcPicturesList._MODE_READONLY : mode;
    		
    		this.newItemMode = newItemMode;
    		this.readonly = readonly;
    		
    		this.menuEdit = new PictureOverviewEditMenu(this, newItemMode);
    		this.menuReorder = new PictureOverviewReorderMenu(this);
    		
    		this.pictureEditList = new DcPicturesList(newItemMode, mode);
    		this.pictureEditList.setModel(new DcListModel<Object>());
    		
    		this.pictureReorderList = new DcPicturesList(newItemMode, DcPicturesList._MODE_REORDER);
    		this.pictureReorderList.setModel(new DcListModel<Object>());
    		
    		addComponentListener(new ResizeListener());
    		
    		build();

    		setMode(mode);
    	}
    	
    	private void build() {
            pictureEditList.addMouseListener(this);
            
            scrollerEdit = new JScrollPane(pictureEditList);
            scrollerEdit.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollerEdit.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollerEdit.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
            scrollerEdit.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            
            scrollerReorder = new JScrollPane(pictureReorderList);
            scrollerReorder.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollerReorder.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollerReorder.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
            scrollerReorder.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            
            setLayout(Layout.getGBL());
            
            if (allowActions()) {
            	this.add(menuEdit, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                         GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                         new Insets(4, 5, 0, 5), 0, 0));
            }
            
            add(scrollerEdit,  Layout.getGBC( 0, 1, 1, 1, 100.0, 100.0
                    ,GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                     new Insets(5, 5, 5, 5), 0, 0));
            
            
            
            if (allowActions()) {
            	this.add(menuReorder, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                         GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                         new Insets(4, 5, 0, 5), 0, 0));
            }
            
            add(scrollerReorder,  Layout.getGBC( 0, 1, 1, 1, 100.0, 100.0
                    ,GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                     new Insets(5, 5, 5, 5), 0, 0));
    	}
    	
    	private void setMode(int mode) {
    		
    		this.mode = mode;
    		
            scrollerReorder.setVisible(false);
            menuReorder.setVisible(false);
            
            scrollerEdit.setVisible(false);
            menuEdit.setVisible(false);
            
    		if (mode == DcPicturesList._MODE_EDIT) {
                scrollerReorder.setVisible(false);
                menuReorder.setVisible(false);
                
                reload();
                
                scrollerEdit.setVisible(true);
                menuEdit.setVisible(true);
            } else {
                scrollerReorder.setVisible(true);
                menuReorder.setVisible(true);
                
                reload();
                
                scrollerEdit.setVisible(false);
                menuEdit.setVisible(false);
    		}
    		
    		invalidate();
    		repaint();    		
    	}
    	
        public void setObjectID(String objectID) {
        	this.objectID = objectID;
        	this.pictureEditList.setObjectID(objectID);
        	this.pictureReorderList.setObjectID(objectID);
        }
    	
        private void deletePictures() {
        	List<Picture> pictures = pictureEditList.getSelectedPictures();
        	String msg = pictures.size() > 1 ? "msgDeletePicturesConfirmation" : "msgDeletePictureConfirmation";
        	
        	// sort the pictures so they are removed from highest to lowest - 
        	// else you get in trouble on the renumbering after a single picture is deleted
			Collections.sort(pictures, new Comparator<Picture>() {
				
				private String filename1;
				private String filename2;
				private Integer i1;
				private Integer i2;
				
				@Override
				public int compare(Picture p1, Picture p2) {
					
					filename1 = p1.getTargetFile().getName();
					filename2 = p2.getTargetFile().getName();
					
					filename1 = filename1.substring(0, filename1.lastIndexOf("."));
					filename2 = filename2.substring(0, filename2.lastIndexOf("."));
					
					i1 = Integer.valueOf(Integer.parseInt(filename1.substring(filename1.lastIndexOf("picture") + 7)));
					i2 = Integer.valueOf(Integer.parseInt(filename2.substring(filename2.lastIndexOf("picture") + 7)));
					
					return i2.compareTo(i1);
				}
			});
        	
        	   	
        	if (GUI.getInstance().displayQuestion(msg)) {
    	    	for (Picture picture : pictures)
    	    		deletePicture(picture);
    	    	
    	    	pictureEditList.clearSelection();
        	}
        }
        
        public Collection<Picture> getPictures() {
        	return pictureEditList.getPictures();
        }
        
        private void deletePicture(Picture picture) {
        	if (picture != null) {
        		
        		if (!newItemMode)
        			DcConfig.getInstance().getConnector().deletePicture(picture);
        		
        		SwingUtilities.invokeLater(new Thread(new Runnable() { 
                        @Override
                        public void run() {
                        	pictureEditList.remove(picture);
                        	
                        	if (!newItemMode) {
                        		GUI.getInstance().getSearchView(DcModules.getCurrent().getIndex()).getCurrent().update(picture.getObjectID());
        		            	GUI.getInstance().getSearchView(DcModules.getCurrent().getIndex()).getCurrent().refreshQuickView();
                        	}
                        }
                    }));
        	}
        }
        
        private void addPictureFromFile() {
        	BrowserDialog dlg = new BrowserDialog(DcResources.getText("lblSelectFile"), new PictureFileFilter());
        	dlg.setPreview(new ImageFilePreviewPanel());
        	
        	File[] files = dlg.showSelectMultipleFilesDialog(this, null);
        	pictureEditList.addPictures(files);
        }
        
        private void addPictureFromUrl() {
            OpenFromUrlDialog dialog = new OpenFromUrlDialog();
            dialog.setVisible(true);
            
            DcImageIcon image = dialog.getImage();
            if (image != null) {
            	Picture picture = new Picture(objectID, image);
            	pictureEditList.addPicture(picture);
            }
        }
        
        private void addPictureFromMemory() {
    		SwingUtilities.invokeLater(new Thread(new Runnable() { 
                @Override
                public void run() {
                    DcImageIcon image = Utilities.getImageFromClipboard();
                    if (image != null) {
                    	Picture picture = new Picture(objectID, image);
                    	pictureEditList.addPicture(picture);
                    }
    	        }
            }));
        }
        
        public void openPicture() {
        	
        	if (newItemMode)
        		return;
        	
        	try {
    	    	Picture picture = pictureEditList.getSelectedPicture();
    	    	
    	    	if (picture != null)
    	    		new PictureEditorDialog(this, picture);
    	    	
        	} catch (Exception e) {
        		GUI.getInstance().displayErrorMessage(e.getMessage());
        		logger.error(e, e);
        	}
        }
        
        public void addPicture(final Picture picture) {
        	SwingUtilities.invokeLater(
                    new Thread(new Runnable() { 
                        @Override
                        public void run() {
                    		pictureEditList.add(picture);
                        }
                    }));
        }        
        
        public void addPictures(final Collection<Picture> pictures) {
        	SwingUtilities.invokeLater(
                    new Thread(new Runnable() { 
                        @Override
                        public void run() {
                        	reset();
                        	
                        	for (Picture picture : pictures)
                        		pictureEditList.add(picture);
                        }
                    }));
        }
        
        public void reload() {
        	if (!newItemMode)
        		load(DcModules.getCurrent().getIndex());
        }
        
        public void load(int moduleIdx) {
        	
        	setObjectID(objectID);

        	this.readonly = !DcConfig.getInstance().getConnector().getUser().isEditingAllowed(DcModules.get(moduleIdx)) || readonly;
        	
            if (readonly) {
            	remove(menuEdit);
            	remove(menuReorder);	
            }
        	
            if (objectID == null) return; 
            
            SwingUtilities.invokeLater(
                    new Thread(new Runnable() { 
                        @Override
                        public void run() {
                        	
                        	reset();
                        	
                        	Connector conn = DcConfig.getInstance().getConnector();
                        	Collection<Picture> pictures = conn.getPictures(objectID);
                        	
                        	if (mode == DcPicturesList._MODE_EDIT) {
                            	for (Picture picture : pictures)
                            		pictureEditList.add(picture);
                        	} else {
                            	for (Picture picture : pictures)
                            		pictureReorderList.add(picture);
                        	}
                        }
                    }));
        }
        
        private boolean allowActions() {
        	return !readonly;
        }
        
        public void reset() {
            if (pictureEditList != null) pictureEditList.clear();
            if (pictureReorderList != null) pictureReorderList.clear();
        }
        
        @Override
        public void clear() {
        	reset();
            
            super.clear();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        	
        	if (!allowActions()) {
        		e.consume();
        		return;
        	}
        	
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
        
        private void saveOrder() {
        	Collection<Picture> pictures = pictureReorderList.getPictures();
        	
        	LinkedList<String> order = new LinkedList<String>();
        	
        	String name;
        	for (Picture p : pictures) {
        		name = p.getFilename();
        		name = name.indexOf("/") > -1 ? name.substring(name.lastIndexOf("/") + 1) :
        			   name.substring(name.lastIndexOf("\\") + 1);
        			
        		order.add(name);
        	}
        	
        	SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					DcConfig.getInstance().getConnector().savePictureOrder(objectID, order);
					reload();
					GUI.getInstance().getSearchView(DcModules.getCurrent().getIndex()).getCurrent().update(objectID);
					GUI.getInstance().getSearchView(DcModules.getCurrent().getIndex()).getCurrent().refreshQuickView();
				}
			});
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
            else if (e.getActionCommand().equals("sort"))
                setMode(DcPicturesList._MODE_REORDER);
            else if (e.getActionCommand().equals("edit"))
                setMode(DcPicturesList._MODE_EDIT);
            else if (e.getActionCommand().equals("save_order"))
                saveOrder();
            else if (e.getActionCommand().equals("move_top"))
            	pictureReorderList.moveRowToTop();
            else if (e.getActionCommand().equals("move_up"))
            	pictureReorderList.moveRowUp();
            else if (e.getActionCommand().equals("move_down"))
            	pictureReorderList.moveRowDown();
            else if (e.getActionCommand().equals("move_bottom"))
            	pictureReorderList.moveRowToBottom();
        }
        
    	private class ResizeListener extends ComponentAdapter {
    		public void componentResized(ComponentEvent e) {
    			Collection<Picture> pictures = pictureEditList.getPictures();
    			pictureEditList.clear();

    			for (Picture p : pictures)
    				pictureEditList.add(p);
    		}
    	}
    	
        private static class PicturePopupMenu extends DcPopupMenu {
            
    		public PicturePopupMenu(ActionListener al, boolean readonly) {

                JMenuItem menuOpen = new DcMenuItem(DcResources.getText("lblOpen", ""));
                menuOpen.setIcon(IconLibrary._icoOpen);
                
                JMenuItem menuDelete = new DcMenuItem(DcResources.getText("lblDelete", ""));
                menuDelete.setIcon(IconLibrary._icoDelete);            
                
                menuOpen.addActionListener(al);
                menuOpen.setActionCommand("open");
                add(menuOpen);
                
                if (!readonly) {
                	menuDelete.addActionListener(al);
                	menuDelete.setActionCommand("delete");
                	add(menuDelete);
                }
            }
        }
    }
}
