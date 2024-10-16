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

package org.datacrow.client.console.components.renderers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.components.tables.DcTable;
import org.datacrow.core.DcRepository;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.DcSettings;

public class DcTableCellRenderer extends DefaultTableCellRenderer {

	private static final EmptyBorder border = new EmptyBorder(0, 5, 0, 5);
    private static final DcTableCellRenderer instance = new DcTableCellRenderer();
    
    private boolean disabled = false;

    protected DcTableCellRenderer() {}
    
    public static DcTableCellRenderer getInstance() {
        return instance;
    }
    
    protected boolean allowTooltips() {
    	return true;
    }
    
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        JComponent c = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (((DcTable) table).isIgnoringPaintRequests())
            return c;
        
        ((DcTable) table).load(row);
        
        if (DcSettings.getBoolean(DcRepository.Settings.stShowTableTooltip) && allowTooltips()) {
        	if (disabled) {
        		ToolTipManager.sharedInstance().registerComponent(table);
                ToolTipManager.sharedInstance().registerComponent(table.getTableHeader());
                disabled = false;
        	} 
            
            ToolTipManager.sharedInstance().setDismissDelay(2000);
            setToolTipText(getTipText());

        } else {
            if (!disabled) { 
                disabled = true;
            	ToolTipManager.sharedInstance().unregisterComponent(table);
            	ToolTipManager.sharedInstance().unregisterComponent(table.getTableHeader());
            }
        }
        
        Color colorOddRow = ComponentFactory.getColor(DcRepository.Settings.stOddRowColor);
        Color colorEvenRow = ComponentFactory.getColor(DcRepository.Settings.stEvenRowColor);
        Color colorRowSelection = ComponentFactory.getColor(DcRepository.Settings.stSelectionColor);
        Font font = ComponentFactory.getStandardFont();
        setFont(font);
        setForeground(ComponentFactory.getCurrentForegroundColor());
        
        try {
            int idx = ((DcTable) table).getColumnIndexForField(DcObject._SYS_LOANSTATUS);
            if (idx != -1) {
                if (DcResources.getText("lblLoanOverdue").equals(((DcTable) table).getValueAt(row, idx, true)))
                    setForeground(Color.RED);
            }
        } catch (Exception ignore) {}

    	if (!isSelected) {
            if ((row % 2) == 0) {
        		setBackground(colorEvenRow);
            } else {
                setBackground(colorOddRow);
            }
        } else {
            setBackground(colorRowSelection);
            
            if (hasFocus) {
                int red = colorRowSelection.getRed() > 20 ? colorRowSelection.getRed() - 20 : colorRowSelection.getRed();
                int green = colorRowSelection.getGreen() > 20 ? colorRowSelection.getGreen() - 20 : colorRowSelection.getGreen();
                int blue = colorRowSelection.getBlue() > 20 ? colorRowSelection.getBlue() - 20 : colorRowSelection.getBlue();
                
                red = red < 0 ? 0 : red > 255 ? 255 : red;
                green = green < 0 ? 0 : green > 255 ? 255 : green;
                blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;
                
                setBackground(new Color(red, green, blue));
            }
        }
    	
        setBorder(border);
    	return c;

    }
    
    protected String getTipText() {
        return super.getText();
    }
}