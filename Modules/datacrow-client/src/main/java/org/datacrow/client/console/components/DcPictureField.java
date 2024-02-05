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

package org.datacrow.client.console.components;

import java.awt.Dimension;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JToolTip;

import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.fileselection.ImageFilePreviewPanel;
import org.datacrow.client.console.menu.DcPictureFieldMenu;
import org.datacrow.client.console.windows.BrowserDialog;
import org.datacrow.client.console.windows.OpenFromUrlDialog;
import org.datacrow.client.console.windows.PictureDialog;
import org.datacrow.client.util.Utilities;
import org.datacrow.client.util.filefilters.PictureFileFilter;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.filefilters.DcFileFilter;

public class DcPictureField extends JComponent implements ActionListener, MouseListener, DropTargetListener {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DcPictureField.class.getName());
    
    private final DcPicturePane pane;
    
    private DcPictureFieldMenu menu;
    
    private Picture picture;

    private boolean changed = false;
    
    public DcPictureField() {
    	this(true, false);
    }
    
    public DcPictureField(boolean scaled, boolean allowActions) {
        this.setLayout(Layout.getGBL());
        
        if (allowActions) {
        	
        	this.menu = new DcPictureFieldMenu(this);
            this.add(menu, Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                     GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                     new Insets(5, 5, 5, 5), 0, 0));
            
            addMouseListener(this);
            
            new DropTarget(this, DnDConstants.ACTION_COPY, this);
        }
        
        pane = new DcPicturePane(scaled);
        
        
        this.add(pane, Layout.getGBC(0, 1, 1, 1, 80.0, 80.0,
                 GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(1, 1, 1, 1), 0, 0));
    }
    
    public void setScaled(boolean scaled) {
        pane.setScaled(scaled);
    }

    public void setPicture(Picture picture) {
    	this.picture = picture; 
        pane.setImageIcon(picture.getImageIcon());
    }

    public boolean isChanged() {
        return changed;
    }
    
    public void clear() {
        menu = null;
        pane.clear();
    } 
    
    public void flushImage() {
    	pane.clear();
    }
    
    public boolean isEmpty() {
    	return !pane.hasImage();
    }

    public Picture getPicture() {
    	DcImageIcon imageIcon = pane.getImageIcon();
    	picture.setImageIcon(imageIcon);
    	return picture;
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
    
    private void openImageFromURL() {
        OpenFromUrlDialog dialog = new OpenFromUrlDialog();
        dialog.setVisible(true);
        
        DcImageIcon image = dialog.getImage();
        if (image != null) {
        	pane.setImageIcon(image);
            changed = true;
            dialog.setImage(null);
        }
    }
    
    private void openImage() {
    	DcImageIcon image = pane.getImageIcon();
    	
    	if (image != null) {
            PictureDialog dlg = new PictureDialog(picture);
            
            if (dlg.isPictureChanged()) {
            	pane.setImageIcon(dlg.getImage());
                changed = true;
                repaint();
                revalidate();
            }
    	}
    }
    
    private void openImageFromFile() {
        try {
            BrowserDialog dialog = new BrowserDialog(DcResources.getText("lblSelectImageFile"), new PictureFileFilter());
            dialog.setPreview(new ImageFilePreviewPanel());
            
            File file = dialog.showOpenFileDialog(this, null);
            if (file != null && file.isFile()) {
                BufferedImage bi = ImageIO.read(file);
                DcImageIcon icon = new DcImageIcon(bi);
                pane.setImageIcon(icon);
                changed = true;
            }
        } catch (Exception e) {
            logger.error("An error occured while reading the image", e);
        }
    }
    
    private void saveToFile() {
    	
    	DcImageIcon image = pane.getImageIcon();
    	
        if (image != null) {
            BrowserDialog dlg = new BrowserDialog(DcResources.getText("lblSelectFile"), 
                                                  new DcFileFilter("jpg"));
            
            File file = dlg.showCreateFileDialog(this, null);

            try {
                if (file != null) {
                    String filename = file.toString();
                    filename += filename.toLowerCase().endsWith("jpg") || filename.toLowerCase().endsWith("jpeg") ? "" : ".jpg";
                    CoreUtilities.writeToFile(image, new File(filename));
                }
            } catch (Exception e) {
                GUI.getInstance().displayErrorMessage(CoreUtilities.isEmpty(e.getMessage()) ? e.toString() : e.getMessage());
                logger.error("An error occurred while saving the image", e);
            }
        }
    }
    
    public void setEditable(boolean b) {
    	setEnabled(b);
    	if (!b) {
    		remove(menu);
    	}
    }
    
    private void paste() {
        DcImageIcon icon = Utilities.getImageFromClipboard();
        if (icon != null) {
        	pane.setImageIcon(icon);
            changed = true;
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
    	if (!isEnabled())
    		return;
    	
        String action = e.getActionCommand();
        if (action.equals("open_from_file")) {
            openImageFromFile();
        } else if (action.equals("open_from_url")) {
            openImageFromURL();
        } else if (action.equals("Save as")) {
            saveToFile();
        } else if (action.equals("delete")) {
            setPicture(null);
            changed = true;
        } else if (action.equals("rotate_right")) {
            pane.rotate(90);
            changed = true;
        } else if (action.equals("rotate_left")) {
        	pane.rotate(90);
        	pane.rotate(90);
        	pane.rotate(90);
        	changed = true;
        } else if (action.equals("grayscale")) {
        	pane.grayscale();
        	changed = true;
        } else if (action.equals("sharpen")) {
        	pane.sharpen();
        	changed = true;
        } else if (action.equals("blur")) {
        	pane.blur();
        	changed = true;
        } else if (action.equals("open_from_clipboard")) {
            paste();
        }
    }

    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }
    
    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {
    	if (!isEnabled())
    		return;
    	
        if (e.getClickCount() == 2) {
        	DcImageIcon image = pane.getImageIcon();
        	
            if (image == null)
                openImageFromFile();
            else 
                openImage();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    
    private void checkDragAction(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
        } else {
            dtde.rejectDrag();
        }
    }
    
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
            	
            	PictureFileFilter filter = new PictureFileFilter();
            	
                List<File> transferData = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                if (transferData != null && transferData.size() > 0) {
                	
                	File file = transferData.get(0);
                	if (file.isFile() && filter.accept(file)) {
                        BufferedImage bi = ImageIO.read(file);
                        DcImageIcon icon = new DcImageIcon(bi);
                    	pane.setImageIcon(icon);
                        changed = true;
                	}
                	
                    dtde.dropComplete(true);
                }
            } catch (Exception e) {
                logger.warn(e, e);
            }
        } else {
            dtde.rejectDrop();
        }
    }	
}