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

package org.datacrow.client.console.components.fileselection;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcImageLabel;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.utilities.CoreUtilities;

public class ImageFilePreviewPanel extends FileSelectPreviewPanel {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ImageFilePreviewPanel.class.getName());

    private final DcImageLabel preview = new DcImageLabel();

    public ImageFilePreviewPanel() {
        build();
        
        preview.setEnabled(false);
        preview.setMinimumSize(new Dimension(250, 250));
        preview.setPreferredSize(new Dimension(250, 250));
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        
        if (propertyName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            File selection = (File) e.getNewValue();
            
            try {
	            DcImageIcon largeIcon = new DcImageIcon(ImageIO.read(selection));
	            DcImageIcon scaledIcon = new DcImageIcon(CoreUtilities.getScaledImage(largeIcon));
	            
	            preview.setIcon(scaledIcon);
            } catch (IOException ioe) {
            	logger.error("Could not read image for preview panel. File [" + selection + "]", ioe);
            }
        }
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        
        JPanel panel = new JPanel();
        panel.setLayout(Layout.getGBL());
        
        panel.add(preview, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets( 0, 5, 0, 5), 0, 0));
        
        add(panel, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 5, 0, 5), 0, 0));        
    }    
}
