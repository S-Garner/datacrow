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

package org.datacrow.client.console.menu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.components.DcComboBox;
import org.datacrow.client.console.components.DcLabel;
import org.datacrow.client.console.components.DcShortTextField;
import org.datacrow.client.console.components.DcToolBarButton;
import org.datacrow.client.console.windows.filtering.FilterDialog;
import org.datacrow.client.plugins.PluginHelper;
import org.datacrow.client.util.PollerTask;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.console.IMasterView;
import org.datacrow.core.data.DataFilter;
import org.datacrow.core.data.DataFilters;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.plugin.Plugin;
import org.datacrow.core.plugin.Plugins;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.utilities.CoreUtilities;

public class DcToolBar extends JToolBar implements ActionListener, MouseListener, KeyListener {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DcToolBar.class.getName());
    
    private final DcShortTextField fldFilter = ComponentFactory.getShortTextField(255);
    private final DcComboBox<Object> comboFilters = ComponentFactory.getComboBox();
    
    private int moduleIdx;

    public DcToolBar(DcModule module) {
        this.moduleIdx = module.getIndex();
        
        build(module);
        
        setLabelsVisible(false);
        setFloatable(false);
	}
    
    private void search() {
        DataFilter df = DataFilters.createSearchAllFilter(moduleIdx, getSearchString());
        search(df);
    }
    
    public void clearFilter() {
        if (comboFilters != null && comboFilters.getItemCount() > 0) {
            comboFilters.removeActionListener(this);
            comboFilters.setSelectedIndex(0);
        }
        
        if (comboFilters != null) comboFilters.addActionListener(this);
        
        fldFilter.setText("");
        
        search(null);
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("searchOnSelectedFilter")) {
            search(comboFilters.getSelectedIndex() > 0 ? (DataFilter) comboFilters.getSelectedItem() : null);    
        } else if (ae.getActionCommand().equals("search")) {
            search();
        } else if (ae.getActionCommand().equals("cancel")) {
            clearFilter();
        } else if (ae.getActionCommand().equals("advanced")) {
            FilterDialog fd = GUI.getInstance().getFilterDialog(moduleIdx);
            fd.setVisible(true);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            search();
    }    
    
    private void search(DataFilter df) {
        FilterTask ft = new FilterTask(df);
        ft.start();
    }
    
    private String getSearchString() {
        return fldFilter.getText();
    }    
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        
        Component c;
        int i = 0;
        while ((c = getComponentAtIndex(i++)) != null) {
            if (c instanceof DcToolBarButton) {
                DcToolBarButton button = (DcToolBarButton) c;
                button.setFont(ComponentFactory.getSystemFont());
            }
        }
    }    
    
    private void build(DcModule module) {
        
        PluginHelper.add(this, "NewItemWizard");
        PluginHelper.add(this, "NewItems");
        PluginHelper.add(this, "CreateNew", module.getIndex(), Plugin._VIEWTYPE_SEARCH);
        
        if (!module.isAbstract())
            PluginHelper.add(this, "Delete", module.getIndex(), Plugin._VIEWTYPE_SEARCH);
        
        PluginHelper.add(this, "OpenItem", module.getIndex(), Plugin._VIEWTYPE_SEARCH);
        PluginHelper.add(this, "SaveSelected", module.getIndex(), Plugin._VIEWTYPE_SEARCH);
        PluginHelper.add(this, "SaveAll", module.getIndex(), Plugin._VIEWTYPE_SEARCH);
        
        if (module.hasOnlineServices()) {
            PluginHelper.add(this, "OnlineSearch");
        }
            
        PluginHelper.add(this, "Charts");
        
        Collection<Plugin> plugins = Plugins.getInstance().getUserPlugins(null, -1, module.getIndex(), Plugin._VIEWTYPE_SEARCH);
        for (Plugin plugin : plugins) {
            if (plugin.isShowOnToolbar())
                add(ComponentFactory.getToolBarButton(plugin));
        }
        
        Collection<DataFilter> filters = DataFilters.get(moduleIdx);
        comboFilters.addItem(" ");
        for (DataFilter df : filters)
            comboFilters.addItem(df);
        
        comboFilters.addActionListener(this);
        comboFilters.setActionCommand("filterSelected");
        
        comboFilters.setPreferredSize(new Dimension(150, ComponentFactory.getPreferredFieldHeight()));
        comboFilters.setMinimumSize(new Dimension(150, ComponentFactory.getPreferredFieldHeight()));
        
        fldFilter.setMinimumSize(new Dimension(150, ComponentFactory.getPreferredFieldHeight()));
        fldFilter.setPreferredSize(new Dimension(150, ComponentFactory.getPreferredFieldHeight()));
        fldFilter.addKeyListener(this);
        
        DcLabel lblSearch = ComponentFactory.getLabel(DcResources.getText("lblQuickFilter") + " ");
        lblSearch.setBorder(new CompoundBorder(lblSearch.getBorder(), new EmptyBorder(0,20,0,5)));
        
        add(lblSearch);
        add(fldFilter);

        JButton buttonAdvanced = ComponentFactory.getIconButton(IconLibrary._icoSearch);
        buttonAdvanced.setActionCommand("advanced");
        buttonAdvanced.addActionListener(this);
        
        JButton buttonCancel = ComponentFactory.getIconButton(IconLibrary._icoSearchReset);
        buttonCancel.setActionCommand("cancel");
        buttonCancel.addActionListener(this);
        
        JButton button1 = ComponentFactory.getIconButton(IconLibrary._icoAccept);
        button1.setActionCommand("search");
        button1.addActionListener(this);

        JButton button2 = ComponentFactory.getIconButton(IconLibrary._icoAccept);
        button2.setActionCommand("searchOnSelectedFilter");
        button2.addActionListener(this);

        add(button1);
        
        if (filters.size() > 0) {
            DcLabel lblFilters = ComponentFactory.getLabel(DcResources.getText("lblFilters") + " ");
            lblFilters.setBorder(new CompoundBorder(lblSearch.getBorder(), new EmptyBorder(0,20,0,0)));

            add(lblFilters);
            add(comboFilters);
            add(button2);
        }

        add(buttonAdvanced);
        add(buttonCancel);
    }
    
	public void setLabelsVisible(boolean b) {
		Component c;
		int i = 0;
		while ((c = getComponentAtIndex(i++)) != null) {
            if (c instanceof DcToolBarButton) {
                DcToolBarButton button = (DcToolBarButton) c;
                
                if (b) button.showText();
                else button.hideText();
            }
		}
        DcSettings.set(DcRepository.Settings.stShowMenuBarLabels, b);
	}
	

    @Override
    public void keyPressed(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getClickCount() == 2) {
            search();
        }            
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(GUI.getInstance().setRenderingHint(g));
    }
    
    private class FilterTask extends Thread {
        
        private final DataFilter df;
        
        public FilterTask(DataFilter df) {
            this.df = df;
        }
        
        @Override
        public void run() {
            PollerTask poller = new PollerTask(this, DcResources.getText("lblFiltering"));
            poller.start();
            
            DataFilters.setCurrent(moduleIdx, df);
            
            // do not query here if the grouping pane is enabled; the grouping pane will 
            // execute the query by itself..
            IMasterView mv = GUI.getInstance().getSearchView(moduleIdx);
            
            Connector connector = DcConfig.getInstance().getConnector();
            Map<String, Integer> keys = 
                    mv.getGroupingPane() != null && mv.getGroupingPane().isEnabled() ?
                        new HashMap<String, Integer>() :
                            connector.getKeys(df == null ? DataFilters.getCurrent(moduleIdx) : df);
                        
            mv.add(keys);
            
            try {
                poller.finished(true);
            } catch (Exception e) {
                logger.error(e, e);
                GUI.getInstance().displayErrorMessage(CoreUtilities.isEmpty(e.getMessage()) ? e.toString() : e.getMessage());
            }
        }
    }    
}