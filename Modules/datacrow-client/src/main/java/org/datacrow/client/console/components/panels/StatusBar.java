package org.datacrow.client.console.components.panels;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcLabel;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.resources.DcResources;

public class StatusBar extends JPanel {
	
	private final DcLabel lblMessage = ComponentFactory.getLabel("");
	private final DcLabel lblTaskRunning = ComponentFactory.getLabel("");
	
	private final DcLabel icoWhite = ComponentFactory.getLabel(IconLibrary._icoBulletWhite);
	private final DcLabel icoGreen = ComponentFactory.getLabel(IconLibrary._icoBulletGreen);
	private final DcLabel icoRed = ComponentFactory.getLabel(IconLibrary._icoBulletRed);
	
	private IconChangeThread iconChangeThread;
	
	public StatusBar() {
		build();
	}
	
	private void build() {
		setLayout(Layout.getGBL());
		
		add(lblMessage, Layout.getGBC( 0, 0, 1, 1, 50.0, 1.0
                ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 0, 0), 0, 0));
		
		lblTaskRunning.setHorizontalTextPosition(SwingConstants.RIGHT);

		JPanel panelIcons = new JPanel();
		panelIcons.setLayout(Layout.getGBL());
		panelIcons.add(icoGreen, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 5, 0), 0, 0));
		panelIcons.add(icoRed,   Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 5, 0), 0, 0));
		panelIcons.add(icoWhite, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 5, 0), 0, 0));
		
		panelIcons.setBorder(null);

		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout (FlowLayout.TRAILING, 2, 0));

		panel.add(lblTaskRunning);
		panel.add(panelIcons);
		
		add(panel, Layout.getGBC( 1, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.EAST, GridBagConstraints.NONE,
                 new Insets( 0, 0, 0, 0), 0, 0));
		
		icoGreen.setVisible(false);
		icoRed.setVisible(false);
		icoWhite.setVisible(true);
		
	}
	
	public void setMessage(String s) {
		if (SwingUtilities.isEventDispatchThread()) {
			lblMessage.setText(s);
		} else {
			
			SwingUtilities.invokeLater(
                    new Thread(new Runnable() { 
                        @Override
                        public void run() {
                        	lblMessage.setText(s);
                        }
                    }));
		}
	}
	
	public void setTaskMessage(String s) {
		if (SwingUtilities.isEventDispatchThread()) {
			lblTaskRunning.setText(s);
		} else {
			
			SwingUtilities.invokeLater(
                    new Thread(new Runnable() { 
                        @Override
                        public void run() {
                        	lblTaskRunning.setText(s);                     
                        }
                    }));
		}
	}
	
	public void setTaskRunning(boolean b) {
		if (SwingUtilities.isEventDispatchThread()) {
			processTaskStatus(b);
		} else {
			
			SwingUtilities.invokeLater(
                    new Thread(new Runnable() { 
                        @Override
                        public void run() {
                        	processTaskStatus(b);                        
                        }
                    }));
		}
	}
	
	private void processTaskStatus(boolean taskRunning) {
		if (!taskRunning) {
			icoGreen.setVisible(false);
			icoRed.setVisible(false);
			icoWhite.setVisible(true);
			lblTaskRunning.setText(DcResources.getText("msgCompleted"));
			lblMessage.setText("");
			iconChangeThread.cancel();
		}
		
		if (taskRunning) {
			if (iconChangeThread != null)
				iconChangeThread.cancel();
			
			iconChangeThread = new IconChangeThread();
			iconChangeThread.start();
		}		
	}
	
	private class IconChangeThread extends Thread {
		
		private boolean canceled = false;
		
		private void cancel() {
			canceled = true;
		}
		
		@Override
		public void run() {
			
			while (!canceled) {
	            SwingUtilities.invokeLater(
	                    new Thread(new Runnable() { 
	                        @Override
	                        public void run() {
	                           	if (icoRed.isVisible()) {
	                           		icoWhite.setVisible(false);
	                        		icoGreen.setVisible(true);
	                        		icoRed.setVisible(false);
	                        	} else {
	                        		icoWhite.setVisible(false);
	                        		icoGreen.setVisible(false);
	                        		icoRed.setVisible(true);
	                        	}                               
	                        }
	                    }));
	            
	            try { sleep(100); } catch (Exception ignore) {}
	        }
			
			SwingUtilities.invokeLater(
                    new Thread(new Runnable() { 
                        @Override
                        public void run() {
                        	icoGreen.setVisible(false);
                    		icoRed.setVisible(false);
                    		icoWhite.setVisible(true);
                        }
                    }));
		}
	}
}