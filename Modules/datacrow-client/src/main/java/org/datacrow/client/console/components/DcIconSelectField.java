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
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.datacrow.client.console.GUI;
import org.datacrow.client.console.windows.BrowserDialog;
import org.datacrow.client.util.filefilters.PictureFileFilter;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.utilities.CoreUtilities;

public class DcIconSelectField extends DcImageLabel implements MouseListener {

    private final Dimension size;
    
    private boolean changed = false;
    
    public DcIconSelectField(Dimension size) {
        super();
        
        setOpaque(true);
        addMouseListener(this);
        
        this.size = size;
    }
    
    public DcIconSelectField(ImageIcon icon) {
        super(icon);
        repaint();
        
        setOpaque(true);
        addMouseListener(this);
        
        this.size = new Dimension(icon.getIconWidth(), icon.getIconHeight());
    }
    
    @Override
    public void setIcon(ImageIcon icon) {
        this.icon = icon;
        repaint();
        revalidate();
    }
    
    @Override
    public Dimension getSize() {
        return size;
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    @Override
    public Dimension getMinimumSize() {
        return size;
    }

    @Override
    public Dimension getMaximumSize() {
        return size;
    }
    
    private void loadImage() {
        PictureFileFilter filter = new PictureFileFilter();
        BrowserDialog dlg = new BrowserDialog("", filter);
        File file = dlg.showOpenFileDialog(this, null);
        
        if (file == null)
            return;
        
        try {
            DcImageIcon icon = new DcImageIcon(ImageIO.read(file));
            Image image = CoreUtilities.getScaledImage(icon, size.width, size.height);
            setIcon(new DcImageIcon(image));
            image = null;
            changed = true;
        } catch (Exception exp) {
            GUI.getInstance().displayErrorMessage(exp.toString());
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        loadImage();
    }
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}

    public boolean isChanged() {
        return changed;
    }
}
