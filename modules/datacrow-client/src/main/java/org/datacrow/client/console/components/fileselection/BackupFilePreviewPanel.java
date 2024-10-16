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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JFileChooser;
import javax.swing.JScrollPane;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcLongTextField;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.utilities.CoreUtilities;

public class BackupFilePreviewPanel extends FileSelectPreviewPanel {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(BackupFilePreviewPanel.class.getName());
    
    private final DcLongTextField preview = ComponentFactory.getLongTextField();
    
    public BackupFilePreviewPanel() {
        build();
        preview.setEnabled(false);
    }
    
    @SuppressWarnings("resource")
	@Override
    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        
        // Make sure we are responding to the right event.
        if (propertyName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            
            preview.setText("");
            File selection = (File) e.getNewValue();
            
            ZipFile zipFile = null;
            InputStream is = null;
            
            try {
                if (selection == null || !selection.isFile()) return;
                
                zipFile = new ZipFile(selection);
                ZipEntry versionEntry = zipFile.getEntry("version.txt");

                if (versionEntry != null) {
                    is = zipFile.getInputStream(versionEntry);
                    String s = CoreUtilities.readInputStream(is);                    
                    preview.setText(s);
                }
                
            } catch (Exception exp) {
                logger.error(exp, exp);
            } finally {
            	try { if (zipFile != null) zipFile.close(); } catch (Exception ex) {logger.error("Could not close resource");}
            	try { if (is != null) is.close(); } catch (Exception ex) {logger.error("Could not close resource");}
            }
        }
    }
    
    private void build() {
        setLayout(Layout.getGBL());
        JScrollPane scroller = new JScrollPane(preview);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroller, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 5, 0, 5), 0, 0));
    }
}
