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

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcProgressBar;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.objects.DcDimension;
import org.datacrow.core.utilities.IImageConverterListener;
import org.datacrow.server.upgrade.ImageConverter;

public class ConvertImagesDialog extends DcDialog implements IImageConverterListener {

	private final DcProgressBar progressBar;
	
	private ImageConverter converter;
	
    public ConvertImagesDialog() {
    	
        super(GUI.getInstance().getRootFrame());
        
        disableXCloseWindowButton();

        progressBar = new DcProgressBar();
        progressBar.setValue(0);
        
        buildDialog();

        setTitle(DcResources.getText("lblImageConversion"));
        setIconImage(IconLibrary._icoImageSettings.getImage());
        
        pack();
        
        setSize(new DcDimension(400, 175));
        
        setCenteredLocation();
        
        resizeImages();
    }
    
    private void resizeImages() {
    	converter = new ImageConverter(this);
    	converter.start();
    }

    private void buildDialog() {
    	getContentPane().setLayout(Layout.getGBL());

        JTextArea textMessage = ComponentFactory.getTextArea();
        textMessage.setEditable(false);
        
    	textMessage.setText(DcResources.getText("msgConvertImages"));
        
        JScrollPane scrollIn = new JScrollPane(textMessage);
        scrollIn.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollIn.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollIn.setPreferredSize(new Dimension(350,50));
        scrollIn.setBorder(null);
        
        getContentPane().add(
        		scrollIn, Layout.getGBC( 1, 0, 1, 1, 90.0, 90.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 0, 5, 0), 0, 0));
        getContentPane().add(
        		progressBar, Layout.getGBC( 1, 4, 1, 1, 1.0, 1.0
               ,GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL,
                new Insets(5, 10, 5, 10), 0, 0));
    }

	@Override
	public void notifyImageProcessed() {
		SwingUtilities.invokeLater(new Thread(new Runnable() { 
            @Override
            public void run() {
            	progressBar.setValue(progressBar.getValue() + 1);
            }
        }));
	}

	@Override
	public void notifyToBeProcessedImages(int count) {
		Runnable r = new Runnable() { 
            @Override
            public void run() {
        		progressBar.setValue(0);
        		progressBar.setMaximum(count);
            }
        };
		
		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			SwingUtilities.invokeLater(new Thread(r));
		}
	}

	@Override
	public void notifyFinished() {
		
		GUI.getInstance().displayMessage(DcResources.getText("msgSuccessfullyConvertedAllImages"));
		
		SwingUtilities.invokeLater(new Thread(new Runnable() { 
            @Override
            public void run() {
    	        close();
            }
        }));
	}
	
	@Override
	public void notifyError(String s) {
		GUI.getInstance().displayErrorMessage(s);
		
        GUI.getInstance().getMainFrame().setOnExitCheckForChanges(false);
        GUI.getInstance().getMainFrame().close();		
	}
}
