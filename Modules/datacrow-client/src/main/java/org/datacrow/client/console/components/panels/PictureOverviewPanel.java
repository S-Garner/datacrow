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
import org.datacrow.client.console.components.lists.DcListModel;
import org.datacrow.client.console.components.lists.DcPicturesList;
import org.datacrow.client.console.menu.DcPicturesPanelMenu;
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
//    	this.objectID = objectID;
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
    	
    	private final DcPicturesPanelMenu menu;
    	
    	private boolean newItemMode = true;
    	
        private final DcPicturesList list;
        private String objectID;
        
        private boolean readonly = false;
    	
    	private PictureListPanel(boolean readonly, boolean newItemMode, int mode) {
    		
    		this.newItemMode = newItemMode;
    		this.readonly = readonly;
    		this.menu = new DcPicturesPanelMenu(this, newItemMode);
    		this.list = new DcPicturesList(newItemMode, mode);
    		this.list.setModel(new DcListModel<Object>());
    		
    		addComponentListener(new ResizeListener());
    		
    		build();
    	}
    	
    	private void build() {
            list.addMouseListener(this);
            
            JScrollPane scroller = new JScrollPane(list);
            scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
            scroller.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            
            setLayout(Layout.getGBL());
            
            if (allowActions()) {
            	this.add(menu, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                         GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                         new Insets(4, 5, 0, 5), 0, 0));
            }

            add(scroller,  Layout.getGBC( 0, 2, 1, 1, 100.0, 100.0
                    ,GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                     new Insets(5, 5, 5, 5), 0, 0));
    	}
    	
        public void setObjectID(String objectID) {
        	this.objectID = objectID;
        	this.list.setObjectID(objectID);
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
        
        public Collection<Picture> getPictures() {
        	return list.getPictures();
        }
        
        private void deletePicture(Picture picture) {
        	if (picture != null) {
        		
        		if (!newItemMode)
        			DcConfig.getInstance().getConnector().deletePicture(picture);
        		
        		SwingUtilities.invokeLater(new Thread(new Runnable() { 
                        @Override
                        public void run() {
                        	list.remove(picture);
                        	
                        	if (!newItemMode)
                        		GUI.getInstance().getSearchView(DcModules.getCurrent().getIndex()).getCurrent().update(picture.getObjectID());
                        }
                    }));
        	}
        }
        
        private void addPictureFromFile() {
        	BrowserDialog dlg = new BrowserDialog(DcResources.getText("lblSelectFile"), new PictureFileFilter());
        	File[] files = dlg.showSelectMultipleFilesDialog(this, null);
        	list.addPictures(files);
        }
        
        private void addPictureFromUrl() {
            OpenFromUrlDialog dialog = new OpenFromUrlDialog();
            dialog.setVisible(true);
            
            DcImageIcon image = dialog.getImage();
            if (image != null) {
            	Picture picture = new Picture(objectID, image);
            	list.addPicture(picture);
            }
        }
        
        private void addPictureFromMemory() {
            DcImageIcon image = Utilities.getImageFromClipboard();
            if (image != null) {
            	Picture picture = new Picture(objectID, image);
            	list.addPicture(picture);
            }
        }
        
        public void openPicture() {
        	
        	if (newItemMode)
        		return;
        	
        	try {
    	    	Picture picture = list.getSelectedPicture();
    	    	
    	    	if (picture != null)
    	    		new PictureEditorDialog(this, picture);
    	    	
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
        		remove(menu);
        	} else {
        		remove(menu);
        		
            	this.add(menu, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        new Insets(4, 5, 0, 5), 0, 0));
            	
            	invalidate();
            	repaint();
        	}
        	
            SwingUtilities.invokeLater(
                    new Thread(new Runnable() { 
                        @Override
                        public void run() {
                        	
                        	reset();
                        	
                        	Connector conn = DcConfig.getInstance().getConnector();
                        	Collection<Picture> pictures = conn.getPictures(objectID);
                        	
                        	for (Picture picture : pictures)
                        		list.add(picture);
                        }
                    }));
        }
        
        private boolean allowActions() {
        	return !readonly;
        }
        
        public void reset() {
            if (list != null) list.clear();
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
        
    	private class ResizeListener extends ComponentAdapter {
    		public void componentResized(ComponentEvent e) {
    			Collection<Picture> pictures = list.getPictures();
    			list.clear();

    			for (Picture p : pictures)
    				list.add(p);
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
