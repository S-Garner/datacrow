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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcProgressBar;
import org.datacrow.client.console.components.DcResolutionComboBox;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.settings.objects.DcDimension;
import org.datacrow.core.utilities.CoreUtilities;

public class ConvertImageSizesDialog extends DcDialog implements ActionListener {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ConvertImageSizesDialog.class.getName());

	private final DcResolutionComboBox cbResolution;
	private final DcProgressBar progressBar;
	private final JButton buttonStart;
	
	private Thread thread;
	
    public ConvertImageSizesDialog() {
        super(GUI.getInstance().getRootFrame());

        cbResolution = ComponentFactory.getResolutionCombo();
        cbResolution.setSelectedItem(DcSettings.getDimension(DcRepository.Settings.stMaximumImageResolution));
        
        progressBar = new DcProgressBar();
        progressBar.setValue(0);
        
        buttonStart = ComponentFactory.getButton(DcResources.getText("lblStart"));
        
        setTitle(DcResources.getText("lblImageSizeConversion"));
        setIconImage(IconLibrary._icoImageSettings.getImage());
        
        buildDialog();

        pack();
        
        setSize(new DcDimension(450, 350));
        
        setCenteredLocation();
    }
    
    private void resizeImages() {
    	
    	if (thread != null && thread.isAlive())
    		return;
    	
    	final DcDimension maxDim = (DcDimension) cbResolution.getSelectedItem();
    	DcSettings.set(DcRepository.Settings.stMaximumImageResolution, maxDim);
    	
    	thread = new Thread() {
    		public void run() {
    	    	Set<String> images;
    	    	String imageDir = DcConfig.getInstance().getImageDir();
    	    	
    	        try (Stream<Path> stream = Files.list(Paths.get(imageDir))) {
    	        	images = stream
    		              .filter(file -> !Files.isDirectory(file) && !file.toString().endsWith("_small.jpg"))
    		              .map(Path::getFileName)
    		              .map(Path::toString)
    		              .collect(Collectors.toSet());
    	        	
    	        	progressBar.setValue(0);
    	        	progressBar.setMaximum(images.size());
    	        	buttonStart.setEnabled(false);
    	        	cbResolution.setEnabled(false);
    	        	
    	        	DcImageIcon image;
    	        	File src;
    	        	File cpy;
    	        	
    	            for (String imageFile : images) {
	            		src = new File(imageDir, imageFile);
	            		cpy = new File(imageDir, CoreUtilities.getUniqueID() + ".jpg");
    	            	
    	            	CoreUtilities.copy(src, cpy, true);
    	            	
    	            	image = new DcImageIcon(cpy);
    	            	
    	            	if (image.getIconWidth() > maxDim.getWidth() || 
    	            		image.getIconHeight() > maxDim.getHeight()) {
    	            		
    	            		try {
    	            			CoreUtilities.writeMaxImageToFile(image, new File(imageDir, imageFile));
    	            		} catch (Error e) {
    	            			if (e instanceof OutOfMemoryError)
    	            				throw e;
    	            			
    	            			logger.error("Skipping resizing of image [" + src + "] dur to an error.", e);
    	            		}
    	            	}

	    	            try {
	    	            	sleep(20);
	    	            } catch (Exception e) {
	    	            	logger.debug(e, e);
	    	            }
    	            	
    	            	image.flush();
    	            	cpy.delete();
    	            	
    	        		SwingUtilities.invokeLater(new Thread(new Runnable() { 
    	                    @Override
    	                    public void run() {
    	                    	progressBar.setValue(progressBar.getValue() + 1);
    	                    }
    	                }));
    	            }
    	            
    	            // we're done - no need to redo
    	            DcSettings.set(DcRepository.Settings.stMaximumImageResolutionConvertOnStartup, Boolean.FALSE);
    	            
    	            
    	            GUI.getInstance().displayMessage(DcResources.getText("msgSuccessfullyResizedAllImages"));
    	            
    	            cbResolution.setEnabled(true);
    	            buttonStart.setEnabled(true);
    	            
    	        } catch (Exception e) {
    	        	GUI.getInstance().displayErrorMessage(DcResources.getText("msgImageConversionFailed"));
    	        	logger.error(e, e);
    	        	
    	            GUI.getInstance().getMainFrame().setOnExitCheckForChanges(false);
    	            GUI.getInstance().getMainFrame().close();
    	        }
    		};
    	};
    	
    	thread.start();
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
        
        buttonStart.addActionListener(this);
        buttonStart.setActionCommand("start");

        getContentPane().add(scrollIn, Layout.getGBC( 1, 0, 3, 1, 90.0, 90.0
                            ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                             new Insets(5, 0, 5, 0), 0, 0));
        
        JPanel panelActions = new JPanel();
        panelActions.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panelActions.add(buttonStart);
        
        getContentPane().add(ComponentFactory.getLabel(DcResources.getText("lblMaximumImageResolution")), 
        		 Layout.getGBC( 1, 2, 2, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                 new Insets(5, 10, 5, 5), 0, 0));
        
        getContentPane().add(cbResolution, Layout.getGBC( 3, 2, 1, 1, 1.0, 1.0
               ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 10), 0, 0));        
        
        textMessage.setBackground(panelActions.getBackground());
        
        getContentPane().add(panelActions, Layout.getGBC( 3, 3, 1, 1, 0.0, 0.0
                            ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                             new Insets(5, 5, 5, 5), 0, 0));
        
        
        getContentPane().add(progressBar, 
       		 Layout.getGBC( 1, 4, 3, 1, 1.0, 1.0
               ,GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL,
                new Insets(5, 10, 5, 10), 0, 0));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("start"))
        	resizeImages();
    }     
}
