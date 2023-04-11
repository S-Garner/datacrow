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

import java.net.InetAddress;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.components.DcNumberField;
import org.datacrow.client.console.components.DcShortTextField;
import org.datacrow.client.console.windows.DcDialog;
import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.settings.Settings;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.synch.service.SynchService;

/**
 * @author RJ
 *
 */
public class SynchServiceDialog extends DcDialog {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(SynchServiceDialog.class.getName());
	
    private DcShortTextField fldName = ComponentFactory.getShortTextField(255);
    private DcShortTextField fldAddress = ComponentFactory.getShortTextField(255);
    private DcNumberField fldPort = ComponentFactory.getNumberField();
	
	protected Settings settings;
	
	private SynchService service;
	
	public SynchServiceDialog() {
        super();
		
		settings = DcSettings.getSettings();
		
		//setTitle(DcResources.getText(""));
		
		build();
		init();
		
		setSize(settings.getDimension(DcRepository.Settings.stSynchServiceDialogSize));
	}
	
	private void init() {
		try {
			InetAddress inet = InetAddress.getLocalHost();
			fldAddress.setText(inet.getHostName());
		} catch (Exception e) {
			logger.debug("Could not retrieve the host name for this device");
		}
	}
	
	private void build() {
		
	}
	
	private void startService() {
		saveSettings();
		
		String name = settings.getString(DcRepository.Settings.stSynchServiceName);
		String address = settings.getString(DcRepository.Settings.stSynchServiceAddress);
		int port = settings.getInt(DcRepository.Settings.stSynchServicePort);
		
		service = new SynchService(name, address, port);
	}
	
	private void cancel() {
		if (service != null)
			service.shutdown();
	}
	
	protected void saveSettings() {
        settings.set(DcRepository.Settings.stSynchServiceDialogSize, getSize());
        
        String name = fldName.getText();
		String address = fldAddress.getText();
		Long port = ((Long) fldPort.getValue());
		
		if (!CoreUtilities.isEmpty(name))
			settings.set(DcRepository.Settings.stSynchServiceName, name);
		
		if (!CoreUtilities.isEmpty(address))
			settings.set(DcRepository.Settings.stSynchServiceAddress, address);
		
		if (!CoreUtilities.isEmpty(port))
			settings.set(DcRepository.Settings.stSynchServicePort, port);		
    }
	
	@Override
    public void close() {
        cancel();
        saveSettings();
	}
}
