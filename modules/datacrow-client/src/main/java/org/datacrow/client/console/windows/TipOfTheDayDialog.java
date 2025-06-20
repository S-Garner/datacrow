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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.client.console.components.DcButton;
import org.datacrow.client.console.components.DcCheckBox;
import org.datacrow.client.console.components.DcHtmlEditorPane;
import org.datacrow.client.util.Utilities;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;

public class TipOfTheDayDialog extends DcDialog implements ActionListener {

    private final DcHtmlEditorPane tipPane = ComponentFactory.getHtmlEditorPane();
    private final DcCheckBox checkShowTips = ComponentFactory.getCheckBox(DcResources.getText("lblShowTipsOnStartup"));
    
    private final List<String> tips = new LinkedList<String>();
    
    private int currentTip = 0;
    
    public TipOfTheDayDialog() {
        super(GUI.getInstance().getMainFrame());
        
        setTitle(DcResources.getText("lblTipOfTheDay"));
        setIconImage(IconLibrary._icoTips.getImage());
        
        build();
        setSize(400, 300);
        setCenteredLocation();
    }
    
    private void showNextTip() {
        currentTip = currentTip < tips.size() -1 ? currentTip + 1 : 0; 
        showNextTip(currentTip);
    }    

    private void showNextTip(int idx) {
        String tip = tips.get(idx);
        String html = "<html><body " + Utilities.getHtmlStyle() + ">" + tip + "</body></html>";
        tipPane.setHtml(html);
    }    
    
    private void build() {
        getContentPane().setLayout(Layout.getGBL());
        
        DcButton buttonNext = ComponentFactory.getButton(DcResources.getText("lblNext"));
        DcButton buttonClose = ComponentFactory.getButton(DcResources.getText("lblClose"));
        
        buttonNext.addActionListener(this);
        buttonNext.setActionCommand("next");
        
        buttonClose.addActionListener(this);
        buttonClose.setActionCommand("close");
        
        checkShowTips.addActionListener(this);
        checkShowTips.setActionCommand("toggleActive");
        checkShowTips.setSelected(DcSettings.getBoolean(DcRepository.Settings.stShowTipsOnStartup));
        
        JScrollPane scroller = new JScrollPane(tipPane);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JPanel panelActions = new JPanel();
        panelActions.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panelActions.add(buttonNext);
        panelActions.add(buttonClose);
        
        getContentPane().add(scroller,      Layout.getGBC( 0, 0, 4, 1, 30.0, 30.0
                ,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                 new Insets( 5, 5, 5, 5), 0, 0));
        getContentPane().add(checkShowTips, Layout.getGBC( 0, 1, 1, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 5), 0, 0));
        getContentPane().add(panelActions,  Layout.getGBC( 0, 2, 3, 1, 1.0, 1.0
                ,GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                 new Insets( 5, 5, 5, 0), 0, 0));
        
        setResizable(false);
        pack();

        initializeTips();
        currentTip = new Random().nextInt(tips.size());
        showNextTip(currentTip);
    }
    
    private void initializeTips() {
        Map<String, String> map = DcResources.getCurrent().getResourcesMap();
        String key;
        for (Object o : map.keySet()) {
            key = (String) o;
            if (key.startsWith("tip")) 
                tips.add(map.get(key));
        }
    }
    
    @Override
    public void close() {
        tips.clear();
        super.close();
    }    
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("toggleActive"))
            DcSettings.set(DcRepository.Settings.stShowTipsOnStartup, checkShowTips.isSelected());
        else if (ae.getActionCommand().equals("next"))
            showNextTip();
        else if (ae.getActionCommand().equals("close"))
            close();
    }    
}
