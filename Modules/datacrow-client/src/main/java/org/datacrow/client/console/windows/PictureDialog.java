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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcPictureField;
import org.datacrow.client.console.components.panels.PicturesPanel;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;

public class PictureDialog extends DcDialog implements ActionListener {
	
    private final DcPictureField pf = ComponentFactory.getPictureField(true, true);
    private final PicturesPanel picturesPanel;

    public PictureDialog(PicturesPanel picturesPanel, Picture picture) {

    	super();
 
        setTitle(DcResources.getText("lblPictureViewer"));
        
        this.picturesPanel = picturesPanel;
        
        getContentPane().setLayout(Layout.getGBL());
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });
        
        JPanel panelActions = new JPanel();
        panelActions.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton btnSave = ComponentFactory.getButton(DcResources.getText("lblSave"));
        btnSave.addActionListener(this);
        btnSave.setActionCommand("save");
        panelActions.add(btnSave);

        JButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        panelActions.add(buttonClose);
        
        getContentPane().add(pf, Layout.getGBC(0, 0, 1, 1, 10.0, 10.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets(5, 5, 5, 5), 0, 0));        
        getContentPane().add(panelActions, Layout.getGBC(0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets(0, 0, 5, 5), 0, 0));
        
        pf.setPicture(picture);
        
        pack();
        
        setSize(DcSettings.getDimension(DcRepository.Settings.stPictureViewerDialogSize));
        
        setCenteredLocation();
        setModal(true);
        setVisible(true);
    }
    
    @Override
    public void close() {
    	DcSettings.set(DcRepository.Settings.stPictureViewerDialogSize, getSize());
        super.close();
    }
    
    public boolean isPictureChanged() {
        return pf != null ? pf.isChanged() : false;
    }
    
    public DcImageIcon getImage() {
        return pf.getPicture() != null ? pf.getPicture().getImageIcon() : null;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("close")) {
        	close();
        } else if (ae.getActionCommand().equals("save")) {
        	Picture p = pf.getPicture();
        	DcConfig.getInstance().getConnector().savePicture(p);
        	picturesPanel.load(DcModules.getCurrent().getIndex());
            close();
        }
    }    
}
