package org.datacrow.client.console.components.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JPanel;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcLabel;

public class StatusBar extends JPanel {
	
	private final DcLabel lblMessage = ComponentFactory.getLabel("");
	
	public StatusBar() {
		build();
	}
	
	private void build() {
		setLayout(Layout.getGBL());
		
		add(lblMessage, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                 new Insets( 0, 0, 5, 0), 0, 0));
	}
	
	public void setMessage(String txt) {
		lblMessage.setText(txt);
	}
}
