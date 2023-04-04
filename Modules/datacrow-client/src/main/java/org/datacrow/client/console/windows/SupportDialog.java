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

package org.datacrow.client.console.windows;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcFileField;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.Directory;

public class SupportDialog extends DcDialog implements ActionListener {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(SupportDialog.class.getName());

    private DcFileField fldTarget = ComponentFactory.getFileField(false, true);
    
    public SupportDialog() {
        super();
        
        setTitle(DcResources.getText("lblCreateSupportPackage"));

        build();
        
        pack();
        setSize(new Dimension(400, 200));
        setResizable(false);
        setCenteredLocation();
    }
    
    private void createPackage() {
        File file = fldTarget.getFile();
        
        if (file == null) {
            GUI.getInstance().displayWarningMessage(DcResources.getText("msgSelectTargetFolderFirst"));
        }
        
        FileOutputStream fos = null;
        ZipOutputStream zipOut = null;
        
        
        try {
            File zipFileName = new File(file, "dc_support.zip");
            zipFileName.delete();
            
            fos = new FileOutputStream(zipFileName);
            zipOut = new ZipOutputStream(fos);            
            
            DcConfig dcc = DcConfig.getInstance();
            
            addEntry(new Directory(dcc.getDataDir(), true, new String[] {"log", "1"}), zipOut, "log");
            addEntry(new Directory(DcConfig.getInstance().getApplicationSettingsDir(), true, null), zipOut, "settings");
            addEntry(new Directory(DcConfig.getInstance().getModuleSettingsDir(), true, null), zipOut, "modules");
            addEntry(new Directory(DcConfig.getInstance().getDatabaseDir(), true, null), zipOut, "database");
            
            GUI.getInstance().displayMessage(DcResources.getText("msgSupportFileCreated", zipFileName.toString()));
            
        } catch (Exception e) {
            GUI.getInstance().displayErrorMessage(DcResources.getText("msgErrorCreatingSupportFile", e.getMessage()));
            logger.error(e, e);
        } finally {
            try {
                zipOut.close();
                fos.close();
            } catch (Exception ignore) {}
        }
    }
    
    private void addEntry(Directory dir, ZipOutputStream zipOut, String folder) {
    	try {
    	    zipOut.putNextEntry(new ZipEntry(folder + "/"));
            zipOut.closeEntry();
    	    
            File f;
            for (String s : dir.read()) {
                f = new File(s);

                ZipEntry zipEntry = new ZipEntry(folder + "/" + f.getName());
                zipOut.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                
                FileInputStream fis = new FileInputStream(f);
                while((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }
        } catch (IOException e) {
            logger.error("An error occured while adding " + folder + " files to the support zip file", e);
        }
    }
    
    private void build() {

        getContentPane().setLayout(Layout.getGBL());
        
        getContentPane().add(ComponentFactory.getLabel(DcResources.getText("lblTargetDirectory")), 
                Layout.getGBC(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        
        getContentPane().add(fldTarget, Layout.getGBC(1, 0, 1, 1, 10.0, 10.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        
        JPanel panelAction = new JPanel();
        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        
        JButton buttonCreate = ComponentFactory.getButton(DcResources.getText("lblStart"));
        buttonCreate.addActionListener(this);
        buttonCreate.setActionCommand("create");
        
        panelAction.add(buttonCreate);
        panelAction.add(buttonClose);
        
        getContentPane().add(panelAction, Layout.getGBC(0, 1, 2, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));
    }
    
    @Override
    public void close() {
        DcSettings.set(DcRepository.Settings.stNewItemsDialogSize, getSize());
        setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close")) {
            close();
        } else if (ae.getActionCommand().equals("create")) {
            createPackage();
        }
    }
}
