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

package org.datacrow.client.console.windows.synch;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcLongTextField;
import org.datacrow.client.console.components.DcNumberField;
import org.datacrow.client.console.components.DcShortTextField;
import org.datacrow.client.console.windows.DcDialog;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.settings.Settings;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.synch.service.ISynchServiceListener;
import org.datacrow.synch.service.SynchService;

/**
 * @author RJ
 *
 */
public class SynchServiceDialog extends DcDialog implements ActionListener, ISynchServiceListener {
	
    private final DcShortTextField fldName = ComponentFactory.getShortTextField(255);
    private final DcNumberField fldPort = ComponentFactory.getNumberField();
    private final JTextArea txtLog = ComponentFactory.getTextArea();
	
	private final Settings settings;
	
	private SynchService service;
	
	public SynchServiceDialog() {
        super();
        
        setIconImage(IconLibrary._icoSynchService.getImage());
		
		settings = DcSettings.getSettings();
		
		setTitle(DcResources.getText("lblSynchService"));
		
		build();
		init();
		
		setSize(settings.getDimension(DcRepository.Settings.stSynchServiceDialogSize));
		setCenteredLocation();
	}
	
	private void init() {
		if (fldName.getText().length() == 0)
			fldName.setText(settings.getString(DcRepository.Settings.stSynchServiceName));

		fldPort.setValue(settings.getInt(DcRepository.Settings.stSynchServicePort));
	}
	
	private void build() {
        getContentPane().setLayout(Layout.getGBL());

        //**********************************************************
        //Help Panel
        //**********************************************************
        DcLongTextField help = ComponentFactory.getHelpTextField();
        help.setText(DcResources.getText("msgSynchServiceHelp"));
        
        getContentPane().add(help,   
                Layout.getGBC(0, 0, 2, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));
        
        //**********************************************************
        //Input Panel
        //**********************************************************
        getContentPane().add(ComponentFactory.getLabel(DcResources.getText("lblSynchServiceName")),   
                Layout.getGBC(0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(fldName, Layout.getGBC(1, 1, 1, 1, 50.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(ComponentFactory.getLabel(DcResources.getText("lblSynchServicePort")),   
                Layout.getGBC(0, 3, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 0, 0));
        getContentPane().add(fldPort, Layout.getGBC(1, 3, 1, 1, 50.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        
        //**********************************************************
        //Log Panel
        //**********************************************************
        JPanel panelLog = new JPanel();
        panelLog.setLayout(Layout.getGBL());

        txtLog.setEditable(false);
        JScrollPane scroller = new JScrollPane(txtLog);
//        scroller.setBorder(new EmptyBorder(1, 1, 1, 1));
        
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panelLog.setBorder(ComponentFactory.getTitleBorder(DcResources.getText("lblLog")));
        panelLog.add(scroller, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                    ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                     new Insets(5, 5, 5, 5), 0, 0));
        
        
        getContentPane().add(panelLog, Layout.getGBC(0, 4, 2, 1, 10.0, 10.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));
        
        //**********************************************************
        //Action Panel
        //**********************************************************
        JPanel panelActions = new JPanel();
        
        JButton btStart = ComponentFactory.getButton(DcResources.getText("lblStart"));
        JButton btClose = ComponentFactory.getButton(DcResources.getText("lblClose"));

        btStart.addActionListener(this);
        btStart.setActionCommand("start");

        btClose.addActionListener(this);
        btClose.setActionCommand("close");
        
        panelActions.add(btStart);
        panelActions.add(btClose);
        
        getContentPane().add(panelActions, Layout.getGBC(0, 5, 2, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
	}
	
	private void startService() {
		saveSettings();
		
		String name = settings.getString(DcRepository.Settings.stSynchServiceName);
		int port = settings.getInt(DcRepository.Settings.stSynchServicePort);
		
		service = new SynchService(name, port);
		service.setListener(this);
		service.start();
	}
	
	protected void saveSettings() {
        settings.set(DcRepository.Settings.stSynchServiceDialogSize, getSize());
        
        String name = fldName.getText();
		Long port = ((Long) fldPort.getValue());
		
		if (!CoreUtilities.isEmpty(name))
			settings.set(DcRepository.Settings.stSynchServiceName, name);
		
		if (!CoreUtilities.isEmpty(port))
			settings.set(DcRepository.Settings.stSynchServicePort, port);		
    }
	
	@Override
    public void close() {
		saveSettings();

		if (service != null)
			service.shutdown();
		
		super.close();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand().equals("start"))
			startService();
		else if (ae.getActionCommand().equals("close"))
			close();
	}

	@Override
	public void addError(String error, Throwable t) {
		if (txtLog != null) txtLog.insert(error + '\n', 0);
		if (txtLog != null) txtLog.insert(t.getMessage() + '\n', 0);
	}

	@Override
	public void addMessage(String msg) {
		if (txtLog != null) txtLog.insert(msg + '\n', 0);
	}
}
