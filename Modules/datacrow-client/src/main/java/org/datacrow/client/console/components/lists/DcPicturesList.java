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

package org.datacrow.client.console.components.lists;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.datacrow.client.console.GUI;
import org.datacrow.client.console.components.lists.elements.DcListElement;
import org.datacrow.client.console.components.lists.elements.DcPictureListElement;
import org.datacrow.client.console.components.renderers.DcListRenderer;
import org.datacrow.client.console.views.ISortableComponent;
import org.datacrow.core.DcConfig;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.pictures.Picture;

public class DcPicturesList extends DcList implements ISortableComponent, DropTargetListener {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DcPicturesList.class.getName());
    
	public static final int _MODE_READONLY = 0;
	public static final int _MODE_EDIT = 1;
	public static final int _MODE_REORDER = 2;
	
    private final List<String> extensions = Arrays.asList(ImageIO.getReaderFileSuffixes());
    
    private String objectID;
    
    private boolean acceptDraggedFile = false;
    private boolean newItemMode = false;
	
    public DcPicturesList(boolean newItemMode, int mode) {
        super(new DcListModel<Object>());
        setCellRenderer(new DcListRenderer<Object>(true));
        setLayoutOrientation(JList.VERTICAL_WRAP);

        if (mode == _MODE_REORDER) {
	        setDragEnabled(false);
	        setDropMode(DropMode.ON_OR_INSERT);
	
	        setTransferHandler(new MyListDropHandler(this));
	        new MyDragListener(this);
        }
        
        if (mode == _MODE_EDIT) {
        	new DropTarget(this, DnDConstants.ACTION_COPY, this);
        }
    }
    
    public void setNewItemMode(boolean newItemMode) {
    	this.newItemMode = newItemMode;
    }
    
    public void setObjectID(String objectID) {
    	this.objectID = objectID;
    }

    public List<Picture> getPictures() {
        LinkedList<Picture> pictures = new LinkedList<Picture>();
        for (DcListElement element : getElements())
        	pictures.add(((DcPictureListElement) element).getPicture());

        return pictures;
    }
    
    public List<Picture> getSelectedPictures() {
        int[] rows = getSelectedIndices();
        Object element;
        
        List<Picture> pictures = new ArrayList<Picture>();
        
        if (rows != null) {
            for (int row : rows) {
                element = getModel().getElementAt(row);
                pictures.add(((DcPictureListElement) element).getPicture());
            }
        }
        
        return pictures;
    }
    
    public Picture getSelectedPicture() {
    	DcPictureListElement element = (DcPictureListElement) getSelectedValue();
        return element != null ? element.getPicture() : null;
    }
    
    public void add(Picture picture) {
        getDcModel().addElement(new DcPictureListElement(picture, getParent().getWidth()));
        ensureIndexIsVisible(getModel().getSize());
    }
    
    public void addPictures(List<File> files) {
    	for (File file : files) {
    		if (file.isFile())
    			addPicture(file);
    	}
    }

    public void addPictures(File[] files) {
    	
    	if (files == null) return;
    	
    	for (File file : files) {
    		if (file.isFile())
    			addPicture(file);
    	}
    }
    
    public void addPicture(File file) {
    	if (file == null) return;
    		
		Picture picture = new Picture(objectID, file.toString());
		addPicture(picture);
    }
    
    public void addPicture(Picture picture) {
    
    	boolean saved = true;
    	
    	if (!newItemMode) {
    		saved = DcConfig.getInstance().getConnector().savePicture(picture);
    	}
    	
    	if (saved) {
			SwingUtilities.invokeLater(new Thread(new Runnable() { 
	            @Override
	            public void run() {
	            	add(picture);
	            	
	            	if (!newItemMode)
		            	GUI.getInstance().getSearchView(
		            			DcModules.getCurrent().getIndex()).getCurrent().update(picture.getObjectID());
	            }
	        }));
    	}
    }
    
    public void remove(Picture picture) {
        for (DcListElement element : getElements()) {
            if (((DcPictureListElement) element).getPicture().equals(picture))
                getDcModel().removeElement(element);                
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
    
    private static class MyDragListener implements DragSourceListener, DragGestureListener {
    	DcPicturesList list;

    	DragSource ds = new DragSource();

    	public MyDragListener(DcPicturesList list) {
    		this.list = list;
    	    ds.createDefaultDragGestureRecognizer(list, DnDConstants.ACTION_MOVE, this);
    	}

    	public void dragGestureRecognized(DragGestureEvent dge) {
    	    StringSelection transferable = new StringSelection(Integer.toString(list.getSelectedIndex()));
    	    ds.startDrag(dge, DragSource.DefaultCopyDrop, transferable, this);
    	}

    	public void dragEnter(DragSourceDragEvent dsde) {}

    	public void dragExit(DragSourceEvent dse) {}

    	public void dragOver(DragSourceDragEvent dsde) {}

		public void dragDropEnd(DragSourceDropEvent dsde) {}

		public void dropActionChanged(DragSourceDragEvent dsde) {}
	}

    private static class MyListDropHandler extends TransferHandler {
		DcPicturesList list;

		public MyListDropHandler(DcPicturesList list) {
			this.list = list;
		}

		public boolean canImport(TransferHandler.TransferSupport support) {
			if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				return false;
			}
			JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
			if (dl.getIndex() == -1) {
				return false;
			} else {
				return true;
			}
		}

		public boolean importData(TransferHandler.TransferSupport support) {
			if (!canImport(support)) {
				return false;
			}

			JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
			int dropTargetIndex = dl.getIndex();
			list.moveSelectedRow(dropTargetIndex);

			return true;
		}
	}
}
