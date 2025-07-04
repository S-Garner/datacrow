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

package org.datacrow.client.console.windows.log;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.Layout;
import org.datacrow.core.DcConfig;
import org.datacrow.core.log.DcLog;
import org.datacrow.core.log.ILogListener;

public class LogPanel extends JPanel implements ILogListener {
    
	private static final LogPanel me = new LogPanel();
	
    private final JLabel labelVersion = ComponentFactory.getLabel(DcConfig.getInstance().getVersion().getFullString());
    
    private JTextArea logger;

    public static LogPanel getInstance() {
        return me;
    }
    
    private LogPanel() {
        buildPanel();
        DcLog.getInstance().addListener(this);
    }
    
    @Override
    public void setFont(Font font) {
        if (logger != null) {
            labelVersion.setFont(ComponentFactory.getSystemFont());
            logger.setFont(ComponentFactory.getStandardFont());
        }
    }
    
    @Override
    public void add(String message) {
        logger.insert("\r\n", 0);
        logger.insert(message, 0);
        logger.setCaretPosition(0);
    }      

    private void buildPanel() {
        
        JPanel panelLog = new JPanel();

        //**********************************************************
        //Logging panel
        //**********************************************************
        panelLog.setLayout(Layout.getGBL());

        logger = new JTextArea();
        logger.setFont(ComponentFactory.getStandardFont());
        logger.setEditable(false);
        
        JScrollPane scroller = new JScrollPane(logger);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panelLog.add(scroller, Layout.getGBC( 0, 0, 1, 1, 1.0, 1.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 0, 5, 5, 5), 0, 0));

        //**********************************************************
        //Main panel
        //**********************************************************
        setLayout(Layout.getGBL());
        
        add(panelLog,    Layout.getGBC( 0, 1, 1, 1, 20.0, 50.0
           ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
            new Insets( 5, 0, 0, 0), 0, 0));
    }
}
