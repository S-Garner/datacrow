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

import java.awt.Component;
import java.awt.Font;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import org.datacrow.client.console.GUI;
import org.datacrow.client.console.components.DcFileChooser;
import org.datacrow.client.console.components.fileselection.FileSelectPreviewPanel;
import org.datacrow.core.DcRepository;
import org.datacrow.core.settings.DcSettings;

public class BrowserDialog extends JFrame {

    private final DcFileChooser browser = new DcFileChooser();

    public BrowserDialog(String title) {
        browser.setMultiSelectionEnabled(false);
        browser.setDialogTitle(title);
        this.setTitle(title);
    }
    
    public BrowserDialog(String title, FileFilter filter) {
        this(title);
        browser.setDialogTitle(title);
        
        if (filter != null) 
            browser.setFileFilter(filter);
    }
    
    public void setPreview(FileSelectPreviewPanel preview) {
        browser.setAccessory(preview);
        browser.addPropertyChangeListener(preview);
    }

    @Override
    public void setFont(Font font) {
        if (browser != null)
            browser.setFont(font);
    }

    public File showSelectDirectoryDialog(Component c, File file) {
        setCurrentDirectory(file);
        browser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        int result = 0;
        if (c != null) { 
            result = browser.showOpenDialog(c);
        } else {
            result = browser.showOpenDialog(GUI.getInstance().getRootFrame());
        }        
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = browser.getSelectedFile(); 
            rememberUsedDirectory(f);
            return f;
        } else {
            return null;
        }
    }
    
    public File showCreateFileDialog(Component c, File file) {
        setCurrentDirectory(file);
        browser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        int result = 0;
        if (c != null) { 
            result = browser.showSaveDialog(c);
        } else {
            result = browser.showSaveDialog(GUI.getInstance().getRootFrame());
        }

        if (result == JFileChooser.APPROVE_OPTION) {
            File f = browser.getSelectedFile(); 
            rememberUsedDirectory(f);
            return f;
        } else {
            return null;
        }
    }
    
    public File[] showSelectMultipleFilesDialog(Component c, File file) {
        setCurrentDirectory(file);
        browser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        browser.setMultiSelectionEnabled(true);
        
        int result = 0;
        if (c != null) { 
            result = browser.showOpenDialog(c);
        } else {
        	result = browser.showOpenDialog(GUI.getInstance().getRootFrame());
        }

        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = browser.getSelectedFiles();
            
            if (files.length > 0)
            	rememberUsedDirectory(files[0]);
            
            return files;
        } else {
            return null;
        }
    }
    
    public File showOpenFileDialog(Component c, File file) {
        setCurrentDirectory(file);
        browser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        
        int result = 0;
        if (c != null) { 
            result = browser.showOpenDialog(c);
        } else {
        	result = browser.showOpenDialog(GUI.getInstance().getRootFrame());
        }

        if (result == JFileChooser.APPROVE_OPTION) {
            File f = browser.getSelectedFile();
            rememberUsedDirectory(f);
            return f;
        } else {
            return null;
        }
    }
    
    private void setCurrentDirectory(File file) {
        if (file != null) {
            browser.setCurrentDirectory(file);
        } else {
            String s = DcSettings.getString(DcRepository.Settings.stLastDirectoryUsed);
            browser.setCurrentDirectory(new File(s));
        }
    }
    
    private void rememberUsedDirectory(File file) {
        if (file != null && file.getParentFile() != null) {
            DcSettings.set(DcRepository.Settings.stLastDirectoryUsed, file.getParentFile().toString());
        }        
    }
}
